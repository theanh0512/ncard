/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.user.ncard.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.user.ncard.BuildConfig
import com.user.ncard.api.*
import com.user.ncard.db.*
import com.user.ncard.db.NcardDb.Companion.MIGRATION_1_2
import com.user.ncard.db.NcardDb.Companion.MIGRATION_2_3
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.util.*
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = arrayOf(ViewModelModule::class))
class AppModule {
    @Singleton
    @Provides
    fun provideContext(application: Application): Context {
        return application
    }

    @Singleton
    @Provides
    fun provideCredentialProvider(application: Application): CognitoCachingCredentialsProvider {
        return CognitoCachingCredentialsProvider(
                application,
                AppHelper.getCognitoPoolId(),
                Regions.fromName(AppHelper.getCognitoPoolRegion()))
    }

    @Singleton
    @Provides
    fun provideS3Client(application: Application, credentialsProvider: CognitoCachingCredentialsProvider): AmazonS3 {
        val sS3Client = AmazonS3Client(credentialsProvider)
        sS3Client.setRegion(Region.getRegion(Regions.fromName(AppHelper.BUCKET_REGION)))
        return sS3Client
    }

    @Singleton
    @Provides
    fun provideTransferUtility(application: Application, s3: AmazonS3): TransferUtility {
        return TransferUtility.builder().s3Client(s3).defaultBucket(AppHelper.getCognitoBucketName())
                .context(application).build()
    }

    @Singleton
    @Provides
    fun provideChangeableBaseUrlInterceptor(): ChangeableBaseUrlInterceptor {
        return ChangeableBaseUrlInterceptor()
    }

    @Provides
    @Singleton
    fun provideIntercepter(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideAuthenticator(context: Context): TokenAuthenticator {
        return TokenAuthenticator(SharedPreferenceHelper(context), context)
    }

    @Provides
    @Singleton
    fun provideAddCookiesInterceptor(context: Context): AddCookiesInterceptor {
        return AddCookiesInterceptor(SharedPreferenceHelper(context), context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(addCookiesInterceptor: AddCookiesInterceptor, changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor, httpLoggingInterceptor: HttpLoggingInterceptor, tokenAuthenticator: TokenAuthenticator): OkHttpClient { // The Interceptor is then added to the client
        return OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                //.addInterceptor(changeableBaseUrlInterceptor)
                .addInterceptor(addCookiesInterceptor)
                .authenticator(tokenAuthenticator)
                .build()
    }

    @Singleton
    @Provides
    fun provideNCardService(context: Context, okHttpClient: OkHttpClient): NCardService {
        return Retrofit.Builder()
                //.baseUrl(if (BuildConfig.DEBUG && Constants.DEV) "https://localhost/dev/" else "https://localhost/production/")
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(NCardService::class.java)
    }

    @Provides
    @Singleton
    @Named("scanOkhttp")
    fun provideOkHttpClientScanCard(addCookiesInterceptor: AddCookiesInterceptor, httpLoggingInterceptor: HttpLoggingInterceptor, tokenAuthenticator: TokenAuthenticator): OkHttpClient { // The Interceptor is then added to the client
        return OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(addCookiesInterceptor)
                .authenticator(tokenAuthenticator)
                .build()
    }

    @Singleton
    @Provides
    fun provideScanCardService(context: Context, @Named("scanOkhttp") okHttpClient: OkHttpClient): ScanCardService {
        return Retrofit.Builder()
                .baseUrl(Constants.SCAN_NAME_CARD_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ScanCardService::class.java)
    }

    @Singleton
    @Provides
    fun provideChatService(chatHelper: ChatHelper): ChatService {
        return ChatServiceImpl(chatHelper)
    }

    @Singleton
    @Provides
    fun providesNCardDatabase(context: Context): NcardDb {
        return Room.databaseBuilder(
                context.applicationContext, NcardDb::class.java, "ncard.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }

    @Provides
    @Singleton
    fun provideFFmpeg(context: Context): FFmpeg {
        return FFmpeg.getInstance(context.applicationContext)
    }

    @Singleton
    @Provides
    fun provideUserDao(db: NcardDb): UserDao {
        return db.userDao()
    }

    @Singleton
    @Provides
    fun provideFriendDao(db: NcardDb): FriendDao {
        return db.friendDao()
    }

    @Singleton
    @Provides
    fun provideFilterDao(db: NcardDb): FilterDao {
        return db.filterDao()
    }

    @Singleton
    @Provides
    fun provideCatalogueDao(db: NcardDb): CatalogueDao {
        return db.catalogueDao()
    }

    @Singleton
    @Provides
    fun provideNameCardDao(db: NcardDb): NameCardDao {
        return db.nameCardDao()
    }

    @Singleton
    @Provides
    fun provideGroupDao(db: NcardDb): GroupDao {
        return db.groupDao()
    }

    @Singleton
    @Provides
    fun provideChatDialogDao(db: NcardDb): ChatDialogDao {
        return db.chatDialogDao()
    }

    @Singleton
    @Provides
    fun provideChatMessageDao(db: NcardDb): ChatMessageDao {
        return db.chatMessageDao()
    }

    @Singleton
    @Provides
    fun provideNCardInfoDao(db: NcardDb): NCardInfoDao {
        return db.nCardInfoDao()
    }

    @Singleton
    @Provides
    fun provideChatHelper(): ChatHelper {
        return ChatHelper.instance
    }

    @Singleton
    @Provides
    fun provideJobDao(db: NcardDb): JobDao {
        return db.jobDao()
    }

    @Singleton
    @Provides
    fun provideBroadcastGroupDao(db: NcardDb): BroadcastGroupDao {
        return db.broadcastGroupDao()
    }
}
