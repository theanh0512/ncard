package com.user.ncard.ui.me

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.NumberPicker
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentMyNameCardBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.namecard.EditNameCardFragment
import com.user.ncard.ui.card.namecard.NameCardDetailActivity
import com.user.ncard.util.Utils
import com.user.ncard.vo.Job
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.Resource
import javax.inject.Inject

class MyNameCardFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentMyNameCardBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: MyNameCardViewModel
    lateinit var myNameCardAdapter: MyNameCardAdapter
    lateinit var jobAlert: AlertDialog
    val jobArrayList = ArrayList<Job>()
    val nameCardArrayList = ArrayList<NameCard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myNameCardAdapter = MyNameCardAdapter(object : MyNameCardAdapter.OnClickCallBack {
            override fun onClick(nameCard: NameCard) {
                val intent = Intent(activity, NameCardDetailActivity::class.java)
                intent.putExtra("namecard", nameCard)
                intent.putExtra("isMyNameCard", true)
                startActivity(intent)
            }

        })
        viewDataBinding.recyclerViewNameCards.apply {
            isNestedScrollingEnabled = false
            adapter = myNameCardAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        viewModel.nameCardList.observe(this@MyNameCardFragment, Observer<Resource<List<NameCard>>> { nameCardList ->
            myNameCardAdapter.replace(nameCardList?.data)
            myNameCardAdapter.notifyDataSetChanged()
            if (nameCardList?.data != null) {
                nameCardArrayList.addAll(nameCardList.data as ArrayList)
            }
        })

        viewModel.jobList.observe(this@MyNameCardFragment, Observer<Resource<List<Job>>> { jobList ->
            jobArrayList.clear()
            if (jobList?.data != null) {
                jobArrayList.addAll(jobList.data as ArrayList)
            }
        })
    }

    private fun showDialogMessage() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_choose_job, null)
        builder.setView(v)
        val numberPicker = v.findViewById<NumberPicker>(R.id.picker)

        nameCardArrayList.forEach { nameCard ->
            val listTemp = jobArrayList.filterNot { nameCard.jobId == it.id }
            jobArrayList.clear()
            jobArrayList.addAll(listTemp)
        }

        if (jobArrayList.size > 0) {
            numberPicker.apply {
                minValue = 0
                maxValue = jobArrayList.size - 1
                displayedValues = jobArrayList.map { job -> job.jobTitle }.toTypedArray()
            }
            val textViewCancel = v.findViewById<TextView>(R.id.textViewCancel)
            textViewCancel.setOnClickListener {
                jobAlert.cancel()
            }
            val textViewDone = v.findViewById<TextView>(R.id.textViewDone)
            textViewDone.setOnClickListener {
                jobAlert.cancel()
                val job = jobArrayList[numberPicker.value]
                var nameCard: NameCard? = null
                job.apply {
                    nameCard = NameCard(1, "", "", jobTitle, companyEmail, companyName,
                            mobile, address, null, "", "", "", Utils.provideBackgroundUrl(0),
                            description, country, "", "", "", "", media, cert, tel1, tel2, did, "",
                            website, fax, "", false, "", "", id, 0, "")
                }

                val editNameCardFragment = EditNameCardFragment.newInstance(nameCard, false, true, null)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, editNameCardFragment, "EditNameCardFragment")
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
            jobAlert = builder.create()
            val window: Window = jobAlert.window
            val attributes = window.attributes
            attributes.gravity = Gravity.BOTTOM
            attributes.y = 50
            attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            window.attributes = attributes
            window.setBackgroundDrawable(ColorDrawable(Color.WHITE))

            jobAlert.show()
        } else {
            Utils.showAlert(activity, getString(R.string.alert_no_job))
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_name_card, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.my_name_card)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyNameCardViewModel::class.java)
        viewModel.start.value = true
        return viewDataBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_add, menu)
        val menuItem: MenuItem? = menu?.findItem(R.id.menu_item_add)
        val actionView = menuItem?.actionView
        actionView?.setOnClickListener {
            showDialogMessage()
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}