package com.user.ncard.ui.catalogue.my

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.alexvasilkov.gestures.animation.ViewPositionAnimator
import com.alexvasilkov.gestures.commons.DepthPageTransformer
import com.alexvasilkov.gestures.transition.GestureTransitions
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator
import com.alexvasilkov.gestures.transition.tracker.FromTracker
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker
import com.bumptech.glide.Glide
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCatalogueMeBinding
import com.user.ncard.ui.card.catalogue.main.CatalogueAdapter
import com.user.ncard.ui.card.catalogue.main.CatalogueMainActivity
import com.user.ncard.ui.card.catalogue.post.CataloguePostActivity
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.detail.CatalogueDetailActivity
import com.user.ncard.ui.catalogue.main.Image
import com.user.ncard.ui.catalogue.main.Media
import com.user.ncard.ui.catalogue.main.Video
import com.user.ncard.ui.catalogue.mediaviewer.ImagesPagerAdapter
import com.user.ncard.ui.catalogue.utils.EndlessRecyclerAdapter
import com.user.ncard.ui.catalogue.utils.GlideHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.CatalogueContainer
import com.user.ncard.vo.CategoryPost
import com.user.ncard.vo.SharePost
import com.user.ncard.vo.Status
import kotlinx.android.synthetic.main.fragment_catalogue_me.*
import kotlinx.android.synthetic.main.layout_images_pager.*
import kotlinx.android.synthetic.main.view_catalogue_me_header.*
import javax.inject.Inject

/**
 * Created by trong-android-dev on 16/10/17.
 */
class CatalogueMeFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener,
        ViewPositionAnimator.PositionUpdateListener, CatalogueAdapter.OnImageClickListener {


    companion object {
        fun newInstance(): CatalogueMeFragment = CatalogueMeFragment()
    }

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper
    lateinit var viewModel: CatalogueMeViewModel
    lateinit var fragmentBinding: FragmentCatalogueMeBinding
    lateinit var adapter: CatalogueMeAdapter

    var category: String? = null

    override fun getLayout(): Int {
        return R.layout.fragment_catalogue_me
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //initToolbar(getString(R.string.title_catalogue_my), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CatalogueMeViewModel::class.java)
        fragmentBinding = FragmentCatalogueMeBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        category = bundle?.getString("category")
        viewModel.initData(category!!)
        if (category == CategoryPost.CategoryPostType.BUSINESS.type) {
            initToolbar(getString(R.string.title_catalogue_business_my), true)
        } else if (category == CategoryPost.CategoryPostType.PERSONAL.type) {
            initToolbar(getString(R.string.title_catalogue_personal_my), true)
        }
    }

    override fun init() {
        // Load data here
        initObser()
        initAdapter()
        iniSwipeRefreshLayout()
        initImagesPager()
        initAnimator()

        viewModel.setUsername(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL))
        viewModel.start.value = true

        // EventBus.getDefault().register(this)

    }

    fun initAdapter() {
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = CatalogueMeAdapter(activity, viewModel.getItems(), this)
        recyclerview.adapter = adapter

        adapter.setLoadingOffset(1)
        adapter.setCallbacks(object : EndlessRecyclerAdapter.LoaderCallbacks {
            override fun canLoadNextItems(): Boolean {
                return viewModel.canLoadMore()
            }

            override fun loadNextItems() {
                viewModel.loadMore()
            }

        })
        recyclerview.adapter = adapter
    }

    fun iniSwipeRefreshLayout() {
        fragmentBinding.srl.isEnabled = true
        fragmentBinding.srl.setOnRefreshListener(this)
    }

    fun initObser() {
        viewModel.catalogues.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                // Toast.makeText(activity, "Status.LOADING", Toast.LENGTH_SHORT).show()
            } else if (it?.status == Status.SUCCESS) {
                // Toast.makeText(activity, "Status.SUCCESS", Toast.LENGTH_SHORT).show()
                if (it?.pagination != null) {
                    viewModel.pagination = it?.pagination
                }
                loadingFinish()
                adapter.onNextItemsLoaded()
                notifyAdapterChange()
            } else if (it?.status == Status.ERROR) {
                // Toast.makeText(activity, "Status.ERROR " + it?.message, Toast.LENGTH_SHORT).show()
                loadingFinish()
                if (viewModel.page > viewModel.DEFAULT_PAGE) {
                    viewModel.page--
                }
                adapter.onNextItemsError()
                if (it?.data?.isNotEmpty()!!) {
                    notifyAdapterChange()
                }
                showSnackbarMessage(it?.message)
            }

        })

        viewModel.user.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
            } else if (it?.status == Status.SUCCESS) {
                viewModel.name.set(getString(R.string.display_name,
                        if (it?.data?.firstName.isNullOrEmpty()) "" else it?.data?.firstName,
                        if (it?.data?.lastName.isNullOrEmpty()) "" else it?.data?.lastName))
                /*viewModel.avatarUrl.set(it?.data?.thumbnailUrl)
                viewModel.coverUrl.set(it?.data?.profileImageUrl)*/

                GlideHelper.displaySquareAvatar(imvAvtar, it?.data?.thumbnailUrl);
                if (it?.data?.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(imvCover.getContext()).load(R.drawable.bg_cover_user);
                } else {
                    Glide.with(imvCover.getContext()).load(it?.data?.profileImageUrl);
                }

            } else if (it?.status == Status.ERROR) {
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
        viewModel.getItems().forEach { item ->
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
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_add_post, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_do -> {
                consume {
                    val share = if (category == CategoryPost.CategoryPostType.BUSINESS.type) SharePost.SharePostType.ALL.type else SharePost.SharePostType.FRIENDS.type
                    startActivity(CataloguePostActivity.getIntent(activity, category, share))
                }
            }
            R.id.menu_item_me -> {
                consume {
                    //startActivity(CatalogueMeActivity.getIntent(activity, category))
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // EventBus.getDefault().unregister(this)
    }


    /*Functions: Show images*/
    override fun onImageClick(item: CatalogueContainer, medias: List<Media>?, itemPos: Int, imagePos: Int) {
        catalogueSelectedPosition = itemPos
        imageSelectedPosition = imagePos

        imagesPagerAdapter.setImages(medias)
        imagesPagerAdapter.setActivated(true)

        animator.enter(imagePos, true)
    }

    override fun onItemClick(item: CatalogueContainer, position: Int) {
        startActivity(CatalogueDetailActivity.getIntent(activity, item.cataloguePost.id))
    }

    override fun onLikeClick(item: CatalogueContainer, position: Int) {
        viewModel.like(item)

        viewModel.catalogue.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
            } else if (it?.status == Status.SUCCESS) {
            } else if (it?.status == Status.ERROR) {
            }
        })
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