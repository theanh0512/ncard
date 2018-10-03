package com.user.ncard.ui.card.profile

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentUserJobBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.namecard.NameCardDetailActivity
import com.user.ncard.ui.me.MyJobAdapter
import com.user.ncard.util.Utils
import com.user.ncard.vo.Job
import com.user.ncard.vo.NameCard
import javax.inject.Inject

class UserJobFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentUserJobBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var jobAdapter: MyJobAdapter
    val cards = ArrayList<NameCard>()
    val jobs = ArrayList<Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_job, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.job)
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        jobAdapter = MyJobAdapter(object : MyJobAdapter.OnClickCallBack {
            override fun onClick(job: Job) {
                //We do nothing here
            }
        }, object : MyJobAdapter.OnNameCardClickCallBack {
            override fun onClick(job: Job) {
                val nameCard = cards.findLast { job.id == it.jobId }
                if (nameCard != null) {
                    val intent = Intent(activity, NameCardDetailActivity::class.java)
                    intent.putExtra("namecard", nameCard)
                    intent.putExtra("isFromUserJob", true)
                    startActivity(intent)
                }
            }

        })

        viewDataBinding.recyclerViewJob.apply {
            isNestedScrollingEnabled = false
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(this.context,
                    (layoutManager as LinearLayoutManager).orientation)
            dividerItemDecoration.setDrawable(context.resources.getDrawable(R.drawable.view_divider_decoration))
            addItemDecoration(dividerItemDecoration)
        }
    }

    override fun onStart() {
        super.onStart()
        jobAdapter.replace2(jobs)
        jobAdapter.notifyDataSetChanged()

        jobs.addAll(arguments.getParcelableArrayList(ARGUMENT_JOB_LIST))
        cards.addAll(arguments.getParcelableArrayList(ARGUMENT_CARD_LIST))
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

    companion object {
        private const val ARGUMENT_JOB_LIST = "JOB"
        private const val ARGUMENT_CARD_LIST = "CARD"
        fun newInstance(jobs: ArrayList<Job>?, cards: ArrayList<NameCard>?) = UserJobFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ARGUMENT_JOB_LIST, jobs)
                putParcelableArrayList(ARGUMENT_CARD_LIST, cards)
            }
        }
    }
}