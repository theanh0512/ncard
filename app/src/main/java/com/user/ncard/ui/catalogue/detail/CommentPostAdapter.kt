package com.user.ncard.ui.catalogue.detail

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ItemCatalogueCommentDetailBinding
import com.user.ncard.vo.CatalogueComment
import com.user.ncard.vo.CatalogueContainer

/**
 * Created by Concaro on 05/11/2017.
 */
class CommentPostAdapter(
        val ctx: Context,
        var items: List<CatalogueComment>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_catalogue_comment_detail, parent, false)
        return ItemViewHolder(v)
    }

    fun updateItems(items: List<CatalogueComment>) {
        if (items == null) {
            return
        }
        this.items = items
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val holder = holder as ItemViewHolder
        val item = items.get(position)
        holder?.itemBinding?.item = item
        holder?.itemBinding?.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemBinding = ItemCatalogueCommentDetailBinding.bind(itemView)

    }
}