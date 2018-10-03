package com.user.ncard.ui.group

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentGroupBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.Utils
import com.user.ncard.vo.Group
import com.user.ncard.vo.Resource
import javax.inject.Inject

class GroupFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentGroupBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GroupViewModel
    lateinit var groupAdapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        groupAdapter = GroupAdapter(object : GroupAdapter.OnClickCallBack {
            override fun onClick(group: Group) {
                val createGroupFragment = CreateGroupFragment.newInstance(group)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, createGroupFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }

        })
        viewDataBinding.recyclerViewGroup.apply {
            isNestedScrollingEnabled = false
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        viewModel.groupList.observe(this@GroupFragment, Observer<Resource<List<Group>>> { groupList ->
            if (groupList?.data != null && groupList.data.isNotEmpty()) {
                groupAdapter.replace(groupList.data)
                groupAdapter.notifyDataSetChanged()
                viewDataBinding.textViewNoGroupFound.visibility = View.GONE
            } else {
                viewDataBinding.textViewNoGroupFound.visibility = View.VISIBLE
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_group, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.groups)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GroupViewModel::class.java)
        viewModel.start.value = true
        return viewDataBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_add, menu)
        val menuItem: MenuItem? = menu?.findItem(R.id.menu_item_add)
        val actionView = menuItem?.actionView
        actionView?.setOnClickListener {
            val createGroupFragment = CreateGroupFragment.newInstance(null)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, createGroupFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
        }
        super.onCreateOptionsMenu(menu, inflater)
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
}