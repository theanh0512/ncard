package com.user.ncard.ui.card.catalogue.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import com.alexvasilkov.gestures.animation.ViewPositionAnimator
import com.alexvasilkov.gestures.commons.DepthPageTransformer
import com.alexvasilkov.gestures.transition.GestureTransitions
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator
import com.alexvasilkov.gestures.transition.tracker.FromTracker
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker
import com.barryzhang.temptyview.TViewUtil
import com.google.gson.Gson
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCatalogueMainBinding
import com.user.ncard.ui.card.catalogue.post.CataloguePostActivity
import com.user.ncard.ui.card.catalogue.share.SharePostActivity
import com.user.ncard.ui.card.catalogue.tag.TagPostActivity
import com.user.ncard.ui.card.catalogue.tag.TagPostFragment
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.CatalogueFilterEvent
import com.user.ncard.ui.catalogue.category.CategoryPostActivity
import com.user.ncard.ui.catalogue.detail.CatalogueDetailActivity
import com.user.ncard.ui.catalogue.main.CatalogueMainViewModel
import com.user.ncard.ui.catalogue.main.Image
import com.user.ncard.ui.catalogue.main.Media
import com.user.ncard.ui.catalogue.main.Video
import com.user.ncard.ui.catalogue.mediaviewer.ImagesPagerAdapter
import com.user.ncard.ui.catalogue.my.CatalogueMeActivity
import com.user.ncard.ui.catalogue.utils.EndlessRecyclerAdapter
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.vo.CatalogueContainer
import com.user.ncard.vo.CategoryPost
import com.user.ncard.vo.SharePost
import com.user.ncard.vo.Status
import kotlinx.android.synthetic.main.fragment_catalogue_main.*
import kotlinx.android.synthetic.main.layout_images_pager.*
import kotlinx.android.synthetic.main.view_catalogue_filter.*
import kotlinx.android.synthetic.main.view_search.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by trong-android-dev on 16/10/17.
 */
class CatalogueMainFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener, ViewPositionAnimator.PositionUpdateListener, CatalogueAdapter.OnImageClickListener {

    companion object {
        fun newInstance(): CatalogueMainFragment = CatalogueMainFragment()
    }

    lateinit var viewModel: CatalogueMainViewModel
    lateinit var fragmentBinding: FragmentCatalogueMainBinding
    lateinit var adapter: CatalogueAdapter

    var listenClickFilter = false
    var category: String? = null

    override fun getLayout(): Int {
        return R.layout.fragment_catalogue_main
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //initToolbar(getString(R.string.title_catalogue_main), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CatalogueMainViewModel::class.java)
        fragmentBinding = FragmentCatalogueMainBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        category = bundle?.getString("category")
        viewModel.initData(category!!)
        if (category == CategoryPost.CategoryPostType.BUSINESS.type) {
            initToolbar(getString(R.string.title_catalogue_business), true)
        } else if (category == CategoryPost.CategoryPostType.PERSONAL.type) {
            initToolbar(getString(R.string.title_catalogue_personal), true)
        }
    }

    override fun init() {
        // Load data here
        initObser()
        initAdapter()
        iniSwipeRefreshLayout()
        initImagesPager()
        initAnimator()

        fragmentBinding.srl.post {
            fragmentBinding.srl.isRefreshing = true
            viewModel.start.value = true
        }

        EventBus.getDefault().register(this)

        viewShare.setOnClickListener({
            listenClickFilter = true
            startActivity(SharePostActivity.getIntent(activity, viewModel.filter?.visibility!!))
        })

        viewTags.setOnClickListener({
            listenClickFilter = true
            startActivity(TagPostActivity.getIntent(activity, Gson().toJson(viewModel.filter?.tags)))
        })

        viewCategory.setOnClickListener({
            listenClickFilter = true
            startActivity(CategoryPostActivity.getIntent(activity, category))
        })

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                if (searchView.getQuery().isEmpty()) {
                    search(query)
                }
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                // Search here
                search(query)
                return false
            }
        })
        /*val closeButton = searchView?.findViewById<ImageView>(R.id.search_close_btn)
        closeButton?.setOnClickListener({
            if (searchView.getQuery().isEmpty()) {
                search("")
            }
        })*/
    }

    fun search(query: String?) {
        listenClickFilter = true
        onFilterEvent(CatalogueFilterEvent(null, null, null, query, null))
        Functions.hideSoftKeyboard(activity)
    }

    fun initAdapter() {
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = CatalogueAdapter(activity, viewModel.getItems(), CatalogueAdapter.TYPE_ALL, this)

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

        /*recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = recyclerView!!.layoutManager.childCount
                val totalItemCount = recyclerView.layoutManager.itemCount
                val pastVisiblesItems = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                    if (!viewModel?.isLoading && viewModel.canLoadMore()) {
                        viewModel?.isLoading = true
                        // footerAdapter.clear()
                        // footerAdapter.add(ProgressItem().withEnabled(false))
                        viewModel.loadMore()
                    }
                }
            }
        })*/
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
        TViewUtil.EmptyViewBuilder.getInstance(activity)
                .bindView(recyclerview)
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onFilterEvent(filterEvent: CatalogueFilterEvent) {
        if (listenClickFilter) {
            viewModel.initFilter(filterEvent)
        }
        listenClickFilter = false
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
                    startActivity(CatalogueMeActivity.getIntent(activity, category))
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onItemClick(item: CatalogueContainer, position: Int) {
        startActivity(CatalogueDetailActivity.getIntent(activity, item.cataloguePost.id))
    }

    override fun onLikeClick(item: CatalogueContainer, position: Int) {
        viewModel.like(item)

        viewModel.catalogue.observe(this, android.arch.lifecycle.Observer {
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