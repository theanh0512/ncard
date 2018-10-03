package com.user.ncard.ui.chats.friends

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
import com.user.ncard.databinding.ItemFriendsListBinding
import com.user.ncard.ui.catalogue.utils.DefaultEndlessRecyclerAdapter
import com.user.ncard.vo.Friend
import java.util.*

/**
 * Created by Concaro on 7/17/2017.
 */
class FriendsListAdapter(val ctx: Context,
                         var items: List<Friend>,
                         val itemsSelected: SparseArray<Friend>,
                         val friendsSelected: List<Int>?,
                         val occupants: List<Int>?,
                         val type: Int) : DefaultEndlessRecyclerAdapter<FriendsListAdapter.ItemViewHolder>(), Filterable {

    var friendFilter: FriendFilter? = null

    fun updateItems(items: List<Friend>) {
        if (items == null) {
            return
        }
        this.items = items
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_friends_list, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindHolder(holder: ItemViewHolder?, position: Int) {
        val holder = holder as ItemViewHolder
        val item = items.get(position)
        holder?.itemBinding?.item = item
        // Process here
        if (type == FriendsListActivity.CHAT_GROUP_MANAGE_AS_MEMBER && occupants!= null && occupants?.contains(item.chatId)) {
            holder?.itemBinding?.checkBox?.isEnabled = false
        } else {
            holder?.itemBinding?.checkBox?.isEnabled = true
        }
        holder?.itemBinding?.checkBox?.isChecked = itemsSelected.get(item.id) != null
        holder?.itemBinding?.rootView?.setOnClickListener({
            if (holder?.itemBinding?.checkBox?.isEnabled!!) {
                if (itemsSelected.get(item.id) != null) {
                    itemsSelected.remove(item.id)
                } else {
                    itemsSelected.put(item.id, item)
                }
                notifyDataSetChanged()
            }
        })

        holder?.itemBinding?.executePendingBindings()
    }

    override fun getFilter(): Filter? {
        if (friendFilter == null) {
            friendFilter = FriendFilter(ctx, this, this.items)
        }
        return friendFilter
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
        var itemBinding = ItemFriendsListBinding.bind(itemView)
    }

    class FriendFilter constructor(val ctx: Context, val adapter: FriendsListAdapter, originalList: List<Friend>) : Filter() {

        private val originalList: List<Friend>

        private val filteredList: MutableList<Friend>

        init {
            this.originalList = LinkedList(originalList)
            this.filteredList = ArrayList()
        }

        override fun performFiltering(constraint: CharSequence): FilterResults {
            filteredList.clear()
            val results = FilterResults()

            if (constraint.length == 0) {
                filteredList.addAll(originalList)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }

                for (user in originalList) {
                    if (ctx.getString(R.string.display_name, user.firstName, user.lastName).toLowerCase().contains(filterPattern)) {
                        filteredList.add(user)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            adapter.updateItems(results.values as ArrayList<Friend>)
            adapter.notifyDataSetChanged()
        }
    }

}