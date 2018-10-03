package com.user.ncard.ui.catalogue

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.FragmentListener
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.ProgressLoading
import javax.inject.Inject

/**
 * Created by trong-android-dev on 16/10/17.
 */
abstract class BaseFragment : Fragment(), Injectable, FragmentListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var rootView: View
    lateinit var toolBar: Toolbar

    lateinit var progressLoading: ProgressLoading

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        initInjection()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(getLayout(), container, false)
        rootView = view as View
        return view;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        progressLoading = ProgressLoading(context)
        initBinding()
        init()
        Functions.hideSoftKeyboard(activity)
    }

    abstract fun getLayout(): Int

    abstract fun initBinding()

    open fun initInjection() {
        // Inject everything here
    }

    abstract fun init()

    open fun initToolbar(title: String, hasBackButton: Boolean) {
        // Init toolbar of fragment here
        toolBar = activity.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        supportActionBar?.title = title
        if (hasBackButton) {
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                consume { activity.finish() }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }

    override fun showProgressDialog() {
        progressLoading.showProgress( "")
    }

    override fun hideProgressDialog() {
        progressLoading.hideProgress()
    }

    override fun showSnackbarMessage(error: String?) {
        hideProgressDialog()
        Functions.showSnackbarShortMessage(this.view, error)
    }

    override fun showToastMessage(toast: String?) {
        hideProgressDialog()
        Functions.showToastShortMessage(activity, toast)
    }


}