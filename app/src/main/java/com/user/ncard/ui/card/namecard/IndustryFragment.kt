package com.user.ncard.ui.card.namecard

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentNationalityBinding
import com.user.ncard.ui.filter.FilterAdapter
import com.user.ncard.util.IndustryList
import com.user.ncard.util.Utils
import com.user.ncard.vo.FilterObject
import java.util.*

class IndustryFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentNationalityBinding
    private lateinit var filterIndustryAdapter: FilterAdapter
    private lateinit var filterIndustry: ArrayList<FilterObject>
    private var selectedIndustry = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_nationality, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.select_industry)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterIndustryAdapter = FilterAdapter(object : FilterAdapter.FilterOnClickCallback {
            override fun onClick(filterObject: FilterObject) {
                filterIndustry.forEach { it.isChecked = false }
                filterIndustryAdapter.apply {
                    replace(filterIndustry)
                    notifyDataSetChanged()
                }
                filterObject.isChecked = !filterObject.isChecked
                selectedIndustry = filterObject.name
            }
        })
        filterIndustry = ArrayList()
        val industryList = IndustryList.getList()
        industryList.forEach {
            filterIndustry.add(FilterObject(it, false))
        }
        filterIndustryAdapter.replace(filterIndustry)
        viewDataBinding.recyclerViewNationality.apply {
            adapter = filterIndustryAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
        filterIndustryAdapter.replace(filterIndustry)

        viewDataBinding.searchView.setOnQueryChangeListener { _, newQuery ->
            filterIndustryAdapter.replace(filterIndustry.filter { filterObject ->
                filterObject.name.toLowerCase().contains(newQuery.toLowerCase())
            })
            filterIndustryAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_done, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_done -> {
                val returnIntent = Intent()
                returnIntent.putExtra("industry", selectedIndustry)
                activity.setResult(Activity.RESULT_OK, returnIntent)
                activity.finish()
            }
            android.R.id.home -> {
                val returnIntent = Intent()
                activity.setResult(Activity.RESULT_CANCELED, returnIntent)
                activity.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}