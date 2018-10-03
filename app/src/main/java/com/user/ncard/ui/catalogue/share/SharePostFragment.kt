package com.user.ncard.ui.card.catalogue.share

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCatalogueSharePostBinding
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.CatalogueFilterEvent
import com.user.ncard.ui.catalogue.share.SharePostViewModel
import com.user.ncard.vo.SharePost
import kotlinx.android.synthetic.main.fragment_catalogue_share_post.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by trong-android-dev on 16/10/17.
 */
class SharePostFragment : BaseFragment() {

    val TAG = "SharePostFragment"

    companion object {
        fun newInstance(): SharePostFragment = SharePostFragment()
    }

    lateinit var viewModel: SharePostViewModel
    lateinit var fragmentBinding: FragmentCatalogueSharePostBinding
    lateinit var sharePostAdapter: SharePostAdapter

    override fun getLayout(): Int {
        return R.layout.fragment_catalogue_share_post
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_share), true)
    }

    override fun initBinding() {

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SharePostViewModel::class.java)
        fragmentBinding = FragmentCatalogueSharePostBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel

        // Do with extra data here
        val bundle = activity.intent.extras
        viewModel.init(bundle.getString("visibility"))
    }

    override fun init() {
        // Load data here
        val items = getListItems()
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        sharePostAdapter = SharePostAdapter(activity, items)
        recyclerview.adapter = sharePostAdapter
    }

    fun getListItems(): ArrayList<SharePost> {

        val item0 = SharePost(0, SharePost.SharePostType.ALL.type, "Visible to all users", false)
        val item1 = SharePost(1, SharePost.SharePostType.PUBLIC.type, "Visible to some users", false)
        val item2 = SharePost(2, SharePost.SharePostType.FRIENDS.type, "Only visible to your friends", false)

        if (viewModel.visibility != null) {

            if (item0.name == viewModel.visibility) {
                item0.selected = true
            } else if (item1.name == viewModel.visibility) {
                item1.selected = true
            } else if (item2.name == viewModel.visibility) {
                item2.selected = true
            }
        }

        return arrayListOf(item0, item1, item2)
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
                    Log.d(TAG, "sharePostAdapter.getVisibility() " + sharePostAdapter.getVisibility())
                    EventBus.getDefault().post(CatalogueFilterEvent(sharePostAdapter.getVisibility(), null, null, null, null))
                    activity.finish()
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

}