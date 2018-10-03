package com.user.ncard.ui.card.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentSetRemarkBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.Utils
import com.user.ncard.vo.BaseEntity
import javax.inject.Inject

class SetRemarkFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: SetRemarkViewModel
    private lateinit var viewDataBinding: FragmentSetRemarkBinding
    lateinit var friend: BaseEntity
    lateinit var updateRemarkAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_set_remark, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, ContextCompat.getColor(context, R.color.colorDarkerWhite))
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
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).setText(R.string.remark)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SetRemarkViewModel::class.java)
        friend = arguments.getParcelable(ARGUMENT_USER)
        viewDataBinding.viewmodel = viewModel
        viewDataBinding.viewmodel.remark.set(friend.remark)
        viewModel.successEvent.observe(this@SetRemarkFragment, Observer {
            updateRemarkAlert.cancel()
            fragmentManager.popBackStack()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_save -> {
                updateRemarkAlert = Utils.showAlert(activity)
                viewModel.updateRemark(friend)
            }
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

        fun newInstance(user: BaseEntity) = SetRemarkFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_USER, user)
            }
        }

    }
}