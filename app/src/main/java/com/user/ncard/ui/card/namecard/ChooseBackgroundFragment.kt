package com.user.ncard.ui.card.namecard

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.bumptech.glide.Glide
import com.user.ncard.R
import com.user.ncard.databinding.FragmentChooseBackgroundBinding
import com.user.ncard.util.Utils

class ChooseBackgroundFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentChooseBackgroundBinding
    lateinit var nameCardBackgroundAdapter: NameCardBackgroundAdapter
    val urlList = ArrayList<String>()
    var currentSelectedUrl = Utils.provideBackgroundUrl(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_choose_background, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, ContextCompat.getColor(context, R.color.colorDarkerWhite))
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
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(activity).load(currentSelectedUrl).into(viewDataBinding.imageViewBackground)
        nameCardBackgroundAdapter = NameCardBackgroundAdapter(object : NameCardBackgroundAdapter.OnClickCallback {
            override fun onClick(url: String) {
                currentSelectedUrl = url
                Glide.with(activity).load(url).into(viewDataBinding.imageViewBackground)
            }
        })
        viewDataBinding.recyclerViewBackground.apply {
            adapter = nameCardBackgroundAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
        (0..4).mapTo(urlList) { Utils.provideBackgroundUrl(it) }
        nameCardBackgroundAdapter.replace(urlList)
        nameCardBackgroundAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_done, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_done -> {
                val returnIntent = Intent()
                returnIntent.putExtra("url", currentSelectedUrl)
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