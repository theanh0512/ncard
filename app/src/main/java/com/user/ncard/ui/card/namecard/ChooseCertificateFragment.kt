package com.user.ncard.ui.card.namecard

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentChooseCertificateBinding
import com.user.ncard.util.Utils

class ChooseCertificateFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentChooseCertificateBinding
    lateinit var nameCardCertificateAdapter: NameCardCertificateAdapter
    val urlList = ArrayList<Int>()
    val selectedCerts = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_choose_certificate, container, false)!!
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
        nameCardCertificateAdapter = NameCardCertificateAdapter(object : NameCardCertificateAdapter.OnClickCallback {
            override fun onClick(url: Int) {
                if (selectedCerts.contains(url)) selectedCerts.remove(url)
                else selectedCerts.add(url)
            }
        })
        viewDataBinding.recyclerView.apply {
            adapter = nameCardCertificateAdapter
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 4)
        }

        urlList.apply {
            add(R.drawable.a_2_la)
            add(R.drawable.anab)
            add(R.drawable.as_9100)
            add(R.drawable.cofrac)
            add(R.drawable.cspc)
            add(R.drawable.d_akk_s)
            add(R.drawable.fcc)
            add(R.drawable.gdmpds)
            add(R.drawable.hkas)
            add(R.drawable.iaf)
            add(R.drawable.iso_9001)
            add(R.drawable.iso_13485)
            add(R.drawable.iso_13485_2)
            add(R.drawable.iso_14001)
            add(R.drawable.iso_22000)
            add(R.drawable.iso_22301)
            add(R.drawable.iso_27001)
            add(R.drawable.iso_29990)
            add(R.drawable.ohsas)
            add(R.drawable.singapore_accreditation_council)
            add(R.drawable.ss_501)
            add(R.drawable.ss_590_haccp)
            add(R.drawable.ukas)
            add(R.drawable.wsh_council_1)
            add(R.drawable.wsh_council_2)
            add(R.drawable.wsh_council_3)
            add(R.drawable.wsh_council_4)
        }

        nameCardCertificateAdapter.replace(urlList)
        nameCardCertificateAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_done, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_done -> {
                val returnIntent = Intent()
                returnIntent.putExtra("url", selectedCerts)
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