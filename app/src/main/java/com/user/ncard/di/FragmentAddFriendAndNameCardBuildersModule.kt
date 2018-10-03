package com.user.ncard.di

import com.user.ncard.ui.card.AddFriendFragment
import com.user.ncard.ui.card.SearchByNameFragment
import com.user.ncard.ui.card.SearchFromContactFragment
import com.user.ncard.ui.card.namecard.EditNameCardFragment
import com.user.ncard.ui.card.profile.ProfileFragment
import com.user.ncard.ui.card.profile.ProfileMoreFragment
import com.user.ncard.ui.card.profile.SetRemarkFragment
import com.user.ncard.ui.card.profile.UserJobFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentAddFriendAndNameCardBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeSearchByNameFragment(): SearchByNameFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileMoreFragment(): ProfileMoreFragment

    @ContributesAndroidInjector
    abstract fun contributeSetRemarkFragment(): SetRemarkFragment

    @ContributesAndroidInjector
    abstract fun contributeAddFriendFragment(): AddFriendFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFromContactFragment(): SearchFromContactFragment

    @ContributesAndroidInjector
    abstract fun contributeEditNameCardFragment(): EditNameCardFragment

    @ContributesAndroidInjector
    abstract fun contributeUserJobFragment(): UserJobFragment
}