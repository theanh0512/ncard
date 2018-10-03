package com.user.ncard.ui.card.namecard

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.user.ncard.MainActivity
import com.user.ncard.R
import com.user.ncard.databinding.FragmentNameCardMoreBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.SelectItemActivity
import com.user.ncard.ui.discovery.DiscoveryNavigation
import com.user.ncard.ui.me.MyJobActivity
import com.user.ncard.ui.me.MyProfileActivity
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.NameCard
import kotlinx.android.synthetic.main.fragment_name_card_more.*
import javax.inject.Inject

class NameCardMoreFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: NameCardMoreViewModel
    private lateinit var viewDataBinding: FragmentNameCardMoreBinding
    lateinit var deleteAlert: AlertDialog
    lateinit var confirmAlert: AlertDialog
    lateinit var nameCard: NameCard
    var isMyNameCard = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_name_card_more, container, false)!!
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
        viewDataBinding.editNameCard = object : DiscoveryNavigation(getString(R.string.edit_name_card), 0) {
            override fun onClick(view: View) {
                //we are creating new name card, hence isUpdating = true
                val editNameCardFragment = EditNameCardFragment.newInstance(nameCard.copy(), true, isMyNameCard, null)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, editNameCardFragment, "EditNameCardFragment")
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
                val setRemarkFragment = SetRemarkNameCardFragment.newInstance(arguments.getParcelable(ARGUMENT_NAME_CARD))
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
            intent.putExtra("nameCard", nameCard)
            startActivity(intent)
        }
    }

    private fun showDialogMessage() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_delete_name_card, null)
        builder.setView(v)
        val deleteButton = v.findViewById<Button>(R.id.buttonDelete)
        deleteButton.setOnClickListener {
            deleteAlert = Utils.showAlert(activity)
            viewModel.deleteNameCard(nameCard, arguments.getBoolean(ARGUMENT_IS_MY_NAME_CARD))
        }
        val cancelButton = v.findViewById<Button>(R.id.buttonCancel)
        cancelButton.setOnClickListener {
            confirmAlert.cancel()
        }
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
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NameCardMoreViewModel::class.java)
        nameCard = arguments.getParcelable(ARGUMENT_NAME_CARD)
        viewDataBinding.nameCard = nameCard
        viewModel.successEvent.observe(this@NameCardMoreFragment, Observer {
            deleteAlert.cancel()
            if (arguments.getBoolean(ARGUMENT_IS_MY_NAME_CARD)) {
                if (arguments.getBoolean(ARGUMENT_IS_FROM_JOB_ICON)) {
                    val intent = Intent(activity, MyJobActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                } else {
                    val intent = Intent(activity, MyProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            } else {
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        })
        isMyNameCard = arguments.getBoolean(ARGUMENT_IS_MY_NAME_CARD)
        if (isMyNameCard) {
            includeGroup.visibility = View.GONE
            includeSetRemark.visibility = View.GONE
            buttonShare.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"
        private const val ARGUMENT_IS_MY_NAME_CARD = "IS_MY_NAME_CARD"
        private const val ARGUMENT_IS_FROM_JOB_ICON = "IS_FROM_JOB_ICON"

        fun newInstance(nameCard: NameCard, isMyNameCard: Boolean, fromJobIcon: Boolean = false) = NameCardMoreFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_NAME_CARD, nameCard)
                putBoolean(ARGUMENT_IS_MY_NAME_CARD, isMyNameCard)
                putBoolean(ARGUMENT_IS_FROM_JOB_ICON, fromJobIcon)
            }
        }

    }
}