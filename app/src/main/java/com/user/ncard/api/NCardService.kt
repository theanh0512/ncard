package com.user.ncard.api

import android.arch.lifecycle.LiveData
import com.user.ncard.ui.catalogue.*
import com.user.ncard.vo.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Pham on 27/7/17.
 */

interface NCardService {
    @GET("users/byUserName/{userName}")
    fun getUser(@Path("userName") userName: String): LiveData<ApiResponse<User>>

    @GET("users/{userId}/byUserName/{userName}")
    fun getUserSingle(@Path("userId") userId: Int, @Path("userName") userName: String): Single<SearchUserApiResponse>

    @POST("users/{userId}/byContact")
    fun getUsersFromContact(@Path("userId") userId: Int, @Body body: List<Contact>): LiveData<ApiResponse<List<SearchUserApiResponse>>>

    @GET("users/byUserName/{userName}")
    fun getUserWhenSignIn(@Path("userName") userName: String): Call<User>

    @GET("users/byId/{userId}")
    fun getUserById(@Path("userId") userId: Int): Flowable<User>

    @GET("users/{userId}/byId/{targetId}/mutual-friends")
    fun getMutualFriend(@Path("userId") userId: Int, @Path("targetId") targetId: Int): Flowable<List<Friend>>

    @PUT("users/byId/{userId}")
    fun updateUserInformation(@Path("userId") userId: Int, @Body body: ExtraSignUpInfo): Completable

    @PUT("users/byId/{userId}")
    fun updateMyInformation(@Path("userId") userId: Int, @Body user: User): Completable

    @PUT("users/byId/{userId}/chat")
    fun updateUserChatInfo(@Path("userId") userId: Int, @Body body: ChatInfo): Call<UpdateChatInfoResponse>

    @POST("users/{userId}/byFilters")
    fun searchUsers(@Path("userId") userId: Int, @Body body: UserFilter): LiveData<ApiResponse<List<SearchUserApiResponse>>>

    @POST("users/{userId}/by-jobs")
    fun searchUsersByProfession(@Path("userId") userId: Int, @Body body: ProfessionFiler): LiveData<ApiResponse<SearchUserByJobApiResponse>>

    //Friend
    @POST("users/{userId}/friendRequests")
    fun createFriendRequest(@Path("userId") userId: Int, @Body body: RequestFriend): Completable

    @PUT("users/{initiatorId}/friendRequests/{recipientId}")
    fun updateFriendRequest(@Path("initiatorId") initiatorId: Int, @Path("recipientId") userId: Int, @Body body: FriendRequestStatus): Completable

    @GET("users/{userId}/receivedFriendRequests")
    fun getAllFriendRequests(@Path("userId") userId: Int): Flowable<List<AllFriendRequest>>

    @GET("users/{userId}/friendRequests")
    fun getAllFriendRequestsSent(@Path("userId") userId: Int): Flowable<List<AllFriendRequest>>

    @GET("users/{userId}/friends")
    fun getAllFriends(@Path("userId") userId: Int): LiveData<ApiResponse<List<Friend>>>

    @GET("users/{userId}/friends")
    fun regetAllFriends(@Path("userId") userId: Int): Single<List<Friend>>

    @GET("users/{userId}/friendSharings")
    fun getAllFriendsShared(@Path("userId") userId: Int): Flowable<List<FriendRecommendation>>

    @PUT("users/{userId}/friendSharings/{friendSharingId}")
    fun updateFriendSharing(@Path("userId") userId: Int, @Path("friendSharingId") friendSharingId: Int, @Body body: FriendRequestStatus): Completable

    @PUT("users/{userId}/friends/{friendId}")
    fun updateFriendRemark(@Path("userId") userId: Int, @Path("friendId") friendId: Int, @Body body: FriendRemark): Completable

    @DELETE("users/{userId}/friends/{friendId}")
    fun deleteFriend(@Path("userId") userId: Int, @Path("friendId") friendId: Int): Completable

    @POST("users/{userId}/friendSharings")
    fun shareFriend(@Path("userId") userId: Int, @Body body: List<FriendSharing>): Completable

    @GET("catalogueTags")
    fun getCatalogueTags(): LiveData<ApiResponse<TagsResponse>>

    @POST("users/{userId}/cataloguePosts")
    fun createCataloguePost(@Path("userId") userId: Int, @Body body: RequestCatalogue): LiveData<ApiResponse<CataloguePost>>

    @GET("users/{userId}/cataloguePosts/me")
    fun getMyCataloguePosts(@Path("userId") userId: Int, @Query("postCategory") postCategory: String,
                            @Query("page") page: Int): LiveData<ApiResponse<CataloguePostResponse>>

    @GET("users/{userId}/cataloguePosts")
    fun getCataloguePosts(@Path("userId") userId: Int, @Query("filterTags") filterTags: String,
                          @Query("filterVisibility") filterVisibility: String,
                          @Query("postCategory") postCategory: String,
                          @Query("keyword") keyword: String,
                          @Query("page") page: Int): LiveData<ApiResponse<CataloguePostResponse>>

    @GET("users/{userId}/cataloguePosts/{postId}")
    fun getDetailCataloguePost(@Path("userId") userId: Int, @Path("postId") postId: Int): LiveData<ApiResponse<CataloguePost>>

    @DELETE("users/{userId}/cataloguePosts/{postId}")
    fun deleteCataloguePost(@Path("userId") userId: Int, @Path("postId") postId: Int): LiveData<ApiResponse<MessageObject>>

    @POST("users/{userId}/cataloguePosts/{postId}/comments")
    fun createCommentCataloguePost(@Path("userId") userId: Int, @Path("postId") postId: Int, @Body body: RequestComment): LiveData<ApiResponse<CataloguePost>>

    @POST("users/{userId}/cataloguePosts/{postId}/likes")
    fun likeCataloguePost(@Path("userId") userId: Int, @Path("postId") postId: Int, @Body body: RequestLike): LiveData<ApiResponse<CataloguePost>>

    //NameCard
    @POST("users/{userId}/otherNameCards")
    fun createNameCardForUser(@Path("userId") userId: Int, @Body body: NameCard): Completable

    @PUT("users/{userId}/otherNameCards/{otherNameCardId}")
    fun updateNameCardForUser(@Path("userId") userId: Int, @Path("otherNameCardId") otherNameCardId: Int, @Body body: NameCard): Completable

    @GET("users/{userId}/otherNameCards")
    fun getAllNameCards(@Path("userId") userId: Int): LiveData<ApiResponse<List<NameCard>>>

    @DELETE("users/{userId}/otherNameCards/{otherNameCardId}")
    fun deleteNameCard(@Path("userId") userId: Int, @Path("otherNameCardId") otherNameCardId: Int): Completable

    @Multipart
    @POST("BCRService/BCR_VCF2?PIN=&user=yuanfu@user.com.sg&pass=PE8WFQCMEGMGG8AN&json=1&lang=15")
    fun scanNameCard(@Query("size") size: Long, @Part file: MultipartBody.Part): Call<CamCardApiResponse>

    @POST("users/{userId}/nameCardSharings")
    fun shareNameCard(@Path("userId") userId: Int, @Body body: List<NameCardSharing>): Completable

    @GET("users/{userId}/nameCardSharings")
    fun getAllNameCardsShared(@Path("userId") userId: Int): Flowable<List<NameCardRecommendation>>

    @PUT("users/{userId}/nameCardSharings/{nameCardSharingId}")
    fun updateNameCardSharing(@Path("userId") userId: Int, @Path("nameCardSharingId") nameCardSharingId: Int, @Body body: FriendRequestStatus): Completable

    //Group
    @POST("users/{userId}/groups")
    fun createGroupForUser(@Path("userId") userId: Int, @Body body: RequestGroup): Single<Group>

    @PUT("users/{userId}/groups/{groupId}")
    fun updateGroupForUser(@Path("userId") userId: Int, @Path("groupId") groupId: Int, @Body body: RequestGroup): Completable

    @GET("users/{userId}/groups")
    fun getAllGroups(@Path("userId") userId: Int): LiveData<ApiResponse<List<Group>>>

    @DELETE("users/{userId}/groups/{groupId}")
    fun deleteGroup(@Path("userId") userId: Int, @Path("groupId") groupId: Int): Completable

    @POST("users/{userId}/subscribePushService")
    fun subscribeSnsService(@Path("userId") userId: Int, @Body body: NotificationInformation): Completable

    //Job
    @GET("users/{userId}/jobs")
    fun getAllJobs(@Path("userId") userId: Int): LiveData<ApiResponse<List<Job>>>

    @GET("users/{userId}/jobs")
    fun getAllJobsForUser(@Path("userId") userId: Int): Flowable<List<Job>>

    @POST("users/{userId}/jobs")
    fun createJobForUser(@Path("userId") userId: Int, @Body body: Job): Completable

    @PUT("users/{userId}/jobs/{jobId}")
    fun updateJobForUser(@Path("userId") userId: Int, @Path("jobId") jobId: Int, @Body body: Job): Completable

    @DELETE("users/{userId}/jobs/{jobId}")
    fun deleteJob(@Path("userId") userId: Int, @Path("jobId") jobId: Int): Completable

    //NameCard for job
    @GET("users/{userId}/jobs/allNameCards")
    fun getAllMyNameCard(@Path("userId") userId: Int): LiveData<ApiResponse<List<NameCard>>>

    @GET("users/{userId}/jobs/allNameCards")
    fun getAllNameCardFromUser(@Path("userId") userId: Int): Flowable<List<NameCard>>

    @POST("users/{userId}/jobs/{jobId}/nameCard")
    fun createNameCardForAJob(@Path("userId") userId: Int, @Path("jobId") jobId: Int, @Body body: NameCard): Completable

    @GET("users/{userId}/jobs/{jobId}/nameCard")
    fun getNameCardForAJob(@Path("userId") userId: Int, @Path("jobId") jobId: Int): LiveData<ApiResponse<NameCard>>

    @PUT("users/{userId}/jobs/{jobId}/nameCard")
    fun updateNameCardForAJob(@Path("userId") userId: Int, @Path("jobId") jobId: Int, @Body body: NameCard): Completable

    @DELETE("users/{userId}/jobs/{jobId}/nameCard")
    fun deleteNameCardForAJob(@Path("userId") userId: Int, @Path("jobId") jobId: Int): Completable

    @POST("chats/broadcast-groups")
    fun createBroadcastGroup(@Body body: BroadcastGroup): LiveData<ApiResponse<BroadcastGroup>>

    @GET("chats/broadcast-groups")
    fun getAllBroadcastGroups(): LiveData<ApiResponse<List<BroadcastGroup>>>

    @PUT("chats/broadcast-groups/{groupId}")
    fun updateBroadcastGroup(@Path("groupId") groupId: Int, @Body body: BroadcastGroup): LiveData<ApiResponse<MessageObject>>

    @GET("chats/broadcast-groups/{groupId}")
    fun getBroadcastGroupDetail(@Path("groupId") groupId: Int): LiveData<ApiResponse<BroadcastGroup>>

    @DELETE("chats/broadcast-groups/{groupId}")
    fun deleteBroadcastGroup(@Path("groupId") groupId: Int): LiveData<ApiResponse<MessageObject>>

    //EWallet
    @GET("wallets/me")
    fun getWalletInfo(): Flowable<WalletInfo>

    @GET("stripe/ephemeral-keys")
    fun getEphemeralKey(): Flowable<ResponseBody>

    @POST("wallets/create-password")
    fun createWalletPassword(@Body walletPassword: WalletPassword): Completable

    @POST("wallets/change-password")
    fun changeWalletPassword(@Body walletPassword: ChangePassword): Completable

    @POST("wallets/forgot-password")
    fun forgetWalletPassword(@Body forgetPassword: ForgetPassword): Single<ForgetPasswordResponse>

    @POST("transactions/withdraw")
    fun withdrawToBank(@Body body: WithdrawRequest): Completable

    //Transaction
    @POST("transactions/deposit")
    fun postTopUpTransaction(@Body body: DepositTransaction): Single<TransactionResponse>

    @GET("users")
    fun getFriendsByIds(@Query("include") include: String): LiveData<ApiResponse<List<Friend>>>

    @GET("users")
    fun getFriendsByChatIds(@Query("chatIds") include: String): LiveData<ApiResponse<List<Friend>>>

    @POST("transactions/transfer")
    fun postTransfer(@Body body: RequestTransfer): Single<TransferCreditResponse>

    //Transaction Log
    @GET("transaction-logs")
    fun getTransactionLogs(@Query("page") page: Int): Flowable<TransactionLogResponse>

    @GET("transaction-logs/{transactionId}")
    fun getTransactionLogDetail(@Path("transactionId") transactionId: Int): Flowable<TransactionLogDetail>

    //Gift
    @GET("shops/gifts")
    fun getShopItem(): Flowable<List<GiftItem>>

    @GET("purchased-items")
    fun getMyGifts(): Flowable<MyGiftResponse>

    @POST("orders")
    fun createOrder(@Body body: GiftOrderBody): Flowable<CreateOrderResponse>

    @POST("gifts/{giftId}/send")
    fun sendGift(@Path("giftId") giftId: Int, @Body body: SendGiftRequest): Single<SendGiftResponse>

    @GET("transactions/{transactionId}")
    fun getCreditTransactionDetail(@Path("transactionId") transactionId: Int): LiveData<ApiResponse<TransferCreditResponse>>

    @PUT("transactions/{transactionId}")
    fun updateCreditTransaction(@Path("transactionId") transactionId: Int, @Body body: RequestUpdateCreditTransaction): LiveData<ApiResponse<TransferCreditResponse>>

    @GET("users/{userId}/gifts/{giftId}")
    fun getGiftDetail(@Path("userId") userId: Int, @Path("giftId") giftId: Int): LiveData<ApiResponse<SendGiftResponse>>

    @PUT("gifts/{giftId}")
    fun updateGift(@Path("giftId") giftId: Int, @Body body: RequestUpdateGift): LiveData<ApiResponse<SendGiftResponse>>

    @GET("wallets/settings")
    fun getEwalletSettings(): Single<EwalletSetting>
}
