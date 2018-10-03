package com.user.ncard.ui.filter

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentFilterBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.IndustryList
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.FilterObject
import com.user.ncard.vo.UserFilter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class FilterFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentFilterBinding
    private var locationSelected: Boolean = true
    private var industrySelected: Boolean = false
    private var genderSelected: Boolean = false
    private var nationalitySelected: Boolean = false
    private lateinit var filterIndustryAdapter: FilterAdapter
    private lateinit var filterLocationAdapter: FilterAdapter
    private lateinit var filterGenderAdapter: FilterAdapter
    private lateinit var filterNationalityAdapter: FilterAdapter
    var filterGender: ArrayList<FilterObject>? = null
    var filterNationality: ArrayList<FilterObject>? = null
    var filterCountry: ArrayList<FilterObject>? = null
    var filterIndustry: ArrayList<FilterObject>? = null
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: FilterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FilterViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        viewModel.getFilterData()
        setupFilterAdaptersData()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_filter, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, context.getColorFromResId(R.color.colorDarkerWhite))
        (activity as AppCompatActivity).setSupportActionBar(viewDataBinding.toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
            val inflaterActionbar = LayoutInflater.from(activity)
            val v = inflaterActionbar.inflate(R.layout.action_bar_layout, null)
            val params = ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER)
            actionBar.setCustomView(v, params)
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).setText(R.string.filter)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }
        setSelectedTextView()
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFilterAdapters()

        initializeRecyclerViews()

        setupTextViewsOnClick()
    }

    private fun setupTextViewsOnClick() {
        viewDataBinding.apply {
            textViewLocation.setOnClickListener {
                locationSelected = true
                industrySelected = false
                genderSelected = false
                nationalitySelected = false
                setSelectedTextView()
            }
            textViewIndustry.setOnClickListener {
                locationSelected = false
                industrySelected = true
                genderSelected = false
                nationalitySelected = false
                setSelectedTextView()
            }
            textViewGender.setOnClickListener {
                locationSelected = false
                industrySelected = false
                genderSelected = true
                nationalitySelected = false
                setSelectedTextView()
            }
            textViewNationality.setOnClickListener {
                locationSelected = false
                industrySelected = false
                genderSelected = false
                nationalitySelected = true
                setSelectedTextView()
            }
        }
    }

    private fun initializeRecyclerViews() {
        viewDataBinding.recyclerViewIndustry.apply {
            adapter = filterIndustryAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
        viewDataBinding.recyclerViewGender.apply {
            adapter = filterGenderAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
        viewDataBinding.recyclerViewLocation.apply {
            adapter = filterLocationAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
        viewDataBinding.recyclerViewNationality.apply {
            adapter = filterNationalityAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun setupFilterAdaptersData() {
        val currentFilter = arguments?.get(ARGUMENT_FILTER) as UserFilter?
        val isLocalFilter = arguments?.getBoolean(ARGUMENT_IS_LOCAL, false) ?: false
        if (isLocalFilter) {
            viewModel.countries.observe(this@FilterFragment, Observer<ArrayList<FilterObject>> { countries ->
                filterCountry = countries ?: ArrayList()
                if (currentFilter != null) {
                    filterCountry?.forEach {
                        if (currentFilter.country.contains(it.name)) it.isChecked = true
                    }
                }
                filterLocationAdapter.replace(filterCountry)
                filterLocationAdapter.notifyDataSetChanged()
                if(countries!=null) reloadCheckMark(currentFilter)
            })
            viewModel.nationalities.observe(this@FilterFragment, Observer<ArrayList<FilterObject>> { nationalities ->
                filterNationality = nationalities ?: ArrayList()
                if (currentFilter != null) {
                    filterNationality?.forEach {
                        if (currentFilter.nationality.contains(it.name)) it.isChecked = true
                    }
                }
                filterNationalityAdapter.replace(filterNationality)
                filterLocationAdapter.notifyDataSetChanged()
                if(nationalities!=null) reloadCheckMark(currentFilter)
            })
            viewModel.industries.observe(this@FilterFragment, Observer<ArrayList<FilterObject>> { industries ->
                filterIndustry = industries ?: ArrayList()
                if (currentFilter != null) {
                    filterIndustry?.forEach {
                        if (currentFilter.industry.contains(it.name)) it.isChecked = true
                    }
                }
                filterIndustryAdapter.replace(filterIndustry)
                filterLocationAdapter.notifyDataSetChanged()
                if(industries!=null) reloadCheckMark(currentFilter)
            })
        } else {
            val locale: Array<Locale> = Locale.getAvailableLocales()
            val countries = ArrayList<String>()
            filterCountry = ArrayList()
            locale.forEach {
                val country = it.displayCountry
                if (country.isNotEmpty() && !countries.contains(country)) {
                    countries.add(country)
                }
            }
            Collections.sort(countries, String.CASE_INSENSITIVE_ORDER)
            countries.forEach {
                if (currentFilter != null && currentFilter.country.contains(it)) {
                    filterCountry?.add(FilterObject(it, true))
                } else filterCountry?.add(FilterObject(it, false))
            }
            filterNationality = ArrayList()
            val nationalities = Utils.nationalityList
            nationalities.forEach {
                if (currentFilter != null && currentFilter.nationality.contains(it)) {
                    filterNationality?.add(FilterObject(it, true))
                } else
                    filterNationality?.add(FilterObject(it, false))
            }

            filterIndustry = ArrayList()
            val industryList = IndustryList.getList()
            industryList.forEach {
                if (currentFilter != null && currentFilter.industry.contains(it)) {
                    filterIndustry?.add(FilterObject(it, true))
                } else
                    filterIndustry?.add(FilterObject(it, false))
            }
            filterLocationAdapter.replace(filterCountry)
            filterNationalityAdapter.replace(filterNationality)
            filterIndustryAdapter.replace(filterIndustry)
        }

        filterGender = ArrayList()
        if (currentFilter != null && currentFilter.gender.contains("male"))
            filterGender?.add(FilterObject("male", true))
        else filterGender?.add(FilterObject("male", false))
        if (currentFilter != null && currentFilter.gender.contains("female"))
            filterGender?.add(FilterObject("female", true))
        else filterGender?.add(FilterObject("female", false))
        filterGenderAdapter.replace(filterGender)

    }

    private fun reloadCheckMark(currentFilter: UserFilter?) {
        if (currentFilter != null) {
        //bo tay bo tay
            if(filterCountry!=null && filterCountry!!.isNotEmpty()){
                for(i in 0 until filterCountry!!.size) {
                    Log.e("NCard", "Called here" + currentFilter.industry + currentFilter.nationality + currentFilter.country)
                    if (currentFilter.industry.contains(filterCountry!![i].name)) {
                        filterCountry!![i].isChecked = true
                        Log.e("NCard", "Called here" + filterCountry!![i].name)
                    }
                }
            }
            filterNationality?.forEach {
                if (currentFilter.nationality.contains(it.name)) {
                    it.isChecked = true
                    Log.e("NCard","Called here"+it.name)
                }
            }
            filterCountry?.forEach {
                if (currentFilter.country.contains(it.name)) {
                    it.isChecked = true
                    Log.e("NCard","Called here"+it.name)
                }
            }
            filterIndustryAdapter.replace2(filterIndustry)
            filterNationalityAdapter.replace2(filterNationality)
            filterLocationAdapter.replace2(filterCountry)
            filterIndustryAdapter.notifyDataSetChanged()
            filterNationalityAdapter.notifyDataSetChanged()
            filterLocationAdapter.notifyDataSetChanged()
            Log.e("NCard","Called here")
        }
    }

    private fun initializeFilterAdapters() {
        filterIndustryAdapter = FilterAdapter(object : FilterAdapter.FilterOnClickCallback {
            override fun onClick(filterObject: FilterObject) {
                filterObject.isChecked = !filterObject.isChecked
            }

        })
        filterLocationAdapter = FilterAdapter(object : FilterAdapter.FilterOnClickCallback {
            override fun onClick(filterObject: FilterObject) {
                filterObject.isChecked = !filterObject.isChecked
            }

        })
        filterNationalityAdapter = FilterAdapter(object : FilterAdapter.FilterOnClickCallback {
            override fun onClick(filterObject: FilterObject) {
                filterObject.isChecked = !filterObject.isChecked
            }

        })
        filterGenderAdapter = FilterAdapter(object : FilterAdapter.FilterOnClickCallback {
            override fun onClick(filterObject: FilterObject) {
                filterObject.isChecked = !filterObject.isChecked
            }

        })
    }

    private fun setSelectedTextView() {
        viewDataBinding.apply {
            textViewGender.isSelected = genderSelected
            textViewIndustry.isSelected = industrySelected
            textViewLocation.isSelected = locationSelected
            textViewNationality.isSelected = nationalitySelected
            recyclerViewLocation.visibility = getVisibility(locationSelected)
            recyclerViewNationality.visibility = getVisibility(nationalitySelected)
            recyclerViewGender.visibility = getVisibility(genderSelected)
            recyclerViewIndustry.visibility = getVisibility(industrySelected)
        }
    }

    private fun getVisibility(selected: Boolean): Int {
        return if (selected) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_filter_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_done -> {
                val returnIntent = Intent()
                val isLocalFilter = arguments?.getBoolean(ARGUMENT_IS_LOCAL, false) ?: false
                if (isLocalFilter) {
                    val localFilter = UserFilter("", ArrayList(), ArrayList(), ArrayList(), ArrayList())
                    val selectedGender = getResultStringList(filterGender!!)

                    if (selectedGender.isEmpty()) returnIntent.putExtra("gender", getAllFilterList(filterGender!!))
                    else {
                        returnIntent.putExtra("gender", selectedGender)
                        localFilter.gender.addAll(selectedGender)
                    }

                    val selectedNationality = getResultStringList(filterNationality!!)
                    if (selectedNationality.isEmpty()) returnIntent.putExtra("nationality", getAllFilterList(filterNationality!!))
                    else {
                        returnIntent.putExtra("nationality", selectedNationality)
                        localFilter.nationality.addAll(selectedNationality)
                    }

                    val selectedIndustry = getResultStringList(filterIndustry!!)
                    if (selectedIndustry.isEmpty()) returnIntent.putExtra("industry", getAllFilterList(filterIndustry!!))
                    else {
                        returnIntent.putExtra("industry", selectedIndustry)
                        localFilter.industry.addAll(selectedIndustry)
                    }

                    val selectedCountry = getResultStringList(filterCountry!!)
                    if (selectedCountry.isEmpty()) returnIntent.putExtra("country", getAllFilterList(filterCountry!!))
                    else {
                        returnIntent.putExtra("country", selectedCountry)
                        localFilter.country.addAll(selectedCountry)
                    }
                    returnIntent.putExtra("localFilter", localFilter)
                } else {
                    returnIntent.putExtra("gender", getResultStringList(filterGender!!))
                    returnIntent.putExtra("nationality", getResultStringList(filterNationality!!))
                    returnIntent.putExtra("industry", getResultStringList(filterIndustry!!))
                    returnIntent.putExtra("country", getResultStringList(filterCountry!!))
                }
                activity.setResult(Activity.RESULT_OK, returnIntent)
                activity.finish()
            }
            R.id.menu_item_clear -> {
                filterGender?.forEach { it.isChecked = false }
                filterGenderAdapter.apply {
                    replace(filterGender)
                    notifyDataSetChanged()
                }
                filterCountry?.forEach { it.isChecked = false }
                filterLocationAdapter.apply {
                    replace(filterCountry)
                    notifyDataSetChanged()
                }
                filterNationality?.forEach { it.isChecked = false }
                filterNationalityAdapter.apply {
                    replace(filterNationality)
                    notifyDataSetChanged()
                }
                filterIndustry?.forEach { it.isChecked = false }
                filterIndustryAdapter.apply {
                    replace(filterIndustry)
                    notifyDataSetChanged()
                }
            }
            android.R.id.home -> {
                activity.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getResultStringList(filter: ArrayList<FilterObject>): ArrayList<String> {
        val option = filter.filter { filterObject -> filterObject.isChecked }
        val resultList = ArrayList<String>()
        option.forEach {
            resultList.add(it.name)
        }
        return resultList
    }

    private fun getAllFilterList(filter: ArrayList<FilterObject>): ArrayList<String> {
        val resultList = ArrayList<String>()
        filter.forEach {
            resultList.add(it.name)
        }
        resultList.add("")
        return resultList
    }

    companion object {
        private const val ARGUMENT_FILTER = "FILTER"
        private const val ARGUMENT_IS_LOCAL = "LOCAL"

        fun newInstance(isLocal: Boolean, filter: UserFilter?) = FilterFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARGUMENT_IS_LOCAL, isLocal)
                putSerializable(ARGUMENT_FILTER, filter)
            }
        }
    }
}