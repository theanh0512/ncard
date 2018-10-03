package com.user.ncard.ui.card.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentProfileMoreBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.SelectItemActivity
import com.user.ncard.ui.chats.PrivacyListEvent
import com.user.ncard.ui.discovery.DiscoveryNavigation
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.BaseEntity
import com.user.ncard.vo.NameCard
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


class ProfileMoreFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: ProfileMoreViewModel
    private lateinit var viewDataBinding: FragmentProfileMoreBinding
    lateinit var deleteAlert: AlertDialog
    lateinit var confirmAlert: AlertDialog
    lateinit var friend: BaseEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_profile_more, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, context.getColorFromResId(R.color.colorDarkerWhite))
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
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).setText(R.string.more)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }

        initializeButtons()

        return viewDataBinding.root
    }

    private fun initializeButtons() {
        viewDataBinding.nameCard = object : DiscoveryNavigation(getString(R.string.name_card), 0) {
            override fun onClick(view: View) {
                val friendNameCardFragment = FriendNameCardFragment.newInstance(arguments.getParcelableArrayList(ARGUMENT_NAME_CARD))
                fragmentManager.beginTransaction()
                        .replace(R.id.container, friendNameCardFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        }

        viewDataBinding.moreProfileInfo = object : DiscoveryNavigation(getString(R.string.more_profile_info), 0) {
            override fun onClick(view: View) {
                val moreProfileInfoFragment = MoreProfileInfoFragment.newInstance(arguments.getParcelable(ARGUMENT_USER))
                fragmentManager.beginTransaction()
                        .replace(R.id.container, moreProfileInfoFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        }
        viewDataBinding.group = object : DiscoveryNavigation(getString(R.string.group), 0) {
            override fun onClick(view: View) {
            }
        }
        viewDataBinding.setRemark = object : DiscoveryNavigation(getString(R.string.set_remark), 0) {
            override fun onClick(view: View) {
                val setRemarkFragment = SetRemarkFragment.newInstance(arguments.getParcelable(ARGUMENT_USER))
                fragmentManager.beginTransaction()
                        .replace(R.id.container, setRemarkFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        }
        viewDataBinding.buttonDelete.setOnClickListener {
            showDialogMessage()
        }
        viewDataBinding.buttonShare.setOnClickListener {
            val intent = Intent(activity, SelectItemActivity::class.java)
            intent.putExtra("friend", friend)
            startActivity(intent)
        }
    }

    private fun showDialogMessage() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_delete_friend, null)
        builder.setView(v)
        val deleteButton = v.findViewById<Button>(R.id.buttonDelete)
        deleteButton.setOnClickListener {
            deleteAlert = Utils.showAlert(activity)
            viewModel.deleteFriend(friend)
            // Block user from chat
            // TODO: need to  wait the delete friend successfully
            EventBus.getDefault().post(PrivacyListEvent(friend.chatId.toString(), false))
        }
        val cancelButton = v.findViewById<Button>(R.id.buttonCancel)
        cancelButton.setOnClickListener {
            confirmAlert.cancel()
        }
        val confirmText = v.findViewById<TextView>(R.id.textViewConfirmText)
        friend = arguments.getParcelable(ARGUMENT_USER)
        confirmText.text = getString(R.string.sure_to_delete, friend.firstName + " " + friend.lastName)
        confirmAlert = builder.create()
        val window: Window = confirmAlert.window
        val attributes = window.attributes
        attributes.gravity = Gravity.BOTTOM
        attributes.y = 124
        attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = attributes
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        confirmAlert.show()

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ProfileMoreViewModel::class.java)
        friend = arguments.getParcelable(ARGUMENT_USER)
        viewModel.successEvent.observe(this@ProfileMoreFragment, Observer {
            deleteAlert.cancel()
            activity.finish()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (fragmentManager.backStackEntryCount > 0)
                    fragmentManager.popBackStack()
                else NavUtils.navigateUpFromSameTask(activity)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARGUMENT_USER = "USER"
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"

        fun newInstance(user: BaseEntity, friendNameCards: ArrayList<NameCard>) = ProfileMoreFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_USER, user)
                putParcelableArrayList(ARGUMENT_NAME_CARD, friendNameCards)
            }
        }

    }
}