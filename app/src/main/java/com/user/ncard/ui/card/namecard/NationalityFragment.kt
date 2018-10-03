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
import com.user.ncard.util.Utils
import com.user.ncard.vo.FilterObject
import java.util.*

class NationalityFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentNationalityBinding
    private lateinit var filterNationalityAdapter: FilterAdapter
    private lateinit var filterNationality: ArrayList<FilterObject>
    private var selectedNationality = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_nationality, container, false)!!
        if (arguments.getBoolean(ARGUMENT_IS_NATIONALITY))
            Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.select_nationality)
        else Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.select_country)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterNationalityAdapter = FilterAdapter(object : FilterAdapter.FilterOnClickCallback {
            override fun onClick(filterObject: FilterObject) {
                filterNationality.forEach { it.isChecked = false }
                filterNationalityAdapter.apply {
                    replace(filterNationality)
                    notifyDataSetChanged()
                }
                filterObject.isChecked = !filterObject.isChecked
                selectedNationality = filterObject.name
            }
        })
        filterNationality = ArrayList()
        val locale: Array<Locale> = Locale.getAvailableLocales()
        val countries = ArrayList<String>()
        locale.forEach {
            val country = it.displayCountry
            if (country.isNotEmpty() && !countries.contains(country)) {
                countries.add(country)
            }
        }
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER)
        val nationalities = Utils.nationalityList
        if(arguments.getBoolean(ARGUMENT_IS_NATIONALITY)){
            nationalities.forEach {
                filterNationality.add(FilterObject(it, false))
            }
        }
        else {
            countries.forEach {
                filterNationality.add(FilterObject(it, false))
            }
        }
        viewDataBinding.recyclerViewNationality.apply {
            adapter = filterNationalityAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
        filterNationalityAdapter.replace(filterNationality)

        viewDataBinding.searchView.setOnQueryChangeListener { _, newQuery ->
            filterNationalityAdapter.replace(filterNationality.filter { filterObject ->
                filterObject.name.toLowerCase().contains(newQuery.toLowerCase())
            })
            filterNationalityAdapter.notifyDataSetChanged()
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
                if (arguments.getBoolean(ARGUMENT_IS_NATIONALITY))
                    returnIntent.putExtra("nationality", selectedNationality)
                else returnIntent.putExtra("country", selectedNationality)
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

    companion object {
        private const val ARGUMENT_IS_NATIONALITY = "IS_NATIONALITY"

        fun newInstance(isNationality: Boolean) = NationalityFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARGUMENT_IS_NATIONALITY, isNationality)
            }
        }
    }
}