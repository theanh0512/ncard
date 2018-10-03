package com.user.ncard.di

import com.user.ncard.ui.me.ewallet.ForgetPasswordCodeFragment
import com.user.ncard.ui.me.ewallet.ForgetPasswordFragment
import com.user.ncard.ui.me.ewallet.SetPaymentPasswordFragment
import com.user.ncard.ui.me.gift.MyGiftFragment
import com.user.ncard.ui.me.gift.ProductDetailFragment
import com.user.ncard.ui.me.gift.SendGiftFragment
import com.user.ncard.ui.me.gift.ShopFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentMyGiftBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFragment(): MyGiftFragment

    @ContributesAndroidInjector
    abstract fun contributeShopFragment(): ShopFragment

    @ContributesAndroidInjector
    abstract fun contributeGiftFragment(): SendGiftFragment

    @ContributesAndroidInjector
    abstract fun contributeProductDetailFragment(): ProductDetailFragment

    @ContributesAndroidInjector
    abstract fun contributeForgetPasswordFragment(): ForgetPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeForgetPasswordCodeFragment(): ForgetPasswordCodeFragment

    @ContributesAndroidInjector
    abstract fun contributeSetPaymentPasswordFragment(): SetPaymentPasswordFragment
}