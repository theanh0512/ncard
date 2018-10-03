package com.user.ncard.ui.me.ewallet

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentTransactionLogDetailBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.profile.FriendProfileActivity
import com.user.ncard.ui.card.profile.ProfileFragment
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.GlideHelper
import com.user.ncard.ui.me.ChangePassActivity
import com.user.ncard.util.Utils
import com.user.ncard.vo.EWalletTransactionStatusType
import javax.inject.Inject

class TransactionLogDetailFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EWalletViewModel
    private lateinit var viewDataBinding: FragmentTransactionLogDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_transaction_log_detail, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.transaction_detail, R.color.colorWhite246)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)
        viewModel.transactionId= arguments.getInt(ARGUMENT_TRANSACTION_ID)
        viewModel.startLoadTransactionLogDetail.value = true
        viewModel.transactionLogDetail.observe(this@TransactionLogDetailFragment, Observer {
            if(it!=null) {
                viewDataBinding.transaction = it
                viewDataBinding.showSender = !(Functions.getMyId(viewModel.sharedPreferenceHelper) == viewDataBinding.transaction.sender.id)
            }
            initType()
        })
    }

    fun initType() {
        var type = viewDataBinding.transaction.title
        when(viewDataBinding.transaction.type) {
            "deposit" -> type = "Top Up"
            "payment" -> type = "Payment"
            "transfer" -> type = "Transfer"
            "gift" -> type = "Gift"
            "withdraw" -> type = "Withdraw"
            "cash_out" -> type = "Cash Out"
            "cash_out_fee" -> type = "Gift Cash Out Charge Fee"
        }
        viewDataBinding.tvType.text = type
        (activity.findViewById<View>(R.id.text_view_action_bar) as TextView)?.text = type

        viewDataBinding.tvWarningTransfer.visibility = View.GONE
        if(viewDataBinding.transaction.type == "transfer") {
            if(Functions.getMyId(viewModel.sharedPreferenceHelper) == viewDataBinding.transaction.sender.id) {
                // Show receiver
                viewDataBinding.tvSenderReceiver.text = "Receiver"
                viewDataBinding.tvName.text = viewDataBinding.transaction.receiver.name
                GlideHelper.displayAvatar(viewDataBinding.imvSenderReceiver, viewDataBinding.transaction.receiver.profileImageUrl)
                if(EWalletTransactionStatusType.ONHOLD.status == viewDataBinding.transaction.status) {
                    viewDataBinding.tvWarningTransfer.visibility = View.VISIBLE
                }

                viewDataBinding.lnSenderReceiver.setOnClickListener({
                    val intent = Intent(activity, FriendProfileActivity::class.java)
                    intent.putExtra(ProfileFragment.ARGUMENT_USER_ID, viewDataBinding.transaction.receiver.id)
                    startActivity(intent)
                })

            } else if(Functions.getMyId(viewModel.sharedPreferenceHelper) == viewDataBinding.transaction.receiver.id) {
                // Show sender
                viewDataBinding.tvSenderReceiver.text = "Sender"
                viewDataBinding.tvName.text = viewDataBinding.transaction.sender.name
                GlideHelper.displayAvatar(viewDataBinding.imvSenderReceiver, viewDataBinding.transaction.sender.profileImageUrl)

                viewDataBinding.lnSenderReceiver.setOnClickListener({
                    val intent = Intent(activity, FriendProfileActivity::class.java)
                    intent.putExtra(ProfileFragment.ARGUMENT_USER_ID, viewDataBinding.transaction.sender.id)
                    startActivity(intent)
                })
            }
        }
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
        private const val ARGUMENT_TRANSACTION_ID = "TRANSACTION_ID"

        fun newInstance(transactionId: Int) = TransactionLogDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARGUMENT_TRANSACTION_ID, transactionId)
            }
        }

    }
}