package com.user.ncard.ui.card.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.TextView
import com.quickblox.chat.model.QBChatDialog
import com.user.ncard.R
import com.user.ncard.databinding.FragmentProfileBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.FriendAdapter
import com.user.ncard.ui.card.namecard.NameCardDetailActivity
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.detail.ChatActivity
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.*
import javax.inject.Inject

class ProfileFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var chatHelper: ChatHelper

    lateinit var viewModel: ProfileViewModel
    private lateinit var viewDataBinding: FragmentProfileBinding
    var menuItem: MenuItem? = null
    lateinit var textViewTitle: TextView
    lateinit var processingAlert: AlertDialog

    lateinit var userJobAdapter: UserJobAdapter
    private lateinit var friendAdapter: FriendAdapter

    val jobs = ArrayList<Job>()
    val nameCards = ArrayList<NameCard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_profile, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, context.getColorFromResId(R.color.colorWhite))
        (activity as AppCompatActivity).setSupportActionBar(viewDataBinding.toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
            val inflaterActionbar = LayoutInflater.from(activity)
            val v = inflaterActionbar.inflate(R.layout.action_bar_layout, null)
            val params = ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER)
            actionBar.setCustomView(v, params)
            textViewTitle = v.findViewById<View>(R.id.text_view_action_bar) as TextView
            textViewTitle.setText(R.string.user_profile)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)
        val user: BaseEntity? = arguments.getParcelable(ARGUMENT_USER)
        val userId: Int? = arguments.getInt(ARGUMENT_USER_ID, 0)
        if (user != null) {
            viewDataBinding.user = user
            viewModel.userId = user.id
            viewModel.getUserJob.value = true
            viewModel.getMutualFriend.value = true
            viewDataBinding.progressBar.visibility = View.VISIBLE

            initData()
        } else if (userId != 0) {
            processingAlert = Utils.showAlert(activity)
            viewModel.getUserById(userId!!)
            viewModel.user.observe(this, Observer {
                processingAlert.cancel()
                if (it != null) {
                    viewDataBinding.user = it
                    initData()
                    activity.invalidateOptionsMenu()
                }
            })
        }
    }

    fun initData() {
        textViewTitle.text = if (viewDataBinding.user.firstName != null) getString(R.string.display_name, viewDataBinding.user.firstName, viewDataBinding.user.lastName) else viewDataBinding.user.email
        viewDataBinding.imageViewChat.setOnClickListener {
            processingAlert = Utils.showAlert(activity)
            //create chat dialog
            chatHelper.createPrivateChatDialog(viewDataBinding.user.chatId, object : ChatHelper.CreateChatDialogListener {
                override fun onCreateSuccess(qbChatDialog: QBChatDialog) {
                    processingAlert.cancel()
                    viewModel.chatRepository.insertDialogToDb(qbChatDialog)
                    startActivity(ChatActivity.getIntent(activity, qbChatDialog.dialogId))
                }

                override fun onCreateError() {
                    processingAlert.cancel()
                }
            })
        }

        viewDataBinding.imageViewQRCode.setOnClickListener {
            val qrCodeFragment = QRCodeFragment.newInstance(viewDataBinding.user)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, qrCodeFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
        }
        viewDataBinding.buttonConnect.setOnClickListener {
            processingAlert = Utils.showAlert(activity)
            viewModel.createFriendRequest(viewDataBinding.user as User)
        }
        viewDataBinding.textViewMobile.setOnClickListener {
            val alertDialogBuilder = android.app.AlertDialog.Builder(
                    context)
            alertDialogBuilder
                    .setMessage("CHOOSE ACTION")
                    .setCancelable(true)
                    .setPositiveButton("Call", { dialog, id ->
                        Functions.openPhoneCall(activity, viewDataBinding.textViewMobile.text.toString())
                        dialog.dismiss()
                    })
                    .setNegativeButton("Copy", { dialog, id ->
                        Functions.setClipboard(activity, viewDataBinding.textViewMobile.text.toString())
                        dialog.cancel()
                    })

            val alertDialog = alertDialogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.show()
        }

        viewDataBinding.textViewEmail.setOnClickListener {
            val alertDialogBuilder = android.app.AlertDialog.Builder(
                    context)
            alertDialogBuilder
                    .setMessage("CHOOSE ACTION")
                    .setCancelable(true)
                    .setPositiveButton("Copy", { dialog, id ->
                        Functions.setClipboard(activity, viewDataBinding.textViewEmail.text.toString())
                        dialog.cancel()
                    })

            val alertDialog = alertDialogBuilder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.show()
        }

        viewModel.sendRequestSuccess.observe(this@ProfileFragment, Observer {
            processingAlert.cancel()
            viewDataBinding.user.status = getString(R.string.status_pending)
            viewDataBinding.buttonConnect.visibility = View.GONE
            viewDataBinding.executePendingBindings()
            viewDataBinding.invalidateAll()
        })

        if (arguments.getBoolean(ARGUMENT_FROM_SENT_LIST)) {
            viewDataBinding.imageViewChat.visibility = View.INVISIBLE
            viewDataBinding.buttonConnect.visibility = View.GONE
        } else {
            if (Functions.getMyId(viewModel.sharedPreferenceHelper) == viewDataBinding.user.id) {
                viewDataBinding.buttonConnect.visibility = View.GONE
                viewDataBinding.imageViewChat.visibility = View.INVISIBLE
            } else {
                when {
                    viewDataBinding.user.status.equals(getString(R.string.status_friend)) -> {
                        viewDataBinding.imageViewChat.visibility = View.VISIBLE
                        viewDataBinding.buttonConnect.visibility = View.GONE
                    }
                    viewDataBinding.user.status.equals(getString(R.string.status_pending)) -> {
                        viewDataBinding.imageViewChat.visibility = View.INVISIBLE
                        viewDataBinding.buttonConnect.visibility = View.GONE
                    }
                    else -> {
                        viewDataBinding.imageViewChat.visibility = View.INVISIBLE
                        viewDataBinding.buttonConnect.visibility = View.VISIBLE
                    }
                }
            }
        }

        userJobAdapter = UserJobAdapter(object : UserJobAdapter.OnClickCallBack {
            override fun onClick(job: Job) {
                val nameCard = nameCards.findLast { job.id == it.jobId }
                if (nameCard != null) {
                    val intent = Intent(activity, NameCardDetailActivity::class.java)
                    intent.putExtra("namecard", nameCard)
                    intent.putExtra("isFromUserJob", true)
                    startActivity(intent)
                }
            }
        })

        friendAdapter = FriendAdapter(object : FriendAdapter.FriendClickCallback{
            override fun onClick(user: Friend) {

            }

        })
        viewDataBinding.recyclerViewJobs.apply {
            isNestedScrollingEnabled = false
            adapter = userJobAdapter
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(this.context,
                    (layoutManager as LinearLayoutManager).orientation);
            dividerItemDecoration.setDrawable(context.resources.getDrawable(R.drawable.view_divider_decoration))
            addItemDecoration(dividerItemDecoration)
        }

        viewDataBinding.recyclerViewMutualFriend.apply {
            isNestedScrollingEnabled = false
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(this.context,
                    (layoutManager as LinearLayoutManager).orientation);
            dividerItemDecoration.setDrawable(context.resources.getDrawable(R.drawable.view_divider_decoration))
            addItemDecoration(dividerItemDecoration)
        }

        viewModel.apply {
            jobList.observe(this@ProfileFragment, Observer {
                if (it != null) {
                    jobs.clear()
                    jobs.addAll(it)
                    jobs.sortByDescending {
                        it.from
                    }
                    viewModel.getCards.value = true
                    userJobAdapter.replace2(jobs)
                    userJobAdapter.notifyDataSetChanged()
                }
            })
            cardList.observe(this@ProfileFragment, Observer { cards ->
                viewDataBinding.progressBar.visibility = View.INVISIBLE
                if (cards != null) {
                    nameCards.clear()
                    nameCards.addAll(cards)
                    nameCards.forEach {
                        jobs.findLast { job -> job.id == it.jobId }?.cardId = it.id
                    }
                    userJobAdapter.replace2(jobs)
                    userJobAdapter.notifyDataSetChanged()
                }
            })
            mutualFriendList.observe(this@ProfileFragment, Observer {
                if(it!=null && it.isNotEmpty()){
                    viewDataBinding.textViewMutualFriend.text = getString(R.string.display_mutual_friend_in_profile,it.size)
                    friendAdapter.replace2(it)
                    friendAdapter.notifyDataSetChanged()
                }
            })
        }

        viewDataBinding.textViewShowDetails.setOnClickListener {
            val myJobFragment = UserJobFragment.newInstance(jobs, nameCards)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, myJobFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_more, menu)
        if (viewDataBinding.user != null) {
            menuItem = menu?.findItem(R.id.menu_item_more)
            menuItem?.isVisible = viewDataBinding.user.status == getString(R.string.status_friend)
            val actionView = menuItem?.actionView
            actionView?.setOnClickListener {
                val profileMoreFragment = ProfileMoreFragment.newInstance(viewDataBinding.user, nameCards)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, profileMoreFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARGUMENT_USER = "USER"
        private const val ARGUMENT_FROM_SENT_LIST = "FROM_SENT_LIST"
        const val ARGUMENT_USER_ID = "USER_ID"

        fun newInstance(user: BaseEntity, isFromSentList: Boolean = false) = ProfileFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_USER, user)
                putBoolean(ARGUMENT_FROM_SENT_LIST, isFromSentList)
            }
        }

        fun newInstance(user: BaseEntity?) = ProfileFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_USER, user)
            }
        }

        fun newInstance(user: BaseEntity?, userId: Int) = ProfileFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_USER, user)
                putInt(ARGUMENT_USER_ID, userId)
            }
        }

    }
}
