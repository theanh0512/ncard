package com.user.ncard.ui.chats.detail

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.load.engine.GlideException
import com.quickblox.chat.model.QBChatDialog
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.DefaultEndlessRecyclerAdapter
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.GlideHelper
import com.user.ncard.ui.catalogue.views.RelativeTimeTextView
import com.user.ncard.ui.chats.utils.audio.AudioController
import com.user.ncard.ui.chats.utils.audio.MediaController
import com.user.ncard.ui.chats.utils.audio.SingleMediaManager
import com.user.ncard.ui.chats.views.QBPlaybackControlView
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.*
import java.util.*


/**
 * Created by dangui on 14/11/17.
 */
class MessageListAdapter(val context: Context,
                         var items: List<ChatMessage>,
                         val qbChatDialog: QBChatDialog?,
                         val chatHelper: ChatHelper,
                         val listener: MessageClickCallback?,
                         val sharedPreferenceHelper: SharedPreferenceHelper) :
        DefaultEndlessRecyclerAdapter<RecyclerView.ViewHolder>() {

    companion object {
        var listener: MessageClickCallback? = null
    }

    val TAG = "MessageListAdapter"

    protected val TYPE_TEXT_LEFT = 1
    protected val TYPE_TEXT_RIGHT = 2
    protected val TYPE_IMAGE_LEFT = 3
    protected val TYPE_IMAGE_RIGHT = 4
    protected val TYPE_VIDEO_LEFT = 5
    protected val TYPE_VIDEO_RIGHT = 6
    protected val TYPE_AUDIO_LEFT = 7
    protected val TYPE_AUDIO_RIGHT = 8
    protected val TYPE_LOCATION_LEFT = 9
    protected val TYPE_LOCATION_RIGHT = 10
    protected val TYPE_CREDIT_LEFT = 11
    protected val TYPE_CREDIT_RIGHT = 12
    protected val TYPE_GIFT_LEFT = 13
    protected val TYPE_GIFT_RIGHT = 14

    private val containerLayoutRes = object : SparseIntArray() {
        init {
            put(TYPE_TEXT_LEFT, R.layout.item_chat_text_left)
            put(TYPE_TEXT_RIGHT, R.layout.item_chat_text_right)
            put(TYPE_IMAGE_LEFT, R.layout.item_chat_image_left)
            put(TYPE_IMAGE_RIGHT, R.layout.item_chat_image_right)
            put(TYPE_VIDEO_LEFT, R.layout.item_chat_video_left)
            put(TYPE_VIDEO_RIGHT, R.layout.item_chat_video_right)
            put(TYPE_AUDIO_LEFT, R.layout.item_chat_audio_left)
            put(TYPE_AUDIO_RIGHT, R.layout.item_chat_audio_right)
            put(TYPE_LOCATION_LEFT, R.layout.item_chat_location_left)
            put(TYPE_LOCATION_RIGHT, R.layout.item_chat_location_right)
            put(TYPE_CREDIT_LEFT, R.layout.item_chat_credit_left)
            put(TYPE_CREDIT_RIGHT, R.layout.item_chat_credit_right)
            put(TYPE_GIFT_LEFT, R.layout.item_chat_gift_left)
            put(TYPE_GIFT_RIGHT, R.layout.item_chat_gift_right)
        }
    }

    fun updateItems(items: List<ChatMessage>) {
        if (items == null) {
            return
        }
        this.items = items
    }

    override fun getItemViewType(position: Int): Int {
        val chatMessage = items[position]
        var type = TYPE_TEXT_LEFT
        if (isIncoming(chatMessage)) {
            when (chatMessage.customParam.chat_content_type) {
                ChatMessageContentType.TEXT.type -> type = TYPE_TEXT_LEFT
                ChatMessageContentType.IMAGE.type -> type = TYPE_IMAGE_LEFT
                ChatMessageContentType.VIDEO.type -> type = TYPE_VIDEO_LEFT
                ChatMessageContentType.AUDIO.type -> type = TYPE_AUDIO_LEFT
                ChatMessageContentType.LOCATION.type -> type = TYPE_LOCATION_LEFT
                ChatMessageContentType.CREDIT.type -> type = TYPE_CREDIT_LEFT
                ChatMessageContentType.GIFT.type -> type = TYPE_GIFT_LEFT
            }
        } else {
            when (chatMessage.customParam.chat_content_type) {
                ChatMessageContentType.TEXT.type -> type = TYPE_TEXT_RIGHT
                ChatMessageContentType.IMAGE.type -> type = TYPE_IMAGE_RIGHT
                ChatMessageContentType.VIDEO.type -> type = TYPE_VIDEO_RIGHT
                ChatMessageContentType.AUDIO.type -> type = TYPE_AUDIO_RIGHT
                ChatMessageContentType.LOCATION.type -> type = TYPE_LOCATION_RIGHT
                ChatMessageContentType.CREDIT.type -> type = TYPE_CREDIT_RIGHT
                ChatMessageContentType.GIFT.type -> type = TYPE_GIFT_RIGHT
            }
        }
        return type
    }


    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT_LEFT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageTextViewHolder(v)
            }
            TYPE_TEXT_RIGHT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageTextViewHolder(v)
            }
            TYPE_IMAGE_LEFT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageImageViewHolder(v)
            }
            TYPE_IMAGE_RIGHT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageImageViewHolder(v)
            }
            TYPE_VIDEO_LEFT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageVideoViewHolder(v)
            }
            TYPE_VIDEO_RIGHT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageVideoViewHolder(v)
            }
            TYPE_AUDIO_LEFT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageAudioViewHolder(v)
            }
            TYPE_AUDIO_RIGHT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageAudioViewHolder(v)
            }
            TYPE_LOCATION_LEFT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageLocationViewHolder(v)
            }
            TYPE_LOCATION_RIGHT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageLocationViewHolder(v)
            }
            TYPE_CREDIT_LEFT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageCreditViewHolder(v)
            }
            TYPE_CREDIT_RIGHT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageCreditViewHolder(v)
            }
            TYPE_GIFT_LEFT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageGiftViewHolder(v)
            }
            TYPE_GIFT_RIGHT -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(containerLayoutRes.get(viewType), parent, false)
                MessageGiftViewHolder(v)
            }

            else -> {
                val v: View = LayoutInflater.from(parent?.context).inflate(R.layout.item_chat_text_left, parent, false)
                MessageTextViewHolder(v)
            }
        }
    }

    override fun onBindHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val message: ChatMessage = items[position]
        when (holder?.itemViewType) {
            TYPE_TEXT_LEFT -> onBindMessageTextLeftViewHolder(holder as MessageTextViewHolder, message, position)
            TYPE_TEXT_RIGHT -> onBindMessageTextRightViewHolder(holder as MessageTextViewHolder, message, position)
            TYPE_IMAGE_LEFT -> onBindMessageImageLeftViewHolder(holder as MessageImageViewHolder, message, position)
            TYPE_IMAGE_RIGHT -> onBindMessageImageRightViewHolder(holder as MessageImageViewHolder, message, position)
            TYPE_VIDEO_LEFT -> onBindMessageVideoLeftViewHolder(holder as MessageVideoViewHolder, message, position)
            TYPE_VIDEO_RIGHT -> onBindMessageVideoRightViewHolder(holder as MessageVideoViewHolder, message, position)
            TYPE_AUDIO_LEFT -> onBindMessageAudioLeftViewHolder(holder as MessageAudioViewHolder, message, position)
            TYPE_AUDIO_RIGHT -> onBindMessageAudioRightViewHolder(holder as MessageAudioViewHolder, message, position)
            TYPE_LOCATION_LEFT -> onBindMessageLocationLeftViewHolder(holder as MessageLocationViewHolder, message, position)
            TYPE_LOCATION_RIGHT -> onBindMessageLocationRightViewHolder(holder as MessageLocationViewHolder, message, position)
            TYPE_CREDIT_LEFT -> onBindMessageCreditLeftViewHolder(holder as MessageCreditViewHolder, message, position)
            TYPE_CREDIT_RIGHT -> onBindMessageCreditRightViewHolder(holder as MessageCreditViewHolder, message, position)
            TYPE_GIFT_LEFT -> onBindMessageGiftLeftViewHolder(holder as MessageGiftViewHolder, message, position)
            TYPE_GIFT_RIGHT -> onBindMessageGiftRightViewHolder(holder as MessageGiftViewHolder, message, position)
            else -> {
                onBindMessageTextLeftViewHolder(holder as MessageTextViewHolder, message, position)
            }
        }
    }

    fun setVisibilityHolder(tvName: TextView?, isRightHolder: Boolean) {
        if (qbChatDialog?.type?.code == ChatDialogType.PRIVATE.type) {
            tvName?.visibility = View.GONE
        } else if (qbChatDialog?.type?.code == ChatDialogType.GROUP.type && !isRightHolder) {
            tvName?.visibility = View.VISIBLE
        }
    }

    fun onBindMessageTextLeftViewHolder(vh: MessageTextViewHolder, message: ChatMessage, position: Int) {
        bindMessageText(vh, message, position)

        setVisibilityHolder(vh?.tvName, false)
    }

    fun onBindMessageTextRightViewHolder(vh: MessageTextViewHolder, message: ChatMessage, position: Int) {
        bindMessageText(vh, message, position)

        setVisibilityHolder(vh?.tvName, true)
    }

    // Bind Text
    fun bindMessageText(vh: MessageTextViewHolder, message: ChatMessage, position: Int) {
        vh.tvName?.text = message.senderName
        GlideHelper.displayAvatar(vh.avatar!!, message.senderAvatar)
        vh.tvTime?.setReferenceTime(message.customParam?.sender_date_sent?.time!!)
        vh.tvMessage?.text = message.text

        setLongClick(vh?.bubleChat!!, message, position)
    }

    fun onBindMessageImageLeftViewHolder(vh: MessageImageViewHolder, message: ChatMessage, position: Int) {
        bindMessageImage(vh, message, position)

        setVisibilityHolder(vh?.tvName, false)
    }

    fun onBindMessageImageRightViewHolder(vh: MessageImageViewHolder, message: ChatMessage, position: Int) {
        bindMessageImage(vh, message, position)

        setVisibilityHolder(vh?.tvName, true)
    }

    // Bind Image
    fun bindMessageImage(vh: MessageImageViewHolder, message: ChatMessage, position: Int) {
        vh.tvName?.text = message.senderName
        GlideHelper.displayAvatar(vh.avatar!!, message.senderAvatar)
        vh.tvTime?.setReferenceTime(message.customParam?.sender_date_sent?.time!!)

        loadImage(vh.imvImage!!, vh.progressBar!!, message.customParam.chat_file?.remoteUrl)

        vh?.imvImage?.setOnClickListener({
            listener?.onImageClick(message, position)
        })

        setLongClick(vh?.bubleChat!!, message, position)
        setLongClick(vh?.imvImage!!, message, position)
    }

    fun onBindMessageVideoLeftViewHolder(vh: MessageVideoViewHolder, message: ChatMessage, position: Int) {
        bindMessageVideo(vh, message, position)

        setVisibilityHolder(vh?.tvName, false)
    }

    fun onBindMessageVideoRightViewHolder(vh: MessageVideoViewHolder, message: ChatMessage, position: Int) {
        bindMessageVideo(vh, message, position)

        setVisibilityHolder(vh?.tvName, true)
    }

    // Bind Video
    fun bindMessageVideo(vh: MessageVideoViewHolder, message: ChatMessage, position: Int) {
        vh.tvName?.text = message.senderName
        GlideHelper.displayAvatar(vh.avatar!!, message.senderAvatar)
        vh.tvTime?.setReferenceTime(message.customParam?.sender_date_sent?.time!!)

        loadImage(vh.imvImage!!, vh.progressBar!!, message.customParam.chat_file?.thumbnailRemoteUrl)

        vh?.imvImage?.setOnClickListener({
            listener?.onVideoClick(message, position)
        })

        setLongClick(vh?.bubleChat!!, message, position)
        setLongClick(vh?.imvImage!!, message, position)
    }

    fun onBindMessageAudioLeftViewHolder(vh: MessageAudioViewHolder, message: ChatMessage, position: Int) {
        bindMessageAudio(vh, message, position)

        setVisibilityHolder(vh?.tvName, false)
    }

    fun onBindMessageAudioRightViewHolder(vh: MessageAudioViewHolder, message: ChatMessage, position: Int) {
        bindMessageAudio(vh, message, position)

        setVisibilityHolder(vh?.tvName, true)
    }

    // Bind Audio
    fun bindMessageAudio(vh: MessageAudioViewHolder, message: ChatMessage, position: Int) {
        vh.tvName?.text = message.senderName
        GlideHelper.displayAvatar(vh.avatar!!, message.senderAvatar)
        vh.tvTime?.setReferenceTime(message.customParam?.sender_date_sent?.time!!)

        showAudioView(vh.playerAudio!!, Uri.parse(message.customParam?.chat_file?.remoteUrl!!), position)
        setDuration(message.customParam?.chat_file?.duration?.toDouble(), vh?.tvTimeDuration!!)

        setLongClick(vh?.bubleChat!!, message, position)
    }

    fun onBindMessageLocationLeftViewHolder(vh: MessageLocationViewHolder, message: ChatMessage, position: Int) {
        bindMessageLocation(vh, message, position)

        setVisibilityHolder(vh?.tvName, false)
    }

    fun onBindMessageLocationRightViewHolder(vh: MessageLocationViewHolder, message: ChatMessage, position: Int) {
        bindMessageLocation(vh, message, position)

        setVisibilityHolder(vh?.tvName, true)
    }

    // Bind Location
    fun bindMessageLocation(vh: MessageLocationViewHolder, message: ChatMessage, position: Int) {
        vh.tvName?.text = message.senderName
        GlideHelper.displayAvatar(vh.avatar!!, message.senderAvatar)
        vh.tvTime?.setReferenceTime(message.customParam?.sender_date_sent?.time!!)
        vh.tvMessage?.text = message?.customParam?.chat_location?.name + if (message?.customParam?.chat_location?.address != null) "\n" + message?.customParam?.chat_location?.address else ""

        loadImage(vh.imvImage!!, vh.progressBar!!,
                Functions.getGoogleMapStatic(message?.customParam?.chat_location?.lat.toString(), message?.customParam?.chat_location?.lng.toString(), context))

        vh?.imvImage?.setOnClickListener({
            listener?.onLocationClick(message, position)
        })

        setLongClick(vh?.bubleChat!!, message, position)
        setLongClick(vh?.imvImage!!, message, position)
    }

    fun onBindMessageCreditLeftViewHolder(vh: MessageCreditViewHolder, message: ChatMessage, position: Int) {
        bindMessageCredit(vh, message, position)

        setVisibilityHolder(vh?.tvName, false)
    }

    fun onBindMessageCreditRightViewHolder(vh: MessageCreditViewHolder, message: ChatMessage, position: Int) {
        bindMessageCredit(vh, message, position)

        setVisibilityHolder(vh?.tvName, true)
    }

    // Bind Credit
    fun bindMessageCredit(vh: MessageCreditViewHolder, message: ChatMessage, position: Int) {
        vh.tvName?.text = message.senderName
        GlideHelper.displayAvatar(vh.avatar!!, message.senderAvatar)
        vh.tvTime?.setReferenceTime(message.customParam?.sender_date_sent?.time!!)

        setLongClick(vh?.bubleChat!!, message, position)
        vh.bubleChat?.setOnClickListener({
            listener?.onCreditClick(message, position)
        })

        // Set message
        val credit_transaction = message?.customParam?.credit_transaction
        var message = ""
        if (sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID) == credit_transaction?.sender?.id) {
            // Sender
            message = "${credit_transaction?.amount?.currency}${credit_transaction?.amount?.amount} To ${credit_transaction?.receiver?.name}"
        } else {
            // Receiver
            message = "${credit_transaction?.amount?.currency}${credit_transaction?.amount?.amount} From ${credit_transaction?.sender?.name}"
        }

        if (credit_transaction?.status == EWalletTransactionStatusType.ONHOLD.status) {
            vh?.tvStatus.text = context.getString(R.string.credit_status_check_out)
            vh?.tvStatus.setBackgroundColor(context.resources.getColor(R.color.colorDarkBlue))
        } else if (credit_transaction?.status == EWalletTransactionStatusType.COMPLETED.status) {
            vh?.tvStatus.text = context.getString(R.string.credit_status_accepted)
            vh?.tvStatus.setBackgroundColor(context.resources.getColor(R.color.colorGrey))
        } else {
            vh?.tvStatus.text = context.getString(R.string.credit_status_refunded)
            vh?.tvStatus.setBackgroundColor(context.resources.getColor(R.color.colorGrey))
        }
        vh.tvMessage.text = message

    }

    fun onBindMessageGiftLeftViewHolder(vh: MessageGiftViewHolder, message: ChatMessage, position: Int) {
        bindMessageGift(vh, message, position)

        setVisibilityHolder(vh?.tvName, false)
    }

    fun onBindMessageGiftRightViewHolder(vh: MessageGiftViewHolder, message: ChatMessage, position: Int) {
        bindMessageGift(vh, message, position)

        setVisibilityHolder(vh?.tvName, true)
    }

    // Bind Gift
    fun bindMessageGift(vh: MessageGiftViewHolder, message: ChatMessage, position: Int) {
        vh.tvName?.text = message.senderName
        GlideHelper.displayAvatar(vh.avatar!!, message.senderAvatar)
        vh.tvTime?.setReferenceTime(message.customParam?.sender_date_sent?.time!!)

        setLongClick(vh?.bubleChat!!, message, position)
        vh.bubleChat?.setOnClickListener({
            listener?.onGiftClick(message, position)
        })

        val gift = message.customParam?.gift
        if (gift?.status == ECommerceGiftStatus.SENT.status) {
            vh?.tvStatus.text = context.getString(R.string.gift_status_check_out)
            vh?.tvStatus.setBackgroundColor(context.resources.getColor(R.color.colorDarkBlue))
        } else if (gift?.status == ECommerceGiftStatus.ACCEPTED.status) {
            vh?.tvStatus.text = context.getString(R.string.gift_status_accepted)
            vh?.tvStatus.setBackgroundColor(context.resources.getColor(R.color.colorGrey))
        } else {
            vh?.tvStatus.text = context.getString(R.string.gift_status_refunded)
            vh?.tvStatus.setBackgroundColor(context.resources.getColor(R.color.colorGrey))
        }
        vh.tvMessage.text = gift?.message
    }


    fun loadImage(imageView: ImageView, progressBar: ProgressBar, url: String?) {
        progressBar.animate().setStartDelay(10).alpha(1f)
        // Loading image
        GlideHelper.displayRawCenterCrop(imageView, url, 0, 0, object : GlideHelper.ImageLoadingListener {
            override fun onLoaded() {
                progressBar.animate().cancel()
                progressBar.animate().alpha(0f)
            }

            override fun onFailed(e: GlideException) {
                progressBar.animate().alpha(0f)
            }
        })
    }

    private fun setLongClick(view: View, chatMessage: ChatMessage, position: Int) {
        view?.setOnLongClickListener {
            listener?.onLongClick(chatMessage, position)
            true
        }
    }

    private fun isIncoming(chatMessage: ChatMessage): Boolean {
        val currentUser = chatHelper.getCurrentQBUser()
        return chatMessage.senderId != null && chatMessage.senderId != currentUser?.getId()
    }

    /*private fun isRead(chatMessage: ChatMessage): Boolean {
        val currentUserId = chatHelper.getCurrentQBUser()?.id
        return !CollectionsUtil.isEmpty(chatMessage.readIds) && chatMessage.readIds.contains(currentUserId)
    }*/

    /*private fun readMessage(chatMessage: ChatMessage) {
        try {
            qbChatDialog.readMessage()
        } catch (e: XMPPException) {
            Log.w(TAG, e)
        } catch (e: SmackException.NotConnectedException) {
            Log.w(TAG, e)
        }
    }*/

    override fun onBindLoadingView(loadingText: TextView) {
        loadingText.setText(R.string.loading)
    }

    override fun onBindErrorView(errorText: TextView) {
        errorText.setText(R.string.reload)
    }

    override fun getCount(): Int {
        return items.size
    }


    /* ViewHolder */
    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root: View? = itemView.findViewById<View>(R.id.root)
        var bubleChat: View? = itemView.findViewById<View>(R.id.bubbleChat)
        var avatar: ImageView? = itemView.findViewById<ImageView>(R.id.imvAvatar)
        var tvName: TextView? = itemView.findViewById<TextView>(R.id.tvName)
        var tvTime: RelativeTimeTextView? = itemView.findViewById<RelativeTimeTextView>(R.id.tvTime)
    }

    class MessageTextViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var tvMessage: TextView? = itemView.findViewById<TextView>(R.id.tvMessage)
    }

    class MessageImageViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var imvImage: ImageView? = itemView.findViewById<ImageView>(R.id.imvImage)
        var progressBar: ProgressBar? = itemView.findViewById<ProgressBar>(R.id.progressBar)
    }

    class MessageVideoViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var imvImage: ImageView? = itemView.findViewById<ImageView>(R.id.imvImage)
        var progressBar: ProgressBar? = itemView.findViewById<ProgressBar>(R.id.progressBar)
    }

    class MessageAudioViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var imvImage: ImageView? = itemView.findViewById<ImageView>(R.id.imvPlayPause)
        var tvMessage: TextView? = itemView.findViewById<TextView>(R.id.tvMessage)
        var playerAudio: QBPlaybackControlView? = itemView.findViewById<QBPlaybackControlView>(R.id.playerAudio)
        var tvTimeDuration: TextView? = itemView.findViewById<TextView>(R.id.tvTimeDuration)
    }

    class MessageLocationViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var imvImage: ImageView? = itemView.findViewById<ImageView>(R.id.imvImage)
        var progressBar: ProgressBar? = itemView.findViewById<ProgressBar>(R.id.progressBar)
        var tvMessage: TextView = itemView.findViewById<TextView>(R.id.tvMessage)
    }

    class MessageCreditViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var tvMessage: TextView = itemView.findViewById<TextView>(R.id.tvMessage)
        var tvStatus: TextView = itemView.findViewById<TextView>(R.id.tvStatus)
    }

    class MessageGiftViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var tvMessage: TextView = itemView.findViewById<TextView>(R.id.tvMessage)
        var tvStatus: TextView = itemView.findViewById<TextView>(R.id.tvStatus)
    }
    /* ViewHolder */

    interface MessageClickCallback {
        fun onLongClick(item: ChatMessage, position: Int)

        fun onItemClick(item: ChatMessage, position: Int)

        fun onImageClick(item: ChatMessage, position: Int)

        fun onVideoClick(item: ChatMessage, position: Int)

        fun onLocationClick(item: ChatMessage, position: Int)

        fun onAudioClick(item: ChatMessage, position: Int)

        fun onCreditClick(item: ChatMessage, position: Int)

        fun onGiftClick(item: ChatMessage, position: Int)

    }

    open fun getImageSelected(holder: RecyclerView.ViewHolder): View? {
        if (holder is RecyclerView.ViewHolder) {

            return if (holder == null) null else {
                var imvImage: ImageView? = holder?.itemView.findViewById<ImageView>(R.id.imvImage)
                return if (imvImage == null) null else imvImage
            }
        }
        return null
    }

    open fun getBubleChatLongClick(holder: RecyclerView.ViewHolder): View? {
        if (holder is RecyclerView.ViewHolder) {

            return if (holder == null) null else {
                var bubbleChat: View? = holder?.itemView.findViewById<View>(R.id.bubbleChat)
                return if (bubbleChat == null) null else bubbleChat
            }
        }
        return null
    }

    fun setDuration(duration: Double?, tvDuration: TextView) {
        duration?.let {
            tvDuration.setText(Functions.stringForTime((duration * 1000).toInt()))
        }
    }

    /* Audio */
    private var mediaManager: SingleMediaManager? = null
    private var audioController: AudioController? = null
    private var mediaControllerEventListener: MediaControllerEventListener? = null
    private var playerViewHashMap: MutableMap<QBPlaybackControlView, Int>? = null
    private var activePlayerViewPosition = -1

    private fun showAudioView(playerView: QBPlaybackControlView, uri: Uri, position: Int) {
        initPlayerView(playerView, uri, position)
        if (isCurrentViewActive(position)) {
            Log.d(TAG, "showAudioView isCurrentViewActive")
            playerView.restoreState(getMediaManagerInstance().exoPlayer)
        }
    }

    private fun initPlayerView(playerView: QBPlaybackControlView, uri: Uri, position: Int) {
        playerView.releaseView()
        setViewPosition(playerView, position)
        playerView.initView(getAudioControllerInstance(), uri)
    }

    fun getMediaManagerInstance(): SingleMediaManager {
        return if (mediaManager == null) SingleMediaManager(context) else mediaManager!!
    }

    private fun getAudioControllerInstance(): AudioController {
        return if (audioController == null)
            AudioController(getMediaManagerInstance(), getMediaControllerEventListenerInstance())
        else
            audioController!!
    }


    private fun isCurrentViewActive(position: Int): Boolean {
        return activePlayerViewPosition == position
    }

    private fun setPlayerViewActivePosition(activeViewPosition: Int) {
        this.activePlayerViewPosition = activeViewPosition
    }

    private fun setViewPosition(view: QBPlaybackControlView, position: Int) {
        if (playerViewHashMap == null) {
            playerViewHashMap = WeakHashMap()
        }
        playerViewHashMap?.put(view, position)
    }

    private fun getPlayerViewPosition(view: QBPlaybackControlView): Int {
        val position = playerViewHashMap!![view]
        return position ?: activePlayerViewPosition
    }

    private fun getMediaControllerEventListenerInstance(): MediaControllerEventListener {
        return if (mediaControllerEventListener == null) MediaControllerEventListener() else mediaControllerEventListener!!
    }

    private inner class MediaControllerEventListener : MediaController.EventMediaController {

        override fun onPlayerInViewInit(view: QBPlaybackControlView) {
            setPlayerViewActivePosition(getPlayerViewPosition(view))
        }
    }
    /* Audio */

}