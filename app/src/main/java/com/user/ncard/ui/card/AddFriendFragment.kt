package com.user.ncard.ui.card

import android.Manifest
import android.annotation.TargetApi
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.MutableBoolean
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.user.ncard.R
import com.user.ncard.databinding.FragmentAddFriendBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.namecard.EditNameCardFragment
import com.user.ncard.ui.card.profile.ProfileFragment
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.discovery.DiscoveryNavigation
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import javax.inject.Inject

class AddFriendFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: AddFriendViewModel
    private val hasOpenedProfile = MutableBoolean(false)
    lateinit var processingAlert: AlertDialog

    private lateinit var viewDataBinding: FragmentAddFriendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_add_friend, container, false)!!
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
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).setText(R.string.title_add_friend)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
//            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).typeface = Utils.getCustomTypeFace(activity, Utils.TypeFaceOption.BRANDON_BLACK)
        }

        initializeButtons()

        return viewDataBinding.root
    }

    private fun initializeButtons() {
        viewDataBinding.searchByName = object : DiscoveryNavigation(getString(R.string.search_by_name), 0) {
            override fun onClick(view: View) {
                val searchByNameFragment = SearchByNameFragment()
                fragmentManager.beginTransaction()
                        .replace(R.id.container, searchByNameFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        }

        viewDataBinding.scanQRCode = object : DiscoveryNavigation(getString(R.string.scan_qr_code), 0) {
            override fun onClick(view: View) {
                IntentIntegrator.forSupportFragment(this@AddFriendFragment)
                        .setCaptureActivity(QRCaptureActivity::class.java)
                        .setOrientationLocked(false)
                        .setBeepEnabled(false)
                        .setPrompt("Place the QR code inside the square")
                        .initiateScan()
            }
        }
        viewDataBinding.importFromContact = object : DiscoveryNavigation(getString(R.string.import_from_contact), 0) {
            override fun onClick(view: View) {
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) getPermissionToReadUserContacts()
                else goToSearchFromContact()
            }
        }
//        viewDataBinding.scanNameCard = object : DiscoveryNavigation(getString(R.string.scan_name_card), 0) {
//            override fun onClick(view: View) {
//                view.showSnackbar("clicked scanNameCard")
//            }
//        }
        viewDataBinding.manuallyCreate = object : DiscoveryNavigation(getString(R.string.manually_create), 0) {
            override fun onClick(view: View) {
                val editNameCardFragment = EditNameCardFragment.newInstance(null, false, false, null)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, editNameCardFragment, "EditNameCardFragment")
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        }
    }

    private fun goToSearchFromContact() {
        val searchFromContactFragment = SearchFromContactFragment()
        fragmentManager.beginTransaction()
                .replace(R.id.container, searchFromContactFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()
//        startActivity(Intent(activity,SearchFromContactActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.e("NCard", "Cancelled scan")
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Log.e("NCard", "Scanned")
                viewModel.getUser(result.contents)
                processingAlert = Utils.showAlert(activity)
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AddFriendViewModel::class.java)
        viewModel.apply {
            userData.observe(this@AddFriendFragment, Observer {
                if (it != null && !hasOpenedProfile.value) {
                    processingAlert.cancel()
                    hasOpenedProfile.value = true
                    val profileFragment = ProfileFragment.newInstance(it)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, profileFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                }
            })
            getUserSuccessEvent.observe(this@AddFriendFragment, Observer {
                hasOpenedProfile.value = false
            })
            getUserFailureEvent.observe(this@AddFriendFragment, Observer {
                processingAlert.cancel()
                Utils.showAlert(activity,getString(R.string.invalid_qr_code))
            })
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun getPermissionToReadUserContacts() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_CONTACTS)) {
            }
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                    READ_CONTACTS_PERMISSIONS_REQUEST)
        }
    }

    // Callback with the request from calling requestPermissions(...)
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goToSearchFromContact()
            } else {
                Toast.makeText(activity, "Read Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {

        // Identifier for the permission request
        private val READ_CONTACTS_PERMISSIONS_REQUEST = 1
    }
}