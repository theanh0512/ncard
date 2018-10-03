package com.user.ncard.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.user.ncard.ui.card.AddFriendViewModel
import com.user.ncard.ui.card.CardsViewModel
import com.user.ncard.ui.card.SearchByNameViewModel
import com.user.ncard.ui.card.SearchFromContactViewModel
import com.user.ncard.ui.card.namecard.EditNameCardViewModel
import com.user.ncard.ui.card.namecard.NameCardMoreViewModel
import com.user.ncard.ui.card.namecard.SetRemarkNameCardViewModel
import com.user.ncard.ui.card.profile.ProfileMoreViewModel
import com.user.ncard.ui.card.profile.ProfileViewModel
import com.user.ncard.ui.card.profile.SetRemarkViewModel
import com.user.ncard.ui.catalogue.category.CategoryPostViewModel
import com.user.ncard.ui.catalogue.detail.CatalogueDetailViewModel
import com.user.ncard.ui.catalogue.main.CatalogueMainViewModel
import com.user.ncard.ui.catalogue.my.CatalogueMeViewModel
import com.user.ncard.ui.catalogue.post.CataloguePostViewModel
import com.user.ncard.ui.catalogue.share.SharePostViewModel
import com.user.ncard.ui.catalogue.tag.TagPostViewModel
import com.user.ncard.ui.chats.broadcast.BroadcastGroupListViewModel
import com.user.ncard.ui.chats.broadcastchat.BroadcastChatViewModel
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailViewModel
import com.user.ncard.ui.chats.detail.ChatViewModel
import com.user.ncard.ui.chats.dialogs.ChatListViewModel
import com.user.ncard.ui.chats.forward.ForwardListViewModel
import com.user.ncard.ui.chats.friends.FriendsListViewModel
import com.user.ncard.ui.chats.groups.ChatGroupDetailViewModel
import com.user.ncard.ui.chats.shipping.ShippingAddressViewModel
import com.user.ncard.ui.discovery.DiscoveryViewModel
import com.user.ncard.ui.discovery.FriendRecommendationViewModel
import com.user.ncard.ui.discovery.FriendRequestViewModel
import com.user.ncard.ui.filter.FilterViewModel
import com.user.ncard.ui.group.CreateGroupViewModel
import com.user.ncard.ui.group.GroupViewModel
import com.user.ncard.ui.group.SelectGroupItemViewModel
import com.user.ncard.ui.landing.confirmaccount.ConfirmAccountViewModel
import com.user.ncard.ui.landing.createaccount.CreateAccountViewModel
import com.user.ncard.ui.landing.inputinfo.InputInfoViewModel
import com.user.ncard.ui.landing.landing.LandingPageViewModel
import com.user.ncard.ui.landing.signin.SignInViewModel
import com.user.ncard.ui.me.*
import com.user.ncard.ui.me.ewallet.EWalletViewModel
import com.user.ncard.ui.me.gift.GiftViewModel
import com.user.ncard.viewmodel.NCardViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(LandingPageViewModel::class)
    internal abstract fun bindLandingPageViewModel(landingPageViewModel: LandingPageViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateAccountViewModel::class)
    internal abstract fun bindCreateAccountViewModel(createAccountViewModel: CreateAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConfirmAccountViewModel::class)
    internal abstract fun bindConfirmAccountViewModel(confirmAccountViewModel: ConfirmAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InputInfoViewModel::class)
    internal abstract fun bindInputInfoViewModel(inputInfoViewModel: InputInfoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    internal abstract fun bindSignInViewModel(signInViewModel: SignInViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeViewModel::class)
    internal abstract fun bindMeViewModel(meViewModel: MeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoveryViewModel::class)
    internal abstract fun bindDiscoveryViewModel(discoveryViewModel: DiscoveryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatListViewModel::class)
    internal abstract fun bindChatsViewModel(chatListViewModel: ChatListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CardsViewModel::class)
    internal abstract fun bindCardsViewModel(cardsViewModel: CardsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchByNameViewModel::class)
    internal abstract fun bindSearchByNameViewModel(searchByNameViewModel: SearchByNameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    internal abstract fun bindProfileViewModel(profileViewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FriendRequestViewModel::class)
    internal abstract fun bindFriendRequestViewModel(friendRequestViewModel: FriendRequestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetRemarkViewModel::class)
    internal abstract fun bindSetRemarkViewModel(setRemarkViewModel: SetRemarkViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileMoreViewModel::class)
    internal abstract fun bindProfileMoreViewModel(profileMoreViewModel: ProfileMoreViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FilterViewModel::class)
    internal abstract fun bindFilterViewModel(filterViewModel: FilterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddFriendViewModel::class)
    internal abstract fun bindAddFriendViewModel(addFriendViewModel: AddFriendViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchFromContactViewModel::class)
    internal abstract fun bindSearchFromContactViewModel(searchFromContactViewModel: SearchFromContactViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditNameCardViewModel::class)
    internal abstract fun bindEditNameCardViewModel(editNameCardViewModel: EditNameCardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NameCardMoreViewModel::class)
    internal abstract fun bindNameCardMoreViewModel(nameCardMoreViewModel: NameCardMoreViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetRemarkNameCardViewModel::class)
    internal abstract fun bindSetRemarkNameCardViewModel(viewModel: SetRemarkNameCardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GroupViewModel::class)
    internal abstract fun bindGroupViewModel(groupViewModel: GroupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateGroupViewModel::class)
    internal abstract fun bindCreateGroupViewModel(createGroupViewModel: CreateGroupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelectGroupItemViewModel::class)
    internal abstract fun bindSelectGroupItemViewModel(selectGroupItemViewModel: SelectGroupItemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyProfileViewModel::class)
    internal abstract fun bindMyProfileViewModel(myProfileViewModel: MyProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditMyProfileViewModel::class)
    internal abstract fun bindEditMyProfileViewModel(editMyProfileViewModel: EditMyProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyJobViewModel::class)
    internal abstract fun bindMyJobViewModel(myJobViewModel: MyJobViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyNameCardViewModel::class)
    internal abstract fun bindMyNameCardViewModel(myNameCardViewModel: MyNameCardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    internal abstract fun bindChatViewModel(viewModel: ChatViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: NCardViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(CataloguePostViewModel::class)
    internal abstract fun bindCataloguePostViewModel(viewModel: CataloguePostViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CatalogueMainViewModel::class)
    internal abstract fun bindCatalogueMainViewModel(viewModel: CatalogueMainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TagPostViewModel::class)
    internal abstract fun bindTagPostViewModel(viewModel: TagPostViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SharePostViewModel::class)
    internal abstract fun bindSharePostViewModel(viewModel: SharePostViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CategoryPostViewModel::class)
    internal abstract fun bindCategoryPostViewModel(viewModel: CategoryPostViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CatalogueMeViewModel::class)
    internal abstract fun bindCatalogueMeViewModel(viewModel: CatalogueMeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CatalogueDetailViewModel::class)
    internal abstract fun bindCatalogueDetailViewModel(viewModel: CatalogueDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FriendsListViewModel::class)
    internal abstract fun bindFriendsListViewModel(viewModel: FriendsListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BroadcastGroupListViewModel::class)
    internal abstract fun bindBroadcastGroupListViewModel(viewModel: BroadcastGroupListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BroadcastGroupDetailViewModel::class)
    internal abstract fun bindBroadcastGroupDetailViewModel(viewModel: BroadcastGroupDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FriendRecommendationViewModel::class)
    internal abstract fun bindFriendRecommendationViewModel(viewModel: FriendRecommendationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EWalletViewModel::class)
    internal abstract fun bindEWalletViewModel(viewModel: EWalletViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GiftViewModel::class)
    internal abstract fun bindGiftViewModel(viewModel: GiftViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BroadcastChatViewModel::class)
    internal abstract fun bindBroadcastChatViewModel(viewModel: BroadcastChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatGroupDetailViewModel::class)
    internal abstract fun bindChatGroupDetailViewModel(viewModel: ChatGroupDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ForwardListViewModel::class)
    internal abstract fun bindForwardListViewModel(viewModel: ForwardListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ShippingAddressViewModel::class)
    internal abstract fun bindShippingAddressViewModel(viewModel: ShippingAddressViewModel): ViewModel
}
