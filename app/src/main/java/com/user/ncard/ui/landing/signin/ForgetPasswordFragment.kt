package com.user.ncard.ui.landing.signin

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.MutableBoolean
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.user.ncard.MainActivity
import com.user.ncard.R
import com.user.ncard.databinding.FragmentForgetPasswordBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.landing.confirmaccount.ConfirmAccountFragment
import com.user.ncard.ui.me.MeViewModel
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ForgetPasswordFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var viewModel: SignInViewModel
    lateinit var meViewModel: MeViewModel
    private lateinit var viewDataBinding: FragmentForgetPasswordBinding

    var isConfirmClicked = MutableBoolean(false)
    var newPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_forget_password, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.space_string)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SignInViewModel::class.java)
        meViewModel = ViewModelProviders.of(this, viewModelFactory).get(MeViewModel::class.java)

        viewDataBinding.apply {
            buttonSubmit.setOnClickListener {
                if (editTextEmail.text.isNotBlank()) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(editTextEmail.text.toString()).matches()) Utils.showAlert(activity, getString(R.string.input_valid_email))
                    else {
                        isConfirmClicked.value = true
                        viewModel.forgetPassword(editTextEmail.text.toString().toLowerCase())
                        viewDataBinding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
        viewModel.apply {
            forgetPasswordSuccess.observe(this@ForgetPasswordFragment, Observer {
                if (newPassword != null) {
                    viewDataBinding.progressBar.visibility = View.GONE
                    viewModel.signInWithEmailAndPassword(viewDataBinding.editTextEmail.text.toString(), this@ForgetPasswordFragment.newPassword!!)
                }
            })

            continueToCode.observe(this@ForgetPasswordFragment, Observer {
                val intent = Intent(activity, ForgetPasswordActivity::class.java)
                intent.putExtra("email", viewDataBinding.editTextEmail.text.toString())
                startActivityForResult(intent, REQUEST_CODE_AND_PASSWORD)
            })

            signInSuccessEvent.observe(this@ForgetPasswordFragment, Observer {
                // Process logout
                val signout_remain_db = sharedPreferenceHelper.getBoolean(SharedPreferenceHelper.Key.SIGNOUT_REMAIN_DB)
                if (signout_remain_db) {
                    processLogout()
                }
                viewModel.getUserInfo(object: Callback<User> {
                    override fun onFailure(call: Call<User>?, t: Throwable?) {
                    }

                    override fun onResponse(call: Call<User>?, response: Response<User>?) {
                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)
                        activity.finish()
                    }

                })
            })
            signInFailureEvent.observe(this@ForgetPasswordFragment, Observer {
                showProgress.set(false)
            })
            userNotConfirmEvent.observe(this@ForgetPasswordFragment, Observer {
                showProgress.set(false)
                if (viewModel.email?.get() != null && viewModel.password?.get() != null) {
                    val confirmAccountFragment = ConfirmAccountFragment.newInstance(viewModel.email!!.get()!!.toLowerCase(), viewModel.password!!.get()!!)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, confirmAccountFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                }
            })
        }
        viewModel.errorForgetPassword.observe(this, Observer {
            if (it != null) {
                if (it.contains("limit exceeded")) {
                    Utils.showAlert(activity, it)
                    viewDataBinding.progressBar.visibility = View.GONE
                } else {
                    viewDataBinding.progressBar.visibility = View.GONE
                    val intent = Intent(activity, ForgetPasswordActivity::class.java)
                    intent.putExtra("error", it)
                    intent.putExtra("email", viewDataBinding.editTextEmail.text.toString())
                    startActivityForResult(intent, REQUEST_CODE_AND_PASSWORD)
                }
            }
        })
        initOldEmail()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_AND_PASSWORD -> if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val password = data.getStringExtra("newPass")
                    newPassword = password
                    val code = data.getStringExtra("code")
                    viewModel.forgetPasswordContinuation?.apply {
                        setPassword(password)
                        setVerificationCode(code)
                        continueTask()
                    }
                }
            }
        }
    }

//    fun showDialogMessage() {
//        confirmAlert = AlertDialog.Builder(activity).create()
//        confirmAlert.setTitle("Email not matched!")
//
//        confirmAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", { _, _ ->
//            run {
//                confirmAlert.cancel()
//            }
//        })
//
//        confirmAlert.show()
//    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    var oldEmail: String? = null

    fun initOldEmail() {
        oldEmail = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL)
    }

    fun processLogout() {
        val newEmail: String? = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL)
        if(oldEmail != null && oldEmail == newEmail) {
            // Still old account -> Do nothing
        } else {
            // New account -> logout
            meViewModel.logout()
        }
    }

    companion object {
        const val ARGUMENT_USERNAME = "USERNAME"
        const val REQUEST_CODE_AND_PASSWORD = 123

        fun newInstance() = ForgetPasswordFragment().apply {
            arguments = Bundle().apply {
            }
        }

    }
}