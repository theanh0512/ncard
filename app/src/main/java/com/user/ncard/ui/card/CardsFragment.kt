package com.user.ncard.ui.card

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.util.MutableBoolean
import android.view.*
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCardsBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.namecard.NameCardDetailActivity
import com.user.ncard.ui.card.profile.FriendProfileActivity
import com.user.ncard.ui.filter.FilterActivity
import com.user.ncard.ui.group.GroupActivity
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.Friend
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.Resource
import com.user.ncard.vo.UserFilter
import javax.inject.Inject


class CardsFragment : Fragment(), Injectable {
    companion object {
        const val ID_FETCHED = "com.user.ncard.id_fetched"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var viewModel: CardsViewModel
    private lateinit var viewDataBinding: FragmentCardsBinding

    private lateinit var friendAdapter: FriendAdapter
    private lateinit var nameCardAdapter: NameCardAdapter
    private var resultGender: ArrayList<String> = ArrayList()
    private var resultNationality: ArrayList<String> = ArrayList()
    private var resultIndustry: ArrayList<String> = ArrayList()
    private var resultCountry: ArrayList<String> = ArrayList()
    private val isWithFilter = MutableBoolean(false)
    var currentFilter: UserFilter? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ID_FETCHED) {
                viewModel.start.value = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(ID_FETCHED)
        activity.registerReceiver(broadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        activity.unregisterReceiver(broadcastReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_cards, container, false)!!

        return viewDataBinding.root
    }

    private fun setupBackgroundAndActionBar() {
        Utils.setWindowsWithBackgroundColor(activity, context.getColorFromResId(R.color.colorDarkerWhite))
        (activity as AppCompatActivity).setSupportActionBar(viewDataBinding.toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            (activity.findViewById<View>(R.id.text_view_action_bar) as TextView)?.setText(R.string.title_cards)
            (activity.findViewById<View>(R.id.tvMenuLeft) as TextView)?.setText(R.string.group)
            (activity.findViewById<View>(R.id.tvMenuLeft) as TextView)?.setOnClickListener({
                val intent = Intent(activity, GroupActivity::class.java)
                startActivity(intent)
            })
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewDataBinding.searchView.setOnQueryChangeListener { oldQuery, newQuery ->
            viewModel.apply {
                if (isWithFilter.value) {
                    val filter = UserFilter(newQuery, resultIndustry, resultNationality, resultCountry, resultGender)
                    getFriendsAndNameCardWithFilter(filter)
                } else getFriendsAndNameCardWithoutFilter(newQuery)
            }
        }

        viewDataBinding.imageViewFilter.setOnClickListener {
            val intent = Intent(activity, FilterActivity::class.java)
            intent.putExtra("local", true)
            if (currentFilter != null) intent.putExtra("filter", currentFilter)
            startActivityForResult(intent, SearchByNameFragment.REQUEST_FILTER)
        }

        setupBackgroundAndActionBar()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CardsViewModel::class.java)

        friendAdapter = FriendAdapter(object : FriendAdapter.FriendClickCallback {
            override fun onClick(user: Friend) {
                val intent = Intent(activity, FriendProfileActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            }
        })
        nameCardAdapter = NameCardAdapter(object : NameCardAdapter.NameCardClickCallback {
            override fun onClick(nameCard: NameCard) {
                val intent = Intent(activity, NameCardDetailActivity::class.java)
                intent.putExtra("namecard", nameCard)
                startActivity(intent)
            }
        })

        viewDataBinding.recyclerViewCardlineFriend.apply {
            isNestedScrollingEnabled = false
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(this.getContext(),
                    (layoutManager as LinearLayoutManager).getOrientation());
            dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.view_divider_decoration));
            addItemDecoration(dividerItemDecoration)
        }
        viewDataBinding.recyclerViewNameCards.apply {
            isNestedScrollingEnabled = false
            adapter = nameCardAdapter
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(this.getContext(),
                    (layoutManager as LinearLayoutManager).getOrientation());
            dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.view_divider_decoration));
            addItemDecoration(dividerItemDecoration)
        }
        setupUserList()

        // Get data
        viewModel.start.value = true
    }

    private fun setupUserList() {
        viewModel.userList.observe(this@CardsFragment, Observer<Resource<List<Friend>>> { friendList ->
            friendAdapter.replace(friendList?.data)
            friendAdapter.notifyDataSetChanged()
        })
        viewModel.nameCardList.observe(this@CardsFragment, Observer<Resource<List<NameCard>>> { nameCardList ->
            nameCardAdapter.replace(nameCardList?.data)
            nameCardAdapter.notifyDataSetChanged()
        })
        viewModel.searchedUserList.observe(this@CardsFragment, Observer<List<Friend>> { friendList ->
            friendAdapter.replace(friendList)
            friendAdapter.notifyDataSetChanged()
        })
        viewModel.searchedNameCardList.observe(this@CardsFragment, Observer<List<NameCard>> { nameCardList ->
            nameCardAdapter.replace(nameCardList)
            nameCardAdapter.notifyDataSetChanged()
        })
    }

    override fun onStart() {
        super.onStart()
        if (!isWithFilter.value) {
            if (!sharedPreferenceHelper.getBoolean(SharedPreferenceHelper.Key.IS_JUST_LOGIN))
                viewModel.start.value = true
            else sharedPreferenceHelper.put(SharedPreferenceHelper.Key.IS_JUST_LOGIN, false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SearchByNameFragment.REQUEST_FILTER) {
            if (resultCode == Activity.RESULT_OK) {
                isWithFilter.value = true
                resultGender = data?.getSerializableExtra("gender") as ArrayList<String>
                resultCountry = data.getSerializableExtra("country") as ArrayList<String>
                resultIndustry = data.getSerializableExtra("industry") as ArrayList<String>
                resultNationality = data.getSerializableExtra("nationality") as ArrayList<String>
                viewModel.apply {
                    val filter = UserFilter(viewDataBinding.searchView.query, resultIndustry, resultNationality, resultCountry, resultGender)
                    getFriendsAndNameCardWithFilter(filter)
                    currentFilter = data.getSerializableExtra("localFilter") as UserFilter
                    viewDataBinding.filter = currentFilter
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_add, menu)
        val menuItem: MenuItem? = menu?.findItem(R.id.menu_item_add)
        val actionView = menuItem?.actionView
        actionView?.setOnClickListener {
            Log.e("NCard", "userId: " + sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
            val intent = Intent(activity, AddFriendAndNameCardActivity::class.java)
            startActivity(intent)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(activity, GroupActivity::class.java)
                startActivity(intent)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }
}