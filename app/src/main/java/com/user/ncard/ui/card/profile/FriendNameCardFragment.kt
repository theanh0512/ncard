package com.user.ncard.ui.card.profile

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentFriendNameCardBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.namecard.NameCardDetailActivity
import com.user.ncard.util.Utils
import com.user.ncard.vo.NameCard

class FriendNameCardFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentFriendNameCardBinding

    private lateinit var nameCardAdapter: FriendNameCardAdapter
    val nameCards = ArrayList<NameCard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        nameCardAdapter = FriendNameCardAdapter(object : FriendNameCardAdapter.NameCardClickCallback {
            override fun onClick(nameCard: NameCard) {
                val intent = Intent(activity, NameCardDetailActivity::class.java)
                intent.putExtra("namecard", nameCard)
                intent.putExtra("isFromUserJob", true)
                startActivity(intent)
            }
        })
        viewDataBinding.recyclerViewNameCards.apply {
            isNestedScrollingEnabled = false
            adapter = nameCardAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        nameCards.addAll(arguments.getParcelableArrayList(ARGUMENT_NAME_CARD))
        nameCardAdapter.replace2(nameCards)
        nameCardAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_friend_name_card, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.empty_string)

        return viewDataBinding.root
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

    companion object {
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"

        fun newInstance(nameCards: ArrayList<NameCard>) = FriendNameCardFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ARGUMENT_NAME_CARD, nameCards)
            }
        }

    }
}