package com.user.ncard.ui.card.catalogue.tag

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCatalogueTagPostBinding
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.CatalogueFilterEvent
import com.user.ncard.ui.catalogue.tag.TagPostViewModel
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.vo.Status
import com.user.ncard.vo.TagPost
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.view_search.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by trong-android-dev on 16/10/17.
 */
class TagPostFragment : BaseFragment() {

    companion object {
        fun newInstance(): TagPostFragment = TagPostFragment()
    }

    lateinit var sectionAdapter: SectionedRecyclerViewAdapter
    lateinit var viewModel: TagPostViewModel
    lateinit var fragmentBinding: FragmentCatalogueTagPostBinding

    override fun getLayout(): Int {
        return R.layout.fragment_catalogue_tag_post
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_tags), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TagPostViewModel::class.java)
        fragmentBinding = FragmentCatalogueTagPostBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel

        // Do with extra data here
        val bundle = activity.intent.extras
        val strTags = bundle.getString("tags")
        if (strTags != null) {
            viewModel.init(Gson().fromJson(
                    strTags,
                    object : TypeToken<List<String>>() {}.type
            ))
        }
    }

    override fun init() {
        // Load data here

        iniSwipeRefreshLayout()
        initObser()
        initAdapter()

        fragmentBinding.srl.post {
            fragmentBinding.srl.isRefreshing = true
            viewModel.start.value = true
        }

        ViewCompat.setNestedScrollingEnabled(fragmentBinding.recyclerview, false)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                for (section in sectionAdapter.sectionsMap.values) {
                    if (section is FilterableSection) {
                        (section as FilterableSection).filter(newText)
                    }
                }
                sectionAdapter.notifyDataSetChanged()
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                Functions.hideSoftKeyboard(activity)
                return false
            }
        })
    }

    fun iniSwipeRefreshLayout() {
        fragmentBinding.srl.setColorSchemeResources(R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue)
        fragmentBinding.srl.isEnabled = false
    }

    fun initAdapter() {
        fragmentBinding.recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        fragmentBinding.recyclerview.setHasFixedSize(true)

        sectionAdapter = SectionedRecyclerViewAdapter()
        fragmentBinding.recyclerview.adapter = sectionAdapter
    }

    fun initObser() {
        viewModel.tags.observe(this, Observer {
            if (it?.status == Status.LOADING) {
            } else if (it?.status == Status.SUCCESS) {
                it?.data?.sortedWith(compareBy({ it.tagGroupId }, { it.id }))
                viewModel.initTagsSelected()
                notifyAdapterChange()
            } else if (it?.status == Status.ERROR) {
                if (it?.data?.isNotEmpty()!!) {
                    notifyAdapterChange()
                }
                showSnackbarMessage(it?.message)
            }
        })

        viewModel.tagGroups.observe(this, Observer {
            it?.sortedWith(compareBy({ it.id }))
            notifyAdapterChange()
        })
    }

    fun notifyAdapterChange() {
        fragmentBinding.srl.isRefreshing = false
        sectionAdapter.removeAllSections()
        viewModel?.tagGroups?.value?.forEach { group ->
            val filterTagByGroup = filterTagByGroup(group.id)
            val name = group.name
            if (filterTagByGroup != null && filterTagByGroup.isNotEmpty()) {
                sectionAdapter.addSection(TagPostAdapter(activity, sectionAdapter, name, filterTagByGroup))
            }
        }
        sectionAdapter.notifyDataSetChanged()
    }

    fun filterTagByGroup(groupId: Int): List<TagPost>? {
        return viewModel?.tags?.value?.data?.filter { it.tagGroupId == groupId }
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
                    val tagPostIds = ArrayList<String>()
                    sectionAdapter.sectionsMap.forEach {
                        if ((it.value as TagPostAdapter).getTagPostIds().isNotEmpty()) {
                            tagPostIds.addAll((it.value as TagPostAdapter).getTagPostIds())
                        }
                    }
                    Log.d("Trong", " " + tagPostIds.toString())
                    EventBus.getDefault().post(CatalogueFilterEvent(null, tagPostIds, null, null, null))
                    activity.finish()
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    open interface FilterableSection {
        fun filter(query: String?)
    }
}