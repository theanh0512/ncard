package com.user.ncard.ui.me.gift

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentSendGiftBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.chats.SendGiftEvent
import com.user.ncard.util.Utils
import com.user.ncard.vo.Friend
import com.user.ncard.vo.GiftItem
import com.user.ncard.vo.SendGiftRequest
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class SendGiftFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentSendGiftBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: GiftViewModel
    val friendsList = ArrayList<Friend>()

    var friend: Friend? = null
    var fromScreen: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_send_gift, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.send_gift, R.color.colorDarkBlue)
        fromScreen = arguments.getString(MyGiftFragment.ARGUMENT_FROM_SCREEN)
        viewDataBinding.gift = arguments.getSerializable(ARGUMENT_GIFT) as GiftItem?
        viewDataBinding.editTextNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!p0.isNullOrEmpty()) {
                    viewDataBinding.buttonConfirm.apply {
                        setBackgroundResource(R.drawable.rounded_corner_button)
                        isEnabled = true
                    }
                } else {
                    viewDataBinding.buttonConfirm.apply {
                        setBackgroundResource(R.drawable.rounded_corner_button_disabled)
                        isEnabled = false
                    }
                }
            }
        })
        viewDataBinding.buttonConfirm.setOnClickListener {
            viewDataBinding.progressBar.visibility = View.VISIBLE
            viewModel.sendGift(arguments.getInt(ARGUMENT_GIFT_ID),
                    SendGiftRequest(arguments.getParcelable<Friend>(ARGUMENT_FRIEND).id,
                            viewDataBinding.editTextNote.text.toString()))
        }

        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GiftViewModel::class.java)
        viewDataBinding.friend = arguments.getParcelable(ARGUMENT_FRIEND)
        viewModel.sendGiftResponse.observe(this@SendGiftFragment, Observer {
            if (it != null) {
                viewDataBinding.progressBar.visibility = View.GONE
                activity.finish()
                if (fromScreen == MyGiftFragment.FROM_SCREEN_WALLET) {
                    startActivity(Intent(activity, MyGiftActivity::class.java).putExtra(MyGiftFragment.ARGUMENT_FROM_SCREEN, fromScreen))
                } else if (fromScreen == MyGiftFragment.FROM_SCREEN_CHAT) {
                    var friend: Friend? = arguments.getParcelable(ARGUMENT_FRIEND)
                    startActivity(Intent(activity, MyGiftActivity::class.java).putExtra(MyGiftFragment.ARGUMENT_FRIEND, friend).putExtra(MyGiftFragment.ARGUMENT_FROM_SCREEN, fromScreen))
                }
                // Send chat message with gift information
                EventBus.getDefault().post(SendGiftEvent(it))
            }
        })
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
        const val ARGUMENT_FRIEND = "FRIEND"
        const val ARGUMENT_GIFT_ID = "GIFT_ID"
        const val ARGUMENT_GIFT = "GIFT"
        const val ARGUMENT_FROM_SCREEN = "FROM_SCREEN"
        fun newInstance(friend: Friend, gift: GiftItem, giftId: Int, fromScreen: String?) = SendGiftFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_FRIEND, friend)
                putSerializable(ARGUMENT_GIFT, gift)
                putInt(ARGUMENT_GIFT_ID, giftId)
                putString(ARGUMENT_FROM_SCREEN, fromScreen)
            }
        }

    }
}