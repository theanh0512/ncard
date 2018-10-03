package com.user.ncard.ui.catalogue.category

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.vo.CategoryPost
import kotlinx.android.synthetic.main.item_catalogue_share_post.view.*

/**
 * Created by Concaro on 7/17/2017.
 */
class CategoryPostAdapter(
        val ctx: Context,
        val items: List<CategoryPost>
) : RecyclerView.Adapter<CategoryPostAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_catalogue_share_post, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: ItemViewHolder?, position: Int) {
        /*val albumModel = viewModel.items.get(position)
        holder?.itemBinding?.viewModel = MusicItemViewModel(ctx, albumModel)
        holder?.itemBinding?.executePendingBindings()*/

        val item = items.get(position)
        holder?.tvDes?.text = item.des
        holder?.tvName?.text = item.name
        holder?.ckBox?.isChecked = if (item.selected) true else false

        holder?.rootView?.setOnClickListener({
            items.forEach {
                it.selected = false
            }
            item.selected = !item.selected
            notifyDataSetChanged()
        })

    }

    fun getCategory(): String {
        items.forEachIndexed { index, item ->
            if (item.selected) {
                return item.name
            }
        }
        return ""
    }

    override fun getItemCount(): Int {
        /*return viewModel?.items.size*/
        return items.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /*var itemBinding = ItemMusicBinding.bind(itemView)*/
        var tvDes = itemView.tvDes
        var tvName = itemView.tvName
        var ckBox = itemView.checkBox
        var rootView = itemView.rootView

    }

    fun <T : RecyclerView.ViewHolder> T.onClick(event: (view: View, position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(it, getAdapterPosition(), getItemViewType())
        }
        return this
    }

}