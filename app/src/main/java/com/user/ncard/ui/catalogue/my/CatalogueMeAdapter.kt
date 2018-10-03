package com.user.ncard.ui.catalogue.my

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bilibili.boxing_impl.view.SpacesItemDecoration
import com.user.ncard.R
import com.user.ncard.databinding.ItemCatalogueMeBinding
import com.user.ncard.ui.card.catalogue.ViewUtils
import com.user.ncard.ui.card.catalogue.main.CatalogueAdapter
import com.user.ncard.ui.catalogue.main.Media
import com.user.ncard.ui.catalogue.main.MediaAdapter
import com.user.ncard.ui.catalogue.main.Video
import com.user.ncard.ui.catalogue.mediaviewer.FullScreenVideoPlayerActivity
import com.user.ncard.ui.catalogue.utils.DefaultEndlessRecyclerAdapter
import com.user.ncard.vo.CatalogueContainer

/**
 * Created by Concaro on 7/17/2017.
 */
class CatalogueMeAdapter(val ctx: Context,
                         var items: List<CatalogueContainer>,
                         val listener: CatalogueAdapter.OnImageClickListener) : DefaultEndlessRecyclerAdapter<CatalogueMeAdapter.ItemViewHolder>(),
        MediaAdapter.ClickListener {

    companion object {
        var listener: CatalogueAdapter.OnImageClickListener? = null
    }

    fun updateItems(items: List<CatalogueContainer>) {
        if (items == null) {
            return
        }
        this.items = items
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_catalogue_me, parent, false)
        return ItemViewHolder(v)
    }


    override fun onBindHolder(holder: ItemViewHolder?, position: Int) {
        val holder = holder as ItemViewHolder
        val item = items.get(position)
        holder?.itemBinding?.item = item
        // Process here
        buildMedias(holder, item, position)
        buildTags(holder, item, position)
        holder?.itemBinding?.cardView?.setOnClickListener({
            listener?.onItemClick(item, position)
        })
        holder?.itemBinding?.imvLike?.setOnClickListener({
            listener?.onLikeClick(item, position)
        })
        holder?.itemBinding?.executePendingBindings()
    }

    fun buildMedias(holder: ItemViewHolder?, item: CatalogueContainer, position: Int) {
        if (item?.medias != null && item?.medias?.size != 0) {
            holder?.itemBinding?.recyclerviewMedias?.visibility = View.VISIBLE
            if (holder?.mediaAdapter == null) {
                val gridLayoutManager = GridLayoutManager(ctx, 3)
                holder?.itemBinding?.recyclerviewMedias?.setLayoutManager(gridLayoutManager)
                holder?.itemBinding?.recyclerviewMedias?.setHasFixedSize(true)
                holder?.itemBinding?.recyclerviewMedias?.addItemDecoration(SpacesItemDecoration(ctx.getResources().getDimensionPixelOffset(com.bilibili.boxing_impl.R.dimen.boxing_media_margin), 3))
                holder?.mediaAdapter = MediaAdapter(ctx, item?.medias!!, this, position)
                holder?.itemBinding?.recyclerviewMedias?.setAdapter(holder?.mediaAdapter)
            }
            (holder?.itemBinding?.recyclerviewMedias?.layoutManager as GridLayoutManager).spanCount = if (item.cataloguePost.type == "video") 1 else 3
            holder?.mediaAdapter?.updateItems(item?.medias!!, this, position)
            holder?.mediaAdapter?.notifyDataSetChanged()

        } else {
            holder?.itemBinding?.recyclerviewMedias?.visibility = View.GONE
        }
    }

    override fun onBindLoadingView(loadingText: TextView) {
        loadingText.setText("Loading...")
    }

    override fun onBindErrorView(errorText: TextView) {
        errorText.setText("Reload")
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
    }


    fun buildComments(holder: ItemViewHolder?, item: CatalogueContainer, position: Int) {
        if (item?.comments?.isNotEmpty()) {
            holder?.itemBinding?.groupComments?.visibility = View.VISIBLE
            holder?.itemBinding?.groupComments?.removeAllViews()
            if (holder?.itemBinding?.groupComments?.childCount == 0) {
                item?.comments?.forEachIndexed { index, it ->
                    if (index < 5) {
                        holder?.itemBinding?.groupComments?.addView(ViewUtils.buildCommentCatalogue(ctx, it.ownerName, it.content))
                    }
                }
            }
        } else {
            holder?.itemBinding?.groupComments?.visibility = View.GONE
        }
    }


    fun buildTags(holder: ItemViewHolder?, item: CatalogueContainer, position: Int) {
        if (item?.cataloguePost?.tags != null && item?.cataloguePost?.tags.isNotEmpty()) {
            holder?.itemBinding?.groupTags?.visibility = View.VISIBLE
            holder?.itemBinding?.groupTags?.removeAllViews()
            if (holder?.itemBinding?.groupTags?.childCount == 0) {
                item?.cataloguePost?.tags?.forEach {
                    holder?.itemBinding?.groupTags?.addView(ViewUtils.buildTagsCatalogue(ctx, it.name))
                }
            }
        } else {
            holder?.itemBinding?.groupTags?.visibility = View.GONE
        }
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemBinding = ItemCatalogueMeBinding.bind(itemView)
        var mediaAdapter: MediaAdapter? = null
    }

    override fun clickMedias(medias: List<Media>?, mediaPosition: Int, cataloguePosition: Int) {
        Log.d("Trong", "mediaPosition " + mediaPosition + " cataloguePosition " + cataloguePosition)
        if (medias != null && medias?.isNotEmpty()) {
            if (medias[0].getType() == Media.TYPE.VIDEO) {
                ctx.startActivity(FullScreenVideoPlayerActivity.getIntent(ctx, (medias[0] as Video).videoUrl))
            } else {
                listener.onImageClick(items.get(cataloguePosition), medias, cataloguePosition, mediaPosition)
            }
        }
    }

    fun <T : RecyclerView.ViewHolder> T.onClick(event: (view: View, position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(it, getAdapterPosition(), getItemViewType())
        }
        return this
    }

    open fun getImageSelected(holder: RecyclerView.ViewHolder, imagePos: Int): View? {
        if (holder is ItemViewHolder) {
            val holderMediaAdapter = holder?.itemBinding?.recyclerviewMedias?.findViewHolderForLayoutPosition(imagePos)
            return if (holderMediaAdapter == null) null else holder?.mediaAdapter?.getImageSelected(holderMediaAdapter)
        }
        return null
    }

}