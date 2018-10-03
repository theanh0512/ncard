package com.user.ncard.ui.landing.signin

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentSetNewPasswordBinding
import com.user.ncard.util.Utils

class SetPaymentPasswordFragment : Fragment(){
    private lateinit var viewDataBinding: FragmentSetNewPasswordBinding
    var notMatchPasswordAlert: AlertDialog? = null
    var createdPasswordAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_set_new_password, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.create_password)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun showAlert(activity: Activity, message: String): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.cancel()
            activity.onBackPressed()
        }
        builder.setTitle(R.string.cardline)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_submit, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
            R.id.menu_item_submit -> {
                if (viewDataBinding.editTextPassword.text.toString() != viewDataBinding.editTextRetypePassword.text.toString())
                    notMatchPasswordAlert = Utils.showAlert(activity, getString(R.string.not_match_password))
                else {
                    val intent = Intent()
                    intent.putExtra("code", arguments.getString(ARGUMENT_CODE))
                    intent.putExtra("newPass", viewDataBinding.editTextPassword.text.toString())
                    activity.setResult(Activity.RESULT_OK, intent)
                    activity.finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARGUMENT_CODE = "CODE"
        fun newInstance(code: String) = SetPaymentPasswordFragment().apply {
            arguments = Bundle().apply {
                putString(ARGUMENT_CODE, code)
            }
        }
    }
}