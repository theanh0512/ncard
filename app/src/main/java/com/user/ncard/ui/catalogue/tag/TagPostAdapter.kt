package com.user.ncard.ui.card.catalogue.tag

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.user.ncard.R
import com.user.ncard.databinding.ItemCatalogueTagPostBinding
import com.user.ncard.vo.TagPost
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import kotlinx.android.synthetic.main.item_catalogue_tag_header_post.view.*


/**
 * Created by Concaro on 7/17/2017.
 */
class TagPostAdapter internal constructor(
        val ctx: Context,
        val sectionAdapter: SectionedRecyclerViewAdapter,
        val title: String,
        val items: List<TagPost>) :
        StatelessSection(SectionParameters.Builder(R.layout.item_catalogue_tag_post)
                .headerResourceId(R.layout.item_catalogue_tag_header_post)
                .build()), TagPostFragment.FilterableSection {

    var filteredList: MutableList<TagPost>

    init {
        this.filteredList = ArrayList<TagPost>()
        this.filteredList.addAll(items)
    }

    override fun getContentItemsTotal(): Int {
        return filteredList.size
    }

    fun getTagPostIds(): List<String> {
        val result = ArrayList<String>()
        items.forEachIndexed { index, tagPost ->
            if (tagPost.selected) {
                result.add(tagPost.id.toString())
            }
        }
        return result
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        return ItemViewHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ItemViewHolder
        val item = filteredList.get(position)
        holder?.itemBinding.item = item
        // Process here
        holder?.itemBinding.root?.setOnClickListener({
            item.selected = !item.selected
            sectionAdapter.notifyItemChangedInSection(this, position)
        })
        holder?.itemBinding?.executePendingBindings()
    }

    /*fun clickItem(tagPostSelected: TagPost) {
        items?.forEach {
            if(it.id == tagPostSelected.id && it.tagGroupId == tagPostSelected.tagGroupId) {
                it.selected = !it.selected
            }
        }
    }*/

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return HeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        val headerHolder = holder as HeaderViewHolder?
        headerHolder?.tvHeader?.setText(title)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemBinding = ItemCatalogueTagPostBinding.bind(itemView)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvHeader = itemView.tvHeader
    }

    fun <T : RecyclerView.ViewHolder> T.onClick(event: (view: View, position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(it, getAdapterPosition(), getItemViewType())
        }
        return this
    }

    override fun filter(query: String?) {
        filteredList.clear()
        if (TextUtils.isEmpty(query)) {
            this.filteredList.addAll(items)
            this.isVisible = true
        } else {
            filteredList.clear()
            val filterPattern = query.toString().toLowerCase().trim { it <= ' ' }
            for (value in items) {
                if (value?.name?.toLowerCase().contains(filterPattern)) {
                    filteredList.add(value)
                }
            }

            this.isVisible = !filteredList.isEmpty()
        }
    }


}