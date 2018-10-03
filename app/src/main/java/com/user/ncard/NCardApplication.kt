package com.user.ncard

import android.app.Activity
import android.app.Application
import com.barryzhang.temptyview.TEmptyView
import com.barryzhang.temptyview.TViewUtil
import com.bilibili.boxing.BoxingCrop
import com.bilibili.boxing.BoxingMediaLoader
import com.crashlytics.android.Crashlytics
import com.quickblox.auth.session.QBSession
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.QBSessionParameters
import com.quickblox.auth.session.QBSettings

import com.user.ncard.di.AppInjector
import com.user.ncard.util.AppHelper
import com.user.ncard.util.Constants
import com.user.ncard.util.Utils
import javax.inject.Inject
import com.devbrackets.android.exomedia.ExoMedia
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.user.ncard.ui.catalogue.utils.BoxingFrescoLoader
import com.user.ncard.ui.catalogue.utils.BoxingUcrop
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import okhttp3.OkHttpClient
import com.user.ncard.ui.chats.utils.gcm.ActivityLifecycle
import io.fabric.sdk.android.Fabric

/**
 * Created by Pham on 27/7/17.
 */

class NCardApplication : Application(), HasActivityInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    val TAG = "NCardApplication"

    override fun onCreate() {
        super.onCreate()
        AppHelper.init(this)
        AppInjector.init(this)
        if(!BuildConfig.DEBUG) Fabric.with(this, Crashlytics())

        //for chat
        initQBSessionManager()
        initChatConfiguration()

        configureExoMedia()
        initEmptyRecyclerView()
        iniBoxingLoader()
        ActivityLifecycle.init(this)
    }

    override fun activityInjector(): DispatchingAndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }

    private fun initChatConfiguration() {
        QBSettings.getInstance().init(applicationContext, Constants.APP_ID, Constants.AUTH_KEY,
                Constants.AUTH_SECRET);
        QBSettings.getInstance().accountKey = Constants.ACCOUNT_KEY
    }

    private fun initQBSessionManager() {
        QBSessionManager.getInstance().addListener(object : QBSessionManager.QBSessionListener {
            override fun onSessionCreated(qbSession: QBSession) {
                Utils.Log(TAG, "Chat Session Created")
            }

            override fun onSessionUpdated(qbSessionParameters: QBSessionParameters) {
                Utils.Log(TAG, "Chat  Session Updated")
            }

            override fun onSessionDeleted() {
                Utils.Log(TAG, "Chat Session Deleted")
            }

            override fun onSessionRestored(qbSession: QBSession) {
                Utils.Log(TAG, "Chat Session Restored")
            }

            override fun onSessionExpired() {
                Utils.Log(TAG, "Chat  Session Expired")
            }

            override fun onProviderSessionExpired(provider: String) {
                Utils.Log(TAG, "Chat  Session Expired for provider:" + provider)
            }
        })
    }

    private fun configureExoMedia() {
        // Registers the media sources to use the OkHttp client instead of the standard Apache one
        // Note: the OkHttpDataSourceFactory can be found in the ExoPlayer extension library `extension-okhttp`
        ExoMedia.setHttpDataSourceFactoryProvider { userAgent, listener ->
            OkHttpDataSourceFactory(OkHttpClient(), userAgent, listener)
        }
    }

    private fun initEmptyRecyclerView() {
        TEmptyView.init(TViewUtil.EmptyViewBuilder.getInstance(this)
                .setShowText(true)
                .setEmptyText("NO DATA")
                .setShowButton(false)
                .setShowIcon(true))
    }

    private fun iniBoxingLoader() {
        val loader = BoxingFrescoLoader(this)
        BoxingMediaLoader.getInstance().init(loader)
        BoxingCrop.getInstance().init(BoxingUcrop())
    }
}
