package com.user.ncard.ui.catalogue.detail

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.alexvasilkov.gestures.animation.ViewPositionAnimator
import com.alexvasilkov.gestures.commons.DepthPageTransformer
import com.alexvasilkov.gestures.transition.GestureTransitions
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator
import com.alexvasilkov.gestures.transition.tracker.FromTracker
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCatalogueDetailBinding
import com.user.ncard.ui.card.catalogue.main.CatalogueAdapter
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.RequestLike
import com.user.ncard.ui.catalogue.main.Image
import com.user.ncard.ui.catalogue.main.Media
import com.user.ncard.ui.catalogue.main.Video
import com.user.ncard.ui.catalogue.mediaviewer.ImagesPagerAdapter
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.CatalogueContainer
import com.user.ncard.vo.Status
import kotlinx.android.synthetic.main.fragment_catalogue_detail.*
import kotlinx.android.synthetic.main.layout_images_pager.*

/**
 * Created by trong-android-dev on 16/10/17.
 */
class CatalogueDetailFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener, ViewPositionAnimator.PositionUpdateListener, CatalogueAdapter.OnImageClickListener {

    companion object {
        fun newInstance(): CatalogueDetailFragment = CatalogueDetailFragment()
    }

    lateinit var viewModel: CatalogueDetailViewModel
    lateinit var fragmentBinding: FragmentCatalogueDetailBinding
    lateinit var adapter: CatalogueAdapter
    lateinit var adapterComment: CommentPostAdapter

    override fun getLayout(): Int {
        return R.layout.fragment_catalogue_detail
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_detail), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CatalogueDetailViewModel::class.java)
        fragmentBinding = FragmentCatalogueDetailBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        viewModel.init(bundle?.getInt("postId"))
    }

    override fun init() {
        // Load data here
        initObser()
        initAdapter()
        iniSwipeRefreshLayout()
        initImagesPager()
        initAnimator()
        initComment()

        viewModel.postId.value = viewModel.id

        // EventBus.getDefault().register(this)

    }

    fun initAdapter() {
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = CatalogueAdapter(activity, viewModel.getItems(), CatalogueAdapter.TYPE_DETAIL, this)
        recyclerview.adapter = adapter


        recyclerviewComment.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerviewComment.setHasFixedSize(true)
        adapterComment = CommentPostAdapter(activity, viewModel.getCommentItems())
        recyclerviewComment.adapter = adapterComment

        ViewCompat.setNestedScrollingEnabled(recyclerview, false)
        ViewCompat.setNestedScrollingEnabled(recyclerviewComment, false)
    }

    fun iniSwipeRefreshLayout() {
        fragmentBinding.srl.setColorSchemeResources(R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue)
        fragmentBinding.srl.isEnabled = true
        fragmentBinding.srl.setOnRefreshListener(this)
    }

    fun initObser() {
        viewModel.catalogue.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                // Toast.makeText(activity, "Status.LOADING", Toast.LENGTH_SHORT).show()
            } else if (it?.status == Status.SUCCESS) {
                // Toast.makeText(activity, "Status.SUCCESS", Toast.LENGTH_SHORT).show()
                notifyAdapterChange()
                loadingFinish()
                activity.invalidateOptionsMenu()
            } else if (it?.status == Status.ERROR) {
                // Toast.makeText(activity, "Status.ERROR " + it?.message, Toast.LENGTH_SHORT).show()
                if(it?.data != null) {
                    notifyAdapterChange()
                }
                loadingFinish()
                showSnackbarMessage(it?.message)
            }

        })

        viewModel.deleteCatalogue.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                Toast.makeText(activity, it.data?.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
                activity.finish()
            } else if (it?.status == Status.ERROR) {
                Toast.makeText(activity, it?.message, Toast.LENGTH_LONG).show()
                showSnackbarMessage(it?.message)
            }
        })
    }

    fun initComment() {
        fragmentBinding?.btnSendComment?.setOnClickListener({
            viewModel.comment(fragmentBinding?.edtComment.text.toString())

            viewModel.catalogueComment.observe(this, android.arch.lifecycle.Observer {
                if (it?.status == Status.LOADING) {
                    showProgressDialog()
                } else if (it?.status == Status.SUCCESS) {
                    fragmentBinding?.edtComment?.setText("", TextView.BufferType.EDITABLE)
                    Functions.hideSoftKeyboard(activity)
                    fragmentBinding?.scrollView?.postDelayed({
                        fragmentBinding?.scrollView?.fullScroll(ScrollView.FOCUS_DOWN);
                    }, 200)
                    hideProgressDialog()
                } else if (it?.status == Status.ERROR) {
                    showSnackbarMessage(it?.message)
                }
            })
        })
        fragmentBinding?.edtComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                fragmentBinding?.btnSendComment.isEnabled = !s.toString().isNullOrEmpty()
                if (!s.toString().isNullOrEmpty()) {
                    DrawableCompat.setTint(fragmentBinding?.btnSendComment.getDrawable(), fragmentBinding?.btnSendComment.context.getResources().getColor(R.color.colorDarkBlue))
                } else {
                    DrawableCompat.setTint(fragmentBinding?.btnSendComment.getDrawable(), fragmentBinding?.btnSendComment.context.getResources().getColor(R.color.colorGrey))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    fun loadingFinish() {
        viewModel.forceLoad = false
        viewModel.isLoading = false
        viewModel.refresh = false;
        fragmentBinding.srl.isRefreshing = false
        hideProgressDialog()
    }

    fun notifyAdapterChange() {
        viewModel.getItems()?.forEach { item ->
            if (item.cataloguePost?.type != "text" && item.medias == null) {
                val medias: MutableList<Media> = ArrayList()
                if (item.cataloguePost?.type == "photo") {
                    item.cataloguePost?.photoUrls?.isNotEmpty().let {
                        item.cataloguePost?.photoUrls?.forEach {
                            medias.add(Image(it))
                        }
                    }

                } else if (item.cataloguePost.type == "video") {
                    if (!item?.cataloguePost?.videoThumbnailUrl.isNullOrEmpty()) {
                        medias.add(Video(item?.cataloguePost?.videoThumbnailUrl, item?.cataloguePost?.videoUrl!!))
                    }
                }
                item.medias = medias
            }
        }
        adapter.updateItems(viewModel.getItems())
        adapter.notifyDataSetChanged()
        adapterComment.updateItems(viewModel.getCommentItems())
        adapterComment.notifyDataSetChanged()
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val itemEdit = menu?.findItem(R.id.menu_action_edit)
        val itemDelete = menu?.findItem(R.id.menu_action_delete)
        val visibility = viewModel?.catalogue?.value?.data?.cataloguePost?.owner?.id ==
                viewModel?.sharedPreferenceHelper?.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
        itemEdit?.isVisible = false
        itemDelete?.isVisible = visibility
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_catalogue_detail, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_action_edit -> {
                consume {

                }
            }
            R.id.menu_action_delete -> {
                consume {
                    viewModel.delete.value = true
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // EventBus.getDefault().unregister(this)
    }

    override fun onItemClick(item: CatalogueContainer, position: Int) {
    }

    override fun onLikeClick(item: CatalogueContainer, position: Int) {
        viewModel.like(item)

        viewModel.catalogueLike.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                hideProgressDialog()
            } else if (it?.status == Status.ERROR) {
                showSnackbarMessage(it?.message)
            }
        })
    }

    /*Functions: Show images*/
    override fun onImageClick(item: CatalogueContainer, medias: List<Media>?, itemPos: Int, imagePos: Int) {
        catalogueSelectedPosition = itemPos
        imageSelectedPosition = imagePos

        imagesPagerAdapter.setImages(medias)
        imagesPagerAdapter.setActivated(true)

        animator.enter(imagePos, true)
    }

    lateinit var imagesPagerAdapter: ImagesPagerAdapter
    lateinit var animator: ViewsTransitionAnimator<Int>
    var catalogueSelectedPosition = FromTracker.NO_POSITION
    var imageSelectedPosition = FromTracker.NO_POSITION

    fun initImagesPager() {
        imagesPagerAdapter = ImagesPagerAdapter(imagesViewPager, arrayListOf())

        imagesViewPager?.adapter = imagesPagerAdapter
        imagesViewPager?.setPageTransformer(true, DepthPageTransformer())
        imagesToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        imagesToolbar?.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })
    }

    private fun initAnimator() {
        val listTracker = object : FromTracker<Int> {
            override fun getViewById(imagePos: Int): View? {
                val holder = recyclerview.findViewHolderForLayoutPosition(catalogueSelectedPosition)
                return if (holder == null) null else adapter.getImageSelected(holder, imagePos)
            }

            override fun getPositionById(imagePos: Int): Int {
                val hasHolder = recyclerview.findViewHolderForLayoutPosition(catalogueSelectedPosition) != null
                return if (!hasHolder || getViewById(imagePos) != null)
                    catalogueSelectedPosition
                else
                    FromTracker.NO_POSITION
            }
        }

        val pagerTracker = object : SimpleTracker() {
            public override fun getViewAt(pos: Int): View? {
                val holder = imagesPagerAdapter.getViewHolder(pos)
                return if (holder == null) null else ImagesPagerAdapter.getImage(holder)
            }
        }

        animator = GestureTransitions.from<Int>(recyclerview, listTracker).into(imagesViewPager, pagerTracker)
        animator.addPositionUpdateListener(this)
    }

    override fun onPositionUpdate(position: Float, isLeaving: Boolean) {
        imagesBackground.setVisibility(if (position == 0f) View.INVISIBLE else View.VISIBLE)
        imagesBackground.getBackground().setAlpha((255 * position).toInt())

        imagesToolbar.setVisibility(if (position == 0f) View.INVISIBLE else View.VISIBLE)
        imagesToolbar.setAlpha(position)

        if (isLeaving && position == 0f) {
            imagesPagerAdapter.setActivated(false)
        }
    }

    open fun onBackPressed() {
        if (!animator?.isLeaving()!!) {
            animator?.exit(true)
        } else {
            activity.finish()
        }
    }
    /*Functions: Show images*/


}