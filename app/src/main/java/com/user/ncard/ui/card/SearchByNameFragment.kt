package com.user.ncard.ui.card

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.user.ncard.R
import com.user.ncard.databinding.FragmentSearchByNameBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.profile.ProfileFragment
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.filter.FilterActivity
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.User
import com.user.ncard.vo.UserFilter
import javax.inject.Inject

class SearchByNameFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var searchUserAdapter: SearchUserAdapter
    lateinit var searchUserByJobAdapter: SearchUserAdapter

    lateinit var viewModel: SearchByNameViewModel
    private lateinit var viewDataBinding: FragmentSearchByNameBinding
    var resultGender: ArrayList<String> = ArrayList()
    var resultNationality: ArrayList<String> = ArrayList()
    var resultIndustry: ArrayList<String> = ArrayList()
    var resultCountry: ArrayList<String> = ArrayList()
    var currentFilter: UserFilter? = null

    val userListByName = ArrayList<User>()
    val userListByProfession = ArrayList<User>()

    lateinit var sendingRequestAlert: AlertDialog

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_search_by_name, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, context.getColorFromResId(R.color.colorDarkerWhite))
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchByNameViewModel::class.java)
        viewDataBinding.searchView.setOnSearchListener(object : FloatingSearchView.OnSearchListener {
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion?) {

            }

            override fun onSearchAction(currentQuery: String?) {
                if (!currentQuery.isNullOrEmpty()) {
                    viewModel.apply {
                        val filter = UserFilter(viewDataBinding.searchView.query, resultIndustry, resultNationality, resultCountry, resultGender)
                        query.value = filter
                    }
                }
            }
        })

        viewDataBinding.searchView.setOnMenuItemClickListener {
            val intent = Intent(activity, FilterActivity::class.java)
            if (currentFilter != null) intent.putExtra("filter", currentFilter)
            startActivityForResult(intent, REQUEST_FILTER)
        }

        viewDataBinding.textViewCancel.setOnClickListener {
            fragmentManager.popBackStackImmediate()
        }

        viewModel.userList.observe(this@SearchByNameFragment, Observer {
            userListByName.clear()
            if (it?.data != null) {
                // Remove my user
                val searchUsers = it.data.filter { it.id != Functions.getMyId(viewModel.sharedPreferenceHelper) }
                (searchUsers as ArrayList?)?.sortByDescending { it.mutualFriendCount }

                userListByName.addAll(searchUsers)
                setHeaderFriendVisibility()

                searchUserAdapter.replace(searchUsers)
                searchUserAdapter.notifyDataSetChanged()
                viewDataBinding.searchView.requestFocus()
                viewDataBinding.recyclerViewCardlineFriend.requestLayout()
            } else {
                setHeaderFriendVisibility()
                searchUserAdapter.replace(null)
                searchUserAdapter.notifyDataSetChanged()
                viewDataBinding.searchView.requestFocus()
                viewDataBinding.recyclerViewCardlineFriend.requestLayout()
            }
        })
        viewModel.professionList.observe(this@SearchByNameFragment, Observer {
            userListByProfession.clear()
            if (it?.data != null) {
                // Remove my user
                val searchUsers = it.data.filter { it.id != Functions.getMyId(viewModel.sharedPreferenceHelper) }
                (searchUsers as ArrayList?)?.sortByDescending { it.mutualFriendCount }

                userListByProfession.addAll(searchUsers)
                setHeaderProfessionVisibility()

                searchUserByJobAdapter.replace(searchUsers)
                searchUserByJobAdapter.notifyDataSetChanged()

                viewDataBinding.recyclerViewProfessions.requestLayout()
            } else {
                setHeaderProfessionVisibility()
                searchUserByJobAdapter.replace(null)
                searchUserByJobAdapter.notifyDataSetChanged()
                viewDataBinding.searchView.requestFocus()
                viewDataBinding.recyclerViewProfessions.requestLayout()
            }
        })
        searchUserAdapter = SearchUserAdapter(
                object : SearchUserAdapter.UserClickCallback {
                    override fun onClick(user: User) {
                        val profileFragment = ProfileFragment.newInstance(user)
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, profileFragment)
                                .addToBackStack(null)
                                .commitAllowingStateLoss()
                    }
                },
                object : SearchUserAdapter.AddFriendCallback {
                    override fun onClick(user: User) {
                        sendingRequestAlert = Utils.showAlert(activity)
                        viewModel.createFriendRequest(user)
                    }
                })
        searchUserByJobAdapter = SearchUserAdapter(
                object : SearchUserAdapter.UserClickCallback {
                    override fun onClick(user: User) {
                        val profileFragment = ProfileFragment.newInstance(user)
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, profileFragment)
                                .addToBackStack(null)
                                .commitAllowingStateLoss()
                    }
                },
                object : SearchUserAdapter.AddFriendCallback {
                    override fun onClick(user: User) {
                        sendingRequestAlert = Utils.showAlert(activity)
                        viewModel.createFriendRequest(user)
                    }
                })
        viewDataBinding.recyclerViewCardlineFriend.apply {
            isNestedScrollingEnabled = false
            adapter = searchUserAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }

        viewDataBinding.recyclerViewProfessions.apply {
            isNestedScrollingEnabled = false
            adapter = searchUserByJobAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }

        viewModel.apply {
            sendRequestSuccess.observe(this@SearchByNameFragment, Observer {
                sendingRequestAlert.cancel()
                val filter = UserFilter(viewDataBinding.searchView.query, resultIndustry, resultNationality, resultCountry, resultGender)
                query.value = filter
            })
        }
        //setHeaderVisibility()
    }

    private fun setHeaderFriendVisibility() {
        if (userListByName.size > 0) {
            viewDataBinding.textViewNoUserFound.visibility = View.GONE
            viewDataBinding.textView6.visibility = View.VISIBLE
            viewDataBinding.recyclerViewCardlineFriend.visibility = View.VISIBLE
        } else {
            viewDataBinding.textView6.visibility = View.GONE
            //viewDataBinding.recyclerViewCardlineFriend.visibility = View.GONE
            if (userListByProfession.size == 0) viewDataBinding.textViewNoUserFound.visibility = View.VISIBLE
        }
    }

    private fun setHeaderProfessionVisibility() {
        if (userListByProfession.size > 0) {
            viewDataBinding.textViewNoUserFound.visibility = View.GONE
            viewDataBinding.textView7.visibility = View.VISIBLE
            viewDataBinding.recyclerViewProfessions.visibility = View.VISIBLE
        } else {
            viewDataBinding.textView7.visibility = View.GONE
            //viewDataBinding.recyclerViewProfessions.visibility = View.GONE
            if (userListByName.size == 0) viewDataBinding.textViewNoUserFound.visibility = View.VISIBLE
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FILTER) {
            if (resultCode == Activity.RESULT_OK) {
                resultGender = data?.getSerializableExtra("gender") as ArrayList<String>
                resultCountry = data.getSerializableExtra("country") as ArrayList<String>
                resultIndustry = data.getSerializableExtra("industry") as ArrayList<String>
                resultNationality = data.getSerializableExtra("nationality") as ArrayList<String>
                currentFilter = UserFilter(viewDataBinding.searchView.query, resultIndustry, resultNationality, resultCountry, resultGender)
                if (viewDataBinding.searchView.query.isNotEmpty()) viewModel.query.value = currentFilter
                viewDataBinding.filter = currentFilter
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val REQUEST_FILTER = 101
    }
}