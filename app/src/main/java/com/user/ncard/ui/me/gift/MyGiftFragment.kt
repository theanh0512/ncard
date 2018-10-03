package com.user.ncard.ui.me.gift

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentMyGiftBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.me.ewallet.SelectFriendActivity
import com.user.ncard.util.Utils
import com.user.ncard.vo.Friend
import com.user.ncard.vo.MyGiftData
import javax.inject.Inject

class MyGiftFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GiftViewModel
    private lateinit var viewDataBinding: FragmentMyGiftBinding
    lateinit var giftAdapter: GiftAdapter
    val giftList = ArrayList<MyGiftData>()
    var chosenGift: MyGiftData? = null

    var friend: Friend? = null
    var fromScreen: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_my_gift, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.my_gifts)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GiftViewModel::class.java)

        friend = arguments.getParcelable(MyGiftFragment.ARGUMENT_FRIEND)
        fromScreen = arguments.getString(MyGiftFragment.ARGUMENT_FROM_SCREEN)
        giftAdapter = GiftAdapter(object : GiftAdapter.OnClickCallback {
            override fun onClick(item: MyGiftData) {

            }
        }, object : GiftAdapter.OnClickSendCallBack {
            override fun onClick(item: MyGiftData) {
                chosenGift = item
                if (friend != null) {
                    val cashTransferFragment = SendGiftFragment.newInstance(friend!!, chosenGift!!.product, chosenGift!!.id, fromScreen)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, cashTransferFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                } else {
                    val intent = Intent(activity, SelectFriendActivity::class.java)
                    startActivityForResult(intent, REQUEST_CODE_SELECT_FRIEND)
                }
            }
        })
        viewModel.giftResponse.observe(this@MyGiftFragment, Observer { giftResponse ->
            giftList.clear()
            if (giftResponse?.purchaseItems != null && giftResponse.purchaseItems.isNotEmpty()) {
                viewDataBinding.textViewNoGift.visibility = View.GONE
                //clear all the count first
                giftResponse.purchaseItems.forEach {
                    it.product.count = 0
                }
                giftResponse.purchaseItems.forEach { data ->
                    val filteredGift = giftList.filter { it.product.title == data.product.title }
                    if (filteredGift.isNotEmpty()) {
                        if (filteredGift[0].product.count == null || filteredGift[0].product.count == 0) filteredGift[0].product.count = 2
                        else filteredGift[0].product.count = filteredGift[0].product.count!! + 1
                    } else giftList.add(data.copy())
                }
                giftList.sortBy { it.product.title }
                giftAdapter.replace2(giftList)
                giftAdapter.notifyDataSetChanged()
            } else {
                giftList.clear()
                viewDataBinding.textViewNoGift.visibility = View.VISIBLE
            }
        })
        viewDataBinding.recyclerViewGift.apply {
            adapter = giftAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getGift.value = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_FRIEND) {
                if (data != null && chosenGift != null) {
                    val cashTransferFragment = SendGiftFragment.newInstance(data.getParcelableExtra("friend"), chosenGift!!.product, chosenGift!!.id, fromScreen)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, cashTransferFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_shop, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
            R.id.menu_item_shop -> {
                val shopFragment = ShopFragment()
                fragmentManager.beginTransaction()
                        .replace(R.id.container, shopFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val REQUEST_CODE_SELECT_FRIEND = 55
        const val ARGUMENT_FRIEND = "FRIEND"
        const val ARGUMENT_FROM_SCREEN = "FROM_SCREEN"
        const val FROM_SCREEN_WALLET = "from_screen_wallet"
        const val FROM_SCREEN_CHAT = "from_screen_chat"
        fun newInstance(friend: Friend?, fromScreen: String?) = MyGiftFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MyGiftFragment.ARGUMENT_FRIEND, friend)
                putString(MyGiftFragment.ARGUMENT_FROM_SCREEN, fromScreen)
            }
        }
    }
}