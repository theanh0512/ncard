package com.user.ncard.ui.me

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentMyJobBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.namecard.NameCardDetailActivity
import com.user.ncard.util.Utils
import com.user.ncard.vo.Job
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.Resource
import javax.inject.Inject

class MyJobFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentMyJobBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: MyJobViewModel
    lateinit var jobAdapter: MyJobAdapter
    val myNameCards = ArrayList<NameCard>()
    lateinit var editMyJobFragment: EditMyJobFragment

    val jobs = ArrayList<Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        jobAdapter = MyJobAdapter(object : MyJobAdapter.OnClickCallBack {
            override fun onClick(job: Job) {
                //to update name card as well
                val filterNameCards = myNameCards.filter { it.jobId == job.id }
                editMyJobFragment = if (filterNameCards.isNotEmpty()) {
                    EditMyJobFragment.newInstance(job, true, filterNameCards[0])
                } else EditMyJobFragment.newInstance(job, true, null)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, editMyJobFragment)
                        .addToBackStack("EditMyJobFragment")
                        .commitAllowingStateLoss()
            }

        }, object : MyJobAdapter.OnNameCardClickCallBack {
            override fun onClick(job: Job) {
                val nameCard = myNameCards.findLast { job.id == it.jobId }
                if (nameCard != null) {
                    val intent = Intent(activity, NameCardDetailActivity::class.java)
                    intent.putExtra("namecard", nameCard)
                    intent.putExtra("isMyNameCard", true)
                    intent.putExtra("fromJobIcon", true)
                    startActivity(intent)
                }
            }

        })
        viewDataBinding.recyclerViewJob.apply {
            isNestedScrollingEnabled = false
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        viewModel.apply {
            jobList.observe(this@MyJobFragment, Observer<Resource<List<Job>>> {
                if (it?.data != null) {
                    jobs.clear()
                    jobs.addAll(it.data)
                    jobs.sortByDescending {
                        it.from
                    }
                    jobAdapter.replace2(jobs)
                    jobAdapter.notifyDataSetChanged()
                }
            })
            nameCardList.observe(this@MyJobFragment, Observer { cardList ->
                if (cardList?.data != null) {
                    myNameCards.clear()
                    myNameCards.addAll(cardList.data)
//                    myNameCards.forEach {
//                        jobs.findLast { job -> job.id == it.jobId }?.cardId = it.id
//                    }
                    jobAdapter.replace2(jobs)
                    jobAdapter.notifyDataSetChanged()
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_job, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.job)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyJobViewModel::class.java)
        return viewDataBinding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.start.value = true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_add, menu)
        val menuItem: MenuItem? = menu?.findItem(R.id.menu_item_add)
        val actionView = menuItem?.actionView
        actionView?.setOnClickListener {
            val editMyJob = EditMyJobFragment.newInstance(null, false, null)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, editMyJob)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
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