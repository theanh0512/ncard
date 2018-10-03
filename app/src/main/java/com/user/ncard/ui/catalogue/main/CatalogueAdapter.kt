package com.user.ncard.ui.card.catalogue.main

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bilibili.boxing_impl.view.SpacesItemDecoration
import com.bumptech.glide.Glide
import com.user.ncard.R
import com.user.ncard.databinding.ItemCatalogueDetailBinding
import com.user.ncard.ui.card.catalogue.ViewUtils
import com.user.ncard.ui.catalogue.views.RelativeTimeTextView
import com.user.ncard.ui.catalogue.main.Media
import com.user.ncard.ui.catalogue.main.MediaAdapter
import com.user.ncard.ui.catalogue.main.Video
import com.user.ncard.ui.catalogue.mediaviewer.FullScreenVideoPlayerActivity
import com.user.ncard.ui.catalogue.utils.DefaultEndlessRecyclerAdapter
import com.user.ncard.ui.catalogue.utils.RecyclerAdapterHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.CatalogueContainer
import kotlinx.android.synthetic.main.fragment_catalogue_detail.*
import javax.inject.Inject


/**
 * Created by Concaro on 7/17/2017.
 */
class CatalogueAdapter(val ctx: Context,
                       var items: List<CatalogueContainer>,
                       var type: Int,
                       val listener: OnImageClickListener) : DefaultEndlessRecyclerAdapter<CatalogueAdapter.ItemViewHolder>(),
        MediaAdapter.ClickListener {

    var sharedPreferenceHelper: SharedPreferenceHelper

    init {
        sharedPreferenceHelper = SharedPreferenceHelper(ctx)
    }

    companion object {
        const val TYPE_ALL = 0
        const val TYPE_DETAIL = 1
        var listener: OnImageClickListener? = null
    }

    fun updateItems(items: List<CatalogueContainer>) {
        if (items == null) {
            return
        }
        this.items = items
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_catalogue_detail, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindHolder(holder: ItemViewHolder?, position: Int) {
        val holder = holder as ItemViewHolder
        val item = items.get(position)
        holder?.itemBinding?.item = item
        holder?.itemBinding?.sharedPreferenceHelper = sharedPreferenceHelper
        // Process here
        buildMedias(holder, item, position)
        buildTags(holder, item, position)
        buildComments(holder, item, position)
        if (item.likes?.isNotEmpty()) {
            holder?.itemBinding?.viewLikes?.visibility = View.VISIBLE
            if(item?.comments?.isNotEmpty()) {
                holder?.itemBinding?.tvSpace?.visibility = View.VISIBLE
            } else {
                holder?.itemBinding?.tvSpace?.visibility = View.GONE
            }
        } else {
            holder?.itemBinding?.viewLikes?.visibility = View.GONE
            holder?.itemBinding?.tvSpace?.visibility = View.GONE
        }
        (holder?.itemBinding?.tvTime as RelativeTimeTextView).setReferenceTime(item.cataloguePost.createdAt)
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
                ViewCompat.setNestedScrollingEnabled(holder?.itemBinding?.recyclerviewMedias!!, false)
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

    fun buildComments(holder: ItemViewHolder?, item: CatalogueContainer, position: Int) {
        if (item?.comments?.isNotEmpty() && type == TYPE_ALL) {
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
            if (holder?.itemBinding?.groupTags?.childCount == 0 && item?.cataloguePost?.tags?.size > 0) {
                item?.cataloguePost?.tags?.forEach {
                    holder?.itemBinding?.groupTags?.addView(ViewUtils.buildTagsCatalogue(ctx, if(it != null) it.name else ""))
                }
            }
        } else {
            holder?.itemBinding?.groupTags?.visibility = View.GONE
        }
    }

    /*fun updateComments(holder: ItemViewHolder?, position: Int) {

        val numberChild = holder?.itemBinding?.groupComments?.childCount?.minus(1)
        for (i in 0..numberChild!!) {
            val child = holder?.itemBinding?.groupComments?.getChildAt(i) as ViewGroup
            ((child.getChildAt(0) as LinearLayout).getChildAt(0) as TextView).text = "Text changed " + i
        }

    }*/

    /*fun updateTags(holder: ItemViewHolder?, item: CatalogueContainer, position: Int) {
        if (item?.cataloguePost?.tags != null && item?.cataloguePost?.tags.isNotEmpty()) {
            val numberChild = holder?.itemBinding?.groupTags?.childCount?.minus(1)
            for (i in 0..numberChild!!) {
                val child = holder?.itemBinding?.groupTags?.getChildAt(i) as ViewGroup
                (child.getChildAt(0) as TextView).text = item?.cataloguePost?.tags[i].name
            }
        }
    }*/

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemBinding = ItemCatalogueDetailBinding.bind(itemView)
        var mediaAdapter: MediaAdapter? = null
    }

    override fun clickMedias(medias: List<Media>?, mediaPosition: Int, cataloguePosition: Int) {
        // Log.d("Trong", "mediaPosition " + mediaPosition + " cataloguePosition " + cataloguePosition)
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

    open interface OnImageClickListener {
        fun onImageClick(item: CatalogueContainer, medias: List<Media>?, itemPos: Int, imagePos: Int)

        fun onItemClick(item: CatalogueContainer, position: Int)

        fun onLikeClick(item: CatalogueContainer, position: Int)
    }

}