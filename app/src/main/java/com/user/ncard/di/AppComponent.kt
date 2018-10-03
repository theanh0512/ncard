package com.user.ncard.di

import android.app.Application
import com.user.ncard.NCardApplication
import com.user.ncard.util.GCMListenerService
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Created by Pham on 27/7/17.
 */
@Singleton
@Component(modules = arrayOf(
        /* Use AndroidInjectionModule.class if you're not using support library */
        AndroidSupportInjectionModule::class, AppModule::class, LandingPageActivityModule::class,
        MainActivityModule::class, AddFriendAndNameCardActivityModule::class, FriendRequestActivityModule::class,
        FriendProfileActivityModule::class, FilterActivityModule::class, ChatActivityModule::class,
        CataloguePostActivityModule::class, NameCardMoreActivityModule::class,
        CatalogueTagPostActivityModule::class, EditNameCardActivityModule::class,
        CatalogueMainActivityModule::class, CatalogueSharePostActivityModule::class, CatalogueCategoryPostActivityModule::class,
        CatalogueDetailActivityModule::class, CatalogueMeActivityModule::class, GroupActivityModule::class,
        SelectGroupItemActivityModule::class, MyProfileActivityModule::class, FriendsListActivityModule::class,
        BroadcastGroupDetailActivityModule::class, BroadcastGroupListActivityModule::class, SelectItemActivityModule::class,
        FriendRecommendationActivityModule::class, EWalletActivityModule::class,
        BroadcastGroupDetailActivityModule::class, BroadcastGroupListActivityModule::class,
        SelectItemActivityModule::class, FriendRecommendationActivityModule::class,
        BroadcastChatActivityModule::class, ChatGroupDetailActivityModule::class, TopUpActivityModule::class,
        MyGiftActivityModule::class, TopUpActivityModule::class, SelectFriendActivityModule::class,
        ChatForwardListActivityModule::class, CashTransferActivityModule::class, ShippingAddressActivityModule::class,
        SettingActivityModule::class, ForgotLoginPasswordActivityModule::class,
        ChangePassActivityModule::class, AccountActivityModule::class, MyNameCardActivityModule::class, MyJobActivityModule::class))
interface AppComponent {
    fun inject(app: NCardApplication)

    fun inject(service: GCMListenerService)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}