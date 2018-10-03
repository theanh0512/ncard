package com.user.ncard.ui.chats.forward

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.ItemDialogsListBinding
import com.user.ncard.ui.catalogue.utils.DefaultEndlessRecyclerAdapter
import com.user.ncard.ui.catalogue.utils.GlideHelper
import com.user.ncard.vo.ChatDialog
import java.util.*

/**
 * Created by Concaro on 7/17/2017.
 */
class DialogsGroupListAdapter(val ctx: Context,
                              var items: List<ChatDialog>,
                              val itemsSelected: ArrayMap<String, ChatDialog>) : DefaultEndlessRecyclerAdapter<DialogsGroupListAdapter.ItemViewHolder>(), Filterable {

    var dialogFilter: DialogFilter? = null

    fun updateItems(items: List<ChatDialog>) {
        if (items == null) {
            return
        }
        this.items = items
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_dialogs_list, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindHolder(holder: ItemViewHolder?, position: Int) {
        val holder = holder as ItemViewHolder
        val item = items.get(position)
        holder?.itemBinding?.item = item
        // Process here
        if(item.photo != null) {
            GlideHelper.displayAvatar(holder.itemBinding.imvAvatar, item.photo)
        } else {
            holder.itemBinding.imvAvatar.setImageResource(R.drawable.group_default)
        }
        holder?.itemBinding?.checkBox?.isChecked = itemsSelected.get(item.dialogId) != null
        holder?.itemBinding?.rootView?.setOnClickListener({
            if (holder?.itemBinding?.checkBox?.isEnabled!!) {
                if (itemsSelected.get(item.dialogId) != null) {
                    itemsSelected.remove(item.dialogId)
                } else {
                    itemsSelected.put(item.dialogId, item)
                }
                notifyDataSetChanged()
            }
        })

        holder?.itemBinding?.executePendingBindings()
    }

    override fun getFilter(): Filter? {
        if (dialogFilter == null) {
            dialogFilter = DialogFilter(ctx, this, this.items)
        }
        return dialogFilter
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
        var itemBinding = ItemDialogsListBinding.bind(itemView)
    }

    class DialogFilter constructor(val ctx: Context, val adapter: DialogsGroupListAdapter, originalList: List<ChatDialog>) : Filter() {

        private val originalList: List<ChatDialog>

        private val filteredList: MutableList<ChatDialog>

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

                for (dialog in originalList) {
                    if (dialog.name?.toLowerCase()?.contains(filterPattern)!!) {
                        filteredList.add(dialog)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            adapter.updateItems(results.values as ArrayList<ChatDialog>)
            adapter.notifyDataSetChanged()
        }
    }

}