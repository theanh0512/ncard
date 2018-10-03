package com.user.ncard

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.LiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.MutableBoolean
import android.view.*
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.quickblox.chat.QBIncomingMessagesManager
import com.quickblox.chat.QBPrivacyListsManager
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.listeners.QBPrivacyListListener
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBPrivacyList
import com.quickblox.chat.model.QBPrivacyListItem
import com.quickblox.users.model.QBUser
import com.tbruyelle.rxpermissions2.RxPermissions
import com.user.ncard.api.CamCardApiResponse
import com.user.ncard.api.NCardService
import com.user.ncard.api.ScanCardService
import com.user.ncard.api.UpdateChatInfoResponse
import com.user.ncard.db.NCardInfoDao
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.card.CardsFragment
import com.user.ncard.ui.card.namecard.EditNameCardActivity
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.GroupDialogEvent
import com.user.ncard.ui.chats.PrivacyListEvent
import com.user.ncard.ui.chats.SendGiftEvent
import com.user.ncard.ui.chats.TransferCreditEvent
import com.user.ncard.ui.chats.dialogs.ChatListFragment
import com.user.ncard.ui.chats.utils.DialogManager
import com.user.ncard.ui.chats.utils.Snippet
import com.user.ncard.ui.chats.utils.SnippetAsync
import com.user.ncard.ui.discovery.DiscoveryFragment
import com.user.ncard.ui.discovery.RequestRecommendationUpdateEvent
import com.user.ncard.ui.me.MeFragment
import com.user.ncard.util.*
import com.user.ncard.vo.*
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {

    var TAG: String = "MainActivity"
    val meFragment = MeFragment()
    val chatListFragment = ChatListFragment()
    val discoveryFragment = DiscoveryFragment()
    val cardsFragment = CardsFragment()
    var file: File? = null
    var uri: Uri? = null
    lateinit var scanProcessingAlert: AlertDialog
    lateinit var scanAlert: AlertDialog
    val scanClicked = MutableBoolean(false)

    var isReceiverRegistered = false
    lateinit var mRegistrationBroadcastReceiver: BroadcastReceiver

    @Inject
    lateinit var nCardService: NCardService

    @Inject
    lateinit var scanCardService: ScanCardService

    @Inject
    lateinit var changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var chatHelper: ChatHelper

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == TOKEN_FETCHED) {
                subscribeSNS(intent.getStringExtra("token"))
            } else if (intent.action == FRIEND_REQUEST) {
                val currentDiscoveryBadge = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_DISCOVERY_BADGE, 0)
                /*val discoveryTab = bottom_bar.getTabWithId(R.id.tab_discovery)
                discoveryTab.setBadgeCount(currentDiscoveryBadge + 1)*/
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_DISCOVERY_BADGE, currentDiscoveryBadge + 1)

                val currentFriendRequestBadge = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_FRIEND_REQUEST_BADGE, 0)
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_FRIEND_REQUEST_BADGE, currentFriendRequestBadge + 1)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.e("NCard", intent.toString())
            }
        }

        registerReceiver()
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            val intent = Intent(this, RegistrationIntentService::class.java)
            startService(intent)
        }

        val currentDiscoveryBadge = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_DISCOVERY_BADGE, 0)
        /*val discoveryTab = bottom_bar.getTabWithId(R.id.tab_discovery)
        discoveryTab.setBadgeCount(currentDiscoveryBadge)*/

        // https://github.com/roughike/BottomBar/issues/570
        bottom_bar.setBadgeBackgroundColor(resources.getColor(R.color.colorWhite))
        bottom_bar.setBadgesHideWhenActive(false)
        bottom_bar.setOnTabSelectListener { tabId ->
            when (tabId) {
                R.id.tab_card -> selectFragment(cardsFragment)
                R.id.tab_chat -> selectFragment(chatListFragment)
                R.id.tab_discovery -> {
                    selectFragment(discoveryFragment)
                    /*discoveryTab.removeBadge()*/
                    sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_DISCOVERY_BADGE, 0)
                }
                R.id.tab_me -> selectFragment(meFragment)
                R.id.tab_scan -> {
                    scanClicked.value = true
                    showDialogMessage()
                }
                else -> {
                }
            }
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        getFriendRecommendationAndRequest()
        initFriendRecommendationAndRequest()
    }

    override fun onStart() {
        super.onStart()
        if (scanClicked.value) {
            bottom_bar.selectTabAtPosition(0)
            scanClicked.value = false
        }
    }

    private fun subscribeSNS(token: String) {
        changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
        nCardService.subscribeSnsService(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID),
                NotificationInformation(if (BuildConfig.DEBUG && Constants.DEV) "sandbox" else "distribution", "android", "hwawei", token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.e(TAG, "subscribe sns success")
                        },
                        { error -> Log.e(TAG, "Unable to subscribe", error) })
    }

    override fun onPause() {
        unregisterReceiver(broadcastReceiver)

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver)
        isReceiverRegistered = false
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterChatListener()
        EventBus.getDefault().unregister(this)
    }

    fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish()
            }
            return false
        }
        return true
    }

    fun registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE))
            isReceiverRegistered = true
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(TOKEN_FETCHED)
        filter.addAction(FRIEND_REQUEST)
        registerReceiver(broadcastReceiver, filter)

        initChatConfiguration()
        registerReceiver()
    }

    private fun initChatConfiguration() {
        var signInChatSuccess = sharedPreferenceHelper.getBoolean(
                SharedPreferenceHelper.Key.IS_SIGN_UP_SIGN_IN_CHAT_SUCCESS, false);
        var updateChatIdSuccess = sharedPreferenceHelper.getBoolean(
                SharedPreferenceHelper.Key.IS_UPDATE_CHAT_ID_SUCCESS, false);

        val shouldFetchUser = !signInChatSuccess || !updateChatIdSuccess
        if (shouldFetchUser) {
            //val signInChatAlert = Utils.showAlert(this@MainActivity, getString(R.string.signing_in_chat))
            fetchUserInfo(object : FetchUserInfoListener {
                override fun onFetchUserSuccess(user: User) {
                    if (!signInChatSuccess) {
                        chatHelper.signUpAndSignIn(user, sharedPreferenceHelper, object : ChatHelper.SignUpListener {

                            override fun onSignUpSuccess(qbUser: QBUser) {
                                //update chat info for the first time
                                updateChatInfo(user, qbUser.id, sharedPreferenceHelper)
                            }

                            override fun onAlreadySignedUp(qbUser: QBUser) {
                                if (!updateChatIdSuccess) { // check and update in case it failed to update
                                    updateChatInfo(user, qbUser.id, sharedPreferenceHelper);
                                }
                            }

                            override fun onSignUpError() {
                                //signInChatAlert.dismiss()
                                Toast.makeText(this@MainActivity, R.string.sign_up_chat_fail, Toast.LENGTH_LONG).show()
                            }

                        }, object : ChatHelper.SignInListener {
                            override fun onSignInSuccess(qbUser: QBUser) {
                                //signInChatAlert.dismiss()
                                //connect
                                chatHelper.connect(sharedPreferenceHelper, connectionListener)
                            }

                            override fun onSignInError() {
                                //signInChatAlert.dismiss()
                                Toast.makeText(this@MainActivity, R.string.sign_in_chat_fail, Toast.LENGTH_LONG).show()
                            }
                        })
                    } else if (!updateChatIdSuccess) {
                        if (user.email != null) {
                            chatHelper.fetchQbUser(user.email, object : ChatHelper.FetchQbUserListener {
                                override fun onFetchSuccess(qbUser: QBUser) {
                                    //signInChatAlert.dismiss()
                                    updateChatInfo(user, qbUser.id, sharedPreferenceHelper);
                                    //connect
                                    chatHelper.connect(sharedPreferenceHelper, connectionListener)
                                }

                                override fun onFetchError() {
                                    //signInChatAlert.dismiss()
                                }
                            })
                        }
                    }
                }

                override fun onFetchUserError() {
                    //signInChatAlert.dismiss()
                }
            })
        } else {
            //connect
            val userEmail = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL)
            chatHelper.connect(sharedPreferenceHelper, connectionListener)
        }
    }

    interface FetchUserInfoListener {
        fun onFetchUserSuccess(user: User)
        fun onFetchUserError()
    }

    private fun fetchUserInfo(listener: FetchUserInfoListener) {
        val username = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USERNAME)
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)

        Utils.Log(TAG, "-> nCardService.getUserWhenSignIn")
        nCardService.getUserWhenSignIn(username).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                Utils.Log(TAG, "<- nCardService.getUserWhenSignIn onFailure")
                listener.onFetchUserError()
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                Utils.Log(TAG, "<- nCardService.getUserWhenSignIn onResponse")
                val user = response?.body()
                if (user == null) {
                    listener.onFetchUserError()
                } else {
                    //save user info
                    sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_USER_FIRST_NAME, user.firstName)
                    sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_USER_LAST_NAME, user.lastName)
                    listener.onFetchUserSuccess(user)
                }
            }
        })
    }

    fun updateChatInfo(user: User, chatId: Int, sharedPreferenceHelper: SharedPreferenceHelper) {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        if (user.id != null) {
            val chatInfo = ChatInfo(chatId)
            Utils.Log(TAG, "-> nCardService.updateUserChatInfo - userId:" + user.id + " chatId:" + chatId)
            nCardService.updateUserChatInfo(user.id, chatInfo).enqueue(object : Callback<UpdateChatInfoResponse> {
                override fun onFailure(call: Call<UpdateChatInfoResponse>?, t: Throwable?) {
                    Utils.Log(TAG, "<- nCardService.updateUserChatInfo onFailure")
                }

                override fun onResponse(call: Call<UpdateChatInfoResponse>?, response: Response<UpdateChatInfoResponse>?) {
                    Utils.Log(TAG, "<- nCardService.updateUserChatInfo onResponse")
                    if (response != null && response.isSuccessful) {
                        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.IS_UPDATE_CHAT_ID_SUCCESS, true);
                    }
                }
            })
        }
    }

    private fun showDialogMessage() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val v: View = inflater.inflate(R.layout.dialog_scan_name_card, null)
        builder.setView(v)
        builder.setCancelable(false)
        val takePhotoButton = v.findViewById<Button>(R.id.buttonTakePhoto)
        takePhotoButton.setOnClickListener {
            scanAlert.cancel()
            takePicture()
        }
        val chooseFromGalleryButton = v.findViewById<Button>(R.id.buttonChooseFromGallery)
        chooseFromGalleryButton.setOnClickListener {
            scanAlert.cancel()
            chooseFromGallery()
        }
        val cancelButton = v.findViewById<Button>(R.id.buttonCancel)
        cancelButton.setOnClickListener {
            scanAlert.cancel()
            bottom_bar.selectTabAtPosition(0)
        }
        scanAlert = builder.create()
        val window: Window = scanAlert.window
        val attributes = window.attributes
        attributes.gravity = Gravity.BOTTOM
        attributes.y = 124
        attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = attributes
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        scanAlert.show()

    }

    private fun chooseFromGallery() {
        val rxPermissions = RxPermissions(this)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object : io.reactivex.Observer<Boolean> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: Boolean) {
                if (t) {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE)
                } else {
                    Toast.makeText(this@MainActivity, R.string.permission_request_denied, Toast.LENGTH_LONG).show()
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }

    private fun takePicture() {
        val rxPermissions = RxPermissions(this)
        rxPermissions.request(Manifest.permission.CAMERA).subscribe(object : io.reactivex.Observer<Boolean> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: Boolean) {
                if (t) {
                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePicture.resolveActivity(packageManager) != null) {
                        try {
                            file = createImageFile()
                        } catch (ex: IOException) {
                            // Error occurred while creating the File
                        }
                        if (file != null) {
                            uri = FileProvider.getUriForFile(this@MainActivity, applicationContext.packageName + ".file.provider", file)
                            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                            startActivityForResult(takePicture, REQUEST_TAKE_PICTURE)
                        }
                    }

                } else {
                    Toast.makeText(this@MainActivity, R.string.permission_request_denied, Toast.LENGTH_LONG).show()
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm").format(Date());
        val imageFileName = "JPEG_" + timeStamp + "_";
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        val image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        )
        return image
    }


    private fun selectFragment(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        val fragmentByTag = supportFragmentManager.findFragmentByTag(fragment.javaClass.simpleName)
        if (fragmentByTag != null) {
            ft.replace(R.id.content, fragmentByTag, fragmentByTag.javaClass.simpleName)
        } else {
            ft.replace(R.id.content, fragment, fragment.javaClass.simpleName)
        }
        ft.commit()
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PICTURE) {

                val requestFile = RequestBody.create(
                        MediaType.parse(contentResolver.getType(uri)), file
                )
                val body = MultipartBody.Part.createFormData("picture", file?.name, requestFile)
                scanProcessingAlert = Utils.showAlert(this)
                postToScanCard(body, file)
            }
            if (requestCode == REQUEST_CHOOSE_IMAGE) {
                val imageUri = data?.data
                val fileFromUri = FileUtils.getFile(this, imageUri)
                val requestFile = RequestBody.create(
                        MediaType.parse(contentResolver.getType(imageUri)), fileFromUri
                )
                val body = MultipartBody.Part.createFormData("picture", fileFromUri?.name, requestFile)
                scanProcessingAlert = Utils.showAlert(this)
                postToScanCard(body, fileFromUri)
            }
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    private fun postToScanCard(body: MultipartBody.Part, fileToPost: File?) {
        //changeableBaseUrlInterceptor.setInterceptor(Constants.SCAN_NAME_CARD_URL)
        scanCardService.scanNameCard(fileToPost?.length() ?: 0, body).enqueue(object : Callback<CamCardApiResponse> {
            override fun onFailure(call: Call<CamCardApiResponse>?, t: Throwable?) {
                Log.e("NCard", "Cannot upload")
            }

            override fun onResponse(call: Call<CamCardApiResponse>?, response: Response<CamCardApiResponse>?) {
                scanProcessingAlert.cancel()
                bottom_bar.selectTabAtPosition(0)
                val camCardApiResponse = response?.body()
                var company = ""
                camCardApiResponse?.organizations?.forEach {
                    company += " " + it.item.name
                }
                var mobile = ""
                var tel1 = ""
                var tel2 = ""
                when (camCardApiResponse?.telephones?.size) {
                    1 -> mobile = camCardApiResponse.telephones[0].item.number
                    2 -> {
                        mobile = camCardApiResponse.telephones[0].item.number
                        tel1 = camCardApiResponse.telephones[1].item.number
                    }
                    3 -> {
                        mobile = camCardApiResponse.telephones[0].item.number
                        tel1 = camCardApiResponse.telephones[1].item.number
                        tel2 = camCardApiResponse.telephones[2].item.number
                    }
                }
                val nameCard = NameCard(1, camCardApiResponse?.names?.get(0)?.item, "", camCardApiResponse?.titles?.get(0)?.item, camCardApiResponse?.emails?.get(0)?.item, company,
                        mobile, camCardApiResponse?.labels?.get(0)?.item?.address, null, "", "", "", Utils.provideBackgroundUrl(0), "",
                        "", "", "", "", "", null, null, tel1, tel2, "", "",
                        "", "", "", false, "", "", 0, 0, "")
                val intent = Intent(this@MainActivity, EditNameCardActivity::class.java)
                intent.putExtra("front", fileToPost?.toURI())
                intent.putExtra("namecard", nameCard)
                startActivity(intent)
            }
        })
    }

    companion object {
        private const val REQUEST_TAKE_PICTURE = 120
        private const val REQUEST_CHOOSE_IMAGE = 121
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        private val TAG = "NCard"
        const val TOKEN_FETCHED = "com.user.ncard.token_fetched"
        const val FRIEND_REQUEST = "com.user.ncard.friend_request"
    }

     /*Friend Request, recommendation */
    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onRequestRecommendationUpdateEvent(requestRecommendationUpdateEvent: RequestRecommendationUpdateEvent) {
         initFriendRecommendationAndRequest()
    }

    var nCardInfoList: LiveData<List<NCardInfo>>? = null
    @Inject lateinit var nCardInfoDao: NCardInfoDao

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var nameCardRepository: NameCardRepository

    fun regetFriendRequest() {
        Log.e("NCard", "regetFriendRequest")
        userRepository.getAllFriendRequests()
    }

    fun regetFriendRecommendation() {
        Log.e("NCard", "regetFriendRecommendation")
        userRepository.getAllFriendShared()
    }

    fun regetCardRecommendation() {
        Log.e("NCard", "regetCardRecommendation")
        nameCardRepository.getAllNameCardShared()
    }

    fun getFriendRecommendationAndRequest() {
        // Need to get friend request and recommendation for each time opening app
        regetFriendRequest()
        regetFriendRecommendation()
        regetCardRecommendation()
    }

    fun initFriendRecommendationAndRequest() {
        Log.e("NCard", "initFriendRecommendationAndRequest")
        nCardInfoList = nCardInfoDao.getNCardInfo()
        nCardInfoList?.observe(this, android.arch.lifecycle.Observer {
            Log.e("NCard", "update initFriendRecommendationAndRequest ")
            if (it != null && it?.isNotEmpty()) {
                val discoveryTab = bottom_bar.getTabWithId(R.id.tab_discovery)
                val count = it?.get(0).friendRequest!! + it?.get(0)?.friendRecommendation!! + it?.get(0)?.cardRecommendation!!
                Log.e("NCard", "update initFriendRecommendationAndRequest " + it?.size + " "  + it?.get(0).friendRequest!! + it?.get(0)?.friendRecommendation!! + it?.get(0)?.cardRecommendation!!)
                if (count > 0) {
                    discoveryTab?.setBadgeCount(count)
                } else {
                    discoveryTab?.removeBadge()
                }
            } else {
                val discoveryTab = bottom_bar.getTabWithId(R.id.tab_discovery)
                discoveryTab?.removeBadge()
            }
        })
    }
    /*Friend Request, recommendation */

    // Chat Listener
    var allDialogsMessagesListener: QBChatDialogMessageListener? = null
    var incomingMessagesManager: QBIncomingMessagesManager? = null
    var systemMessagesListener: SystemMessagesListener? = null
    var systemMessagesManager: QBSystemMessagesManager? = null
    var privacyListListener: QBPrivacyListListener? = null
    var privacyListManager: QBPrivacyListsManager? = null
    var listPrivacy: List<QBPrivacyList>? = null
    var listUnreadChatDialog: LiveData<List<ChatDialog>>? = null
    @Inject lateinit var chatRepository: ChatRepository

    val connectionListener = object : ChatHelper.ChatConnectListener {

        override fun onConnectLoginSuccess() {
            initChatListener()
        }

        override fun onConnectLoginError() {
        }

    }

    fun initChatListener() {
        if (allDialogsMessagesListener == null) {
            allDialogsMessagesListener = AllDialogsMessageListener()
        }
        if (systemMessagesListener == null) {
            systemMessagesListener = SystemMessagesListener()
        }
        if (privacyListListener == null) {
            privacyListListener = PrivacyListListener()
        }
        if (incomingMessagesManager == null) {
            incomingMessagesManager = chatHelper.getCurrentChatService().incomingMessagesManager
        }
        if (systemMessagesManager == null) {
            systemMessagesManager = chatHelper.getCurrentChatService().systemMessagesManager
        }
        if (privacyListManager == null) {
            privacyListManager = chatHelper.getCurrentChatService().privacyListsManager
        }

        registerChatListener()
        getPrivacyList()

        // Get unread chat dialog
        listUnreadChatDialog = chatRepository.getUnreadChatDialog()
        listUnreadChatDialog?.observe(this, android.arch.lifecycle.Observer {
            if (it != null && it?.isNotEmpty()) {
                val chatTab = bottom_bar.getTabWithId(R.id.tab_chat)
                chatTab?.setBadgeCount(it?.size)
            } else {
                val chatTab = bottom_bar.getTabWithId(R.id.tab_chat)
                chatTab?.removeBadge()
            }
        })
    }

    fun getPrivacyList() {
        if (listPrivacy == null) {
            // Get Privacy List
            var getPrivacyLists: Snippet = object : SnippetAsync("", this) {
                override fun executeAsync() {

                    try {
                        listPrivacy = privacyListManager?.privacyLists
                    } catch (e: SmackException.NotConnectedException) {
                        e.printStackTrace()
                    } catch (e: XMPPException.XMPPErrorException) {
                        e.printStackTrace()
                    } catch (e: SmackException.NoResponseException) {
                        e.printStackTrace()
                    }

                    if (listPrivacy != null) {
                        Functions.showLogMessage("QBPrivacyList", listPrivacy.toString())
                        //Functions.showToastShortMessage(this@MainActivity, "Privacy List: " + listPrivacy.toString())
                    }
                }
            }
            getPrivacyLists.performExecution()
        }
    }

    fun registerChatListener() {
        if (incomingMessagesManager?.dialogMessageListeners == null || incomingMessagesManager?.dialogMessageListeners?.isEmpty()!!) {
            incomingMessagesManager?.addDialogMessageListener(if (allDialogsMessagesListener != null)
                allDialogsMessagesListener else AllDialogsMessageListener())
        }

        if (systemMessagesManager?.systemMessageListeners == null || systemMessagesManager?.systemMessageListeners?.isEmpty()!!) {
            systemMessagesManager?.addSystemMessageListener(if (systemMessagesListener != null)
                systemMessagesListener else SystemMessagesListener())
        }

        if (privacyListManager?.privacyListListeners == null || privacyListManager?.privacyListListeners?.isEmpty()!!) {
            privacyListManager?.addPrivacyListListener(if (privacyListListener != null)
                privacyListListener else PrivacyListListener())
        }
    }

    fun unregisterChatListener() {
        incomingMessagesManager?.removeDialogMessageListrener(allDialogsMessagesListener)
        systemMessagesManager?.removeSystemMessageListener(systemMessagesListener)
        privacyListManager?.removePrivacyListListener(privacyListListener)
    }

    inner class AllDialogsMessageListener : QBChatDialogMessageListener {
        override fun processError(s: String, e: QBChatException, qbChatMessage: QBChatMessage, integer: Int?) {
            e.printStackTrace()
        }

        override fun processMessage(dialogId: String, qbChatMessage: QBChatMessage, senderId: Int?) {
            if (senderId != chatHelper.getCurrentQBUser().id) {
                // Process dialog ID and message receive
                Functions.showLogMessage("AllDialogsMessageListener Main", "AllDialogsMessageListener " + qbChatMessage.toString())
                getDialogManagerInstance()?.onGlobalMessageReceived(dialogId, qbChatMessage, senderId)
            }
        }
    }

    inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            Functions.showLogMessage("SystemMessagesListener Main", "SystemMessagesListener " + qbChatMessage.toString())
            getDialogManagerInstance()?.onSystemMessageReceived(qbChatMessage)
        }

        override fun processError(e: QBChatException, qbChatMessage: QBChatMessage) {
            e.printStackTrace()
        }
    }

    inner class PrivacyListListener : QBPrivacyListListener {
        override fun setPrivacyList(listName: String?, listItem: MutableList<QBPrivacyListItem>?) {
            Functions.showLogMessage("PrivacyListListener Main", "setPrivacyList " + listName.toString())
        }

        override fun updatedPrivacyList(listName: String?) {
            Functions.showLogMessage("PrivacyListListener Main", "updatedPrivacyList " + listName.toString())
        }
    }

    fun processCreateDialog(qbChatDialog: QBChatDialog) {
        // Send system message creating dialog
        getDialogManagerInstance()?.sendSystemMessageAboutCreatingDialog(systemMessagesManager!!, qbChatDialog)
    }

    fun processUpdateDialog(qbChatDialog: QBChatDialog, occupantIdsRemoved: List<Int>?) {
        // Send system message updating dialog(leave dialog, add friend, remove friend, change dialog name)
        getDialogManagerInstance()?.sendSystemMessageAboutUpdatingDialog(systemMessagesManager!!, qbChatDialog, occupantIdsRemoved)
    }

    fun processBlockUser(privacyListManager: QBPrivacyListsManager, chatUserId: String, isAllow: Boolean) {
        Functions.showLogMessage(TAG, listPrivacy?.toString())
        getDialogManagerInstance()?.blocUnblockUser(privacyListManager, listPrivacy, chatUserId, isAllow)
    }

    fun processTransferCredit(transferCreditResponse: TransferCreditResponse) {
        Functions.showLogMessage(TAG, transferCreditResponse?.toString())
        getDialogManagerInstance()?.processTransferCredit(transferCreditResponse)
    }

    fun processSendGift(sendGiftResponse: SendGiftResponse) {
        Functions.showLogMessage(TAG, sendGiftResponse?.toString())
        getDialogManagerInstance()?.processSendGift(sendGiftResponse)
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onGroupDialogEvent(groupDialogEvent: GroupDialogEvent) {
        if (groupDialogEvent.notification_type == QMMessageType.createGroupDialog.type) {
            processCreateDialog(groupDialogEvent.qbChatDialog)
        } else if (groupDialogEvent.notification_type == QMMessageType.updateGroupDialog.type) {
            processUpdateDialog(groupDialogEvent.qbChatDialog, groupDialogEvent.occupantIdsRemoved)
        }
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onPrivacyListEvent(privacyListEvent: PrivacyListEvent) {
        // Process block user in private chat when user unFriend
        // Check in local db -> block/unblock user
        processBlockUser(privacyListManager!!, privacyListEvent.chatUserId, privacyListEvent.isAllow)
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onTransferCreditEvent(transferCreditEvent: TransferCreditEvent) {
        // Send chat message with credit information
        processTransferCredit(transferCreditEvent?.transferCreditResponse!!)
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onSendGiftEvent(sendGiftEvent: SendGiftEvent) {
        // Send chat message with gift information
        processSendGift(sendGiftEvent?.sendGiftResponse!!)
    }

    var dialogManager: DialogManager? = null

    fun getDialogManagerInstance(): DialogManager? {
        if (dialogManager == null) {
            dialogManager = DialogManager(this, chatHelper, chatRepository, sharedPreferenceHelper)
        }
        return dialogManager
    }
    // Chat Listener
}
