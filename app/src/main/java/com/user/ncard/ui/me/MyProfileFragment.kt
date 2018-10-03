package com.user.ncard.ui.me

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentMyProfileBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.profile.QRCodeFragment
import com.user.ncard.util.Utils
import com.user.ncard.vo.BaseEntity
import com.user.ncard.vo.User
import javax.inject.Inject

class MyProfileFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: MyProfileViewModel
    private lateinit var viewDataBinding: FragmentMyProfileBinding
    var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_profile, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.title_me)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyProfileViewModel::class.java)
        viewModel.getMe()

        viewModel.userData.observe(this@MyProfileFragment, Observer<User>{ user ->
            viewDataBinding.user = user
            initData()
        })
    }

    fun initData() {
        if(viewDataBinding.user != null) {
            viewDataBinding.imageViewQRCode.setOnClickListener {
                val qrCodeFragment = QRCodeFragment.newInstance(viewDataBinding.user)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, qrCodeFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
            viewDataBinding.containerMe.setOnClickListener {
                val editMyProfile = EditMyProfileFragment.newInstance(viewDataBinding.user)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, editMyProfile)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }

            viewDataBinding.textViewShowDetails.setOnClickListener {
                val myJobFragment = MyJobFragment()
                fragmentManager.beginTransaction()
                        .replace(R.id.container, myJobFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_me, menu)
        menuItem = menu?.findItem(R.id.menu_item_name_card)
        val actionView = menuItem?.actionView
        actionView?.setOnClickListener {
            val myNameCardFragment = MyNameCardFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, myNameCardFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
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
}