package com.user.ncard.ui.catalogue.category

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCatalogueCategoryPostBinding
import com.user.ncard.databinding.FragmentCatalogueSharePostBinding
import com.user.ncard.ui.card.catalogue.share.SharePostAdapter
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.CatalogueFilterEvent
import com.user.ncard.ui.catalogue.share.SharePostViewModel
import com.user.ncard.vo.CategoryPost
import com.user.ncard.vo.SharePost
import kotlinx.android.synthetic.main.fragment_catalogue_category_post.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by trong-android-dev on 16/10/17.
 */
class CategoryPostFragment : BaseFragment() {

    val TAG = "CategoryPostFragment"

    companion object {
        fun newInstance(): CategoryPostFragment = CategoryPostFragment()
    }

    lateinit var viewModel: CategoryPostViewModel
    lateinit var fragmentBinding: FragmentCatalogueCategoryPostBinding
    lateinit var adapter: CategoryPostAdapter

    override fun getLayout(): Int {
        return R.layout.fragment_catalogue_category_post
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_category), true)
    }

    override fun initBinding() {

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CategoryPostViewModel::class.java)
        fragmentBinding = FragmentCatalogueCategoryPostBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel

        // Do with extra data here
        val bundle = activity.intent.extras
        viewModel.init(bundle.getString("category"))
    }

    override fun init() {
        // Load data here
        val items = getListItems()
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = CategoryPostAdapter(activity, items)
        recyclerview.adapter = adapter
    }

    fun getListItems(): ArrayList<CategoryPost> {

        val item0 = CategoryPost(0, CategoryPost.CategoryPostType.PERSONAL.type, "", false)
        val item1 = CategoryPost(1, CategoryPost.CategoryPostType.BUSINESS.type, "", false)

        if (viewModel.category != null) {

            if (item0.name == viewModel.category) {
                item0.selected = true
            } else if (item1.name == viewModel.category) {
                item1.selected = true
            }
        }

        return arrayListOf(item0, item1)
    }


    override fun onPrepareOptionsMenu(menu: Menu?) {
        val item = menu?.findItem(R.id.menu_item_do)
        item?.setTitle(R.string.done)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_do_sth, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_do -> {
                consume {
                    Log.d(TAG, "adapter.getCategory() " + adapter.getCategory())
                    EventBus.getDefault().post(CatalogueFilterEvent(null, null, adapter.getCategory(), null, null))
                    activity.finish()
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

}