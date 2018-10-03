package com.user.ncard.ui.catalogue.main

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.GlideHelper
import kotlinx.android.synthetic.main.item_catalogue_media_thumbnail.view.*

/**
 * Created by trong-android-dev on 18/10/17.
 */
class MediaAdapter(
        val ctx: Context,
        var items: List<Media>,
        var listener: ClickListener,
        var cataloguePosition: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val MEDIA_TYPE = 1
        var listener: ClickListener? = null
    }

    fun updateItems(items: List<Media>, listener: ClickListener, cataloguePosition: Int) {
        if (items == null) {
            return
        }
        this.items = items
        this.listener = listener
        this.cataloguePosition = cataloguePosition
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_catalogue_media_thumbnail, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is ItemViewHolder) {
            val holder = holder as ItemViewHolder

            val media = items.get(position)
            var path: String = ""
            if (media is Image) {
                path = media.path
                holder.imvVideoPlay.visibility = View.GONE
                holder.imv.visibility = View.VISIBLE
                holder.imvVideo.visibility = View.GONE
                GlideHelper.displayThumbnail(holder.imv, path, 150, 150)
            } else if (media is Video) {
                path = media.path
                holder.imvVideoPlay.visibility = View.VISIBLE
                holder.imv.visibility = View.GONE
                holder.imvVideo.visibility = View.VISIBLE
                GlideHelper.displayThumbnail(holder.imvVideo, path, 300, 150)
            }

            holder.imv.setOnClickListener({
                listener?.clickMedias(items, position, cataloguePosition)
            })

            holder.imvVideo.setOnClickListener({
                listener?.clickMedias(items, position, cataloguePosition)
            })
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return MEDIA_TYPE;
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /*var itemBinding = ItemMusicBinding.bind(itemView)*/
        var imv = itemView.imvImage
        var imvVideo = itemView.imvVideo
        var imvVideoPlay = itemView.imvVideoPlay
    }

    open interface ClickListener {
        fun clickMedias(medias: List<Media>?, mediaPosition: Int, cataloguePosition: Int)
    }

    open fun getImageSelected(holder: RecyclerView.ViewHolder): View? {
        if (holder is ItemViewHolder) {
            return holder?.imv
        }
        return null
    }
}