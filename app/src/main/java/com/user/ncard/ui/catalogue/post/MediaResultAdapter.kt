package com.user.ncard.ui.catalogue.post

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.BoxingManager
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.model.entity.impl.VideoMedia
import com.bumptech.glide.Glide
import com.user.ncard.R
import kotlinx.android.synthetic.main.item_catalogue_post_thumbnail.view.*

/**
 * Created by trong-android-dev on 18/10/17.
 */
class MediaResultAdapter(
        val ctx: Context,
        val items: MutableList<BaseMedia>,
        val listener: ClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var showAddMore = true;

    companion object {
        private val ADD_MORE_TYPE = 0
        private val MEDIA_TYPE = 1
        var listener: ClickListener? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ADD_MORE_TYPE) {
            val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_catalogue_post_thumbnail, parent, false)
            return AddMoreViewHolder(v)
        }
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_catalogue_post_thumbnail, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is ItemViewHolder) {
            val holder = holder as ItemViewHolder

            holder.imv.setImageResource(BoxingManager.getInstance().boxingConfig.mediaPlaceHolderRes)
            val media = items.get(position)
            val path: String
            if (media is ImageMedia) {
                path = (media as ImageMedia).thumbnailPath
            } else {
                path = media.getPath()
            }
            BoxingMediaLoader.getInstance().displayThumbnail(holder.imv, path, 150, 150)
            holder.itemView.setTag(position)
            holder.imvRemove.setOnClickListener(View.OnClickListener {
                items.removeAt(position)
                updateShowAddMore()
                notifyDataSetChanged()
            })
        } else if (holder is AddMoreViewHolder) {
            // BoxingMediaLoader.getInstance().displayThumbnail(holder.imv, path, 150, 150)
            holder.imv.setOnClickListener(View.OnClickListener {
                listener?.clickAddMore()
            })
            Glide.with(holder.imv.context).load(R.drawable.bg_add_picture).into(holder.imv)
            holder.imvRemove.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return items.size + if (showAddMore) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        if (showAddMore && position == items?.size) {
            return ADD_MORE_TYPE
        }
        return MEDIA_TYPE;
    }

    public fun updateShowAddMore() {
        if (items?.size == 0) {
            showAddMore = true
            return
        }
        if (items?.size > 0) {
            val media = items.get(0)
            if (media is ImageMedia && items?.size < 9) {
                showAddMore = true
                return
            }
        }
        showAddMore = false
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /*var itemBinding = ItemMusicBinding.bind(itemView)*/
        var imv = itemView.media_item
        var imvRemove = itemView.imvRemove
    }

    class AddMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /*var itemBinding = ItemMusicBinding.bind(itemView)*/
        var imv = itemView.media_item
        var imvRemove = itemView.imvRemove
    }

    open interface ClickListener {
        fun clickAddMore()
    }
}