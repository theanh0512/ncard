package com.user.ncard.ui.chats.broadcastdetail

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.ItemBroadcastGroupMemberListBinding
import com.user.ncard.databinding.ItemFriendsListBinding
import com.user.ncard.ui.catalogue.utils.DefaultEndlessRecyclerAdapter
import com.user.ncard.vo.BroadcastGroup
import com.user.ncard.vo.Friend
import java.util.*

/**
 * Created by Concaro on 7/17/2017.
 */
class BroadcastGroupMemberListAdapter(val ctx: Context,
                                      var items: List<BroadcastGroup.Member>) : DefaultEndlessRecyclerAdapter<BroadcastGroupMemberListAdapter.ItemViewHolder>() {


    fun updateItems(items: List<BroadcastGroup.Member>) {
        if (items == null) {
            return
        }
        this.items = items
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_broadcast_group_member_list, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindHolder(holder: ItemViewHolder?, position: Int) {
        val holder = holder as ItemViewHolder
        val item = items.get(position)
        holder?.itemBinding?.item = item
        // Process here

        holder?.itemBinding?.executePendingBindings()
    }

    override fun onBindLoadingView(loadingText: TextView) {
        loadingText.setText(R.string.loading)
    }

    override fun onBindErrorView(errorText: TextView) {
        errorText.setText(R.string.reload)
    }

    override fun getCount(): Int {
        return items.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemBinding = ItemBroadcastGroupMemberListBinding.bind(itemView)
    }

}