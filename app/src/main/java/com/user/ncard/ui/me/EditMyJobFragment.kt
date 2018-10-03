package com.user.ncard.ui.me

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import com.user.ncard.R
import com.user.ncard.databinding.FragmentEditMyJobBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.namecard.ChooseCertificateActivity
import com.user.ncard.ui.card.namecard.EditNameCardFragment
import com.user.ncard.ui.card.namecard.NameCardRemarkActivity
import com.user.ncard.ui.card.namecard.SelectedPhotosAdapter
import com.user.ncard.ui.catalogue.utils.CallbackAlertDialogListener
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.OnDialogOkClick
import com.user.ncard.util.Utils
import com.user.ncard.vo.Job
import com.user.ncard.vo.NameCard
import io.reactivex.disposables.Disposable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class EditMyJobFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MyJobViewModel
    private lateinit var viewDataBinding: FragmentEditMyJobBinding
    private var job: Job? = null
    private lateinit var updateAlert: AlertDialog
    private lateinit var notEnoughFieldAlert: AlertDialog
    val mediaFileList = ArrayList<Any>()
    val hashMapMediaFileList = HashSet<Any>()
    lateinit var mediaAdapter: SelectedPhotosAdapter

    val logoFileList = ArrayList<Any>()
    val hashMapLogoFileList = HashSet<Any>()
    lateinit var logoAdapter: SelectedPhotosAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_edit_my_job, container, false)!!
        job = arguments?.getParcelable(ARGUMENT_JOB)
        if (job != null) Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.edit_job)
        else Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.create_job)

        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyJobViewModel::class.java)
        viewDataBinding.viewmodel = viewModel
        if (job != null) {
            viewModel.job.set(job)
            if (job?.to.isNullOrEmpty()) viewModel.setCurrentlyWorkHere(true)
            viewDataBinding.textViewDeleteJob.visibility = View.VISIBLE
        } else {
            viewModel.job.set(Job(0, "", "", "", "", "",
                    "", "", "", "", "", "", "", null, null, 0, "", "", "", 0))
            viewDataBinding.textViewDeleteJob.visibility = View.GONE
        }


        mediaAdapter = SelectedPhotosAdapter(object : SelectedPhotosAdapter.RemoveCallback {
            override fun onClick(source: Any) {
                hashMapMediaFileList.remove(source)
                mediaFileList.clear()
                mediaFileList.addAll(hashMapMediaFileList)
                mediaAdapter.replace(mediaFileList)
                mediaAdapter.notifyDataSetChanged()
            }
        })
        initUI()

        //edit the current name card where the profile picture was uploaded
        if (job != null) {
            if (job?.media?.isNotEmpty() == true) {
                hashMapMediaFileList.addAll(job?.media ?: ArrayList())
                mediaFileList.addAll(hashMapMediaFileList)
                mediaAdapter.replace(mediaFileList)
                mediaAdapter.notifyDataSetChanged()
            }
            if (job?.cert?.isNotEmpty() == true) {
                hashMapLogoFileList.addAll(job?.cert ?: ArrayList())
                logoFileList.addAll(hashMapLogoFileList)
                logoAdapter.replace(logoFileList)
                logoAdapter.notifyDataSetChanged()
            }
        }

        viewModel.successEvent.observe(this@EditMyJobFragment, android.arch.lifecycle.Observer {
            updateAlert.cancel()
            val nameCard = arguments.get(ARGUMENT_NAME_CARD) as NameCard?
            if (nameCard != null) {
                nameCard.apply {
                    role = job?.jobTitle
                    email = job?.companyEmail
                    company = job?.companyName
                    mobile = job?.mobile
                    address = job?.address
                    description = job?.description
                    country = job?.country
                    mediaUrls = job?.media
                    certUrls = job?.cert
                    tel1 = job?.tel1
                    tel2 = job?.tel2
                    did = job?.did
                    website = job?.website
                    fax = job?.fax
                }
                Utils.showAlertDialogYesNo(activity, getString(R.string.cardline), getString(R.string.job_updated_like_to_update_name_card), object : CallbackAlertDialogListener {
                    override fun onClickOk() {
                        val editNameCardFragment = EditNameCardFragment.newInstance(nameCard, true, true, null, true)
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, editNameCardFragment, "EditNameCardFragment")
                                .addToBackStack(null)
                                .commitAllowingStateLoss()
                    }

                    override fun onClickCancel() {
                        activity.onBackPressed()
                    }
                })
            } else {
                Utils.showAlertDialogWithOk(activity, getString(R.string.cardline), "Job updated!", object : OnDialogOkClick {
                    override fun onOkClick() {
                        activity.onBackPressed()
                    }

                })
            }
        })

        viewModel.deleteSuccessEvent.observe(this@EditMyJobFragment, android.arch.lifecycle.Observer {
            updateAlert.cancel()
            activity.onBackPressed()
        })
    }

    private fun initUI() {
        viewDataBinding.recyclerViewMedia.apply {
            adapter = mediaAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        viewDataBinding.imageViewAddMedia.setOnClickListener {
            val rxPermissions = RxPermissions(activity)
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object : io.reactivex.Observer<Boolean> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(t: Boolean) {
                    if (t) {
                        val intent = Intent()
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_CHOOSE_MEDIA)
                    } else {
                        Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                .show()
                    }
                }

                override fun onError(e: Throwable) {}
                override fun onComplete() {}
            })
        }

        logoAdapter = SelectedPhotosAdapter(object : SelectedPhotosAdapter.RemoveCallback {
            override fun onClick(source: Any) {
                hashMapLogoFileList.remove(source)
                logoFileList.clear()
                logoFileList.addAll(hashMapLogoFileList)
                logoAdapter.replace(logoFileList)
                logoAdapter.notifyDataSetChanged()
            }
        })
        viewDataBinding.recyclerViewLogo.apply {
            adapter = logoAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        viewDataBinding.imageViewAddLogo.setOnClickListener {
            val rxPermissions = RxPermissions(activity)
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object : io.reactivex.Observer<Boolean> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(t: Boolean) {
                    if (t) {
                        val intent = Intent(activity, ChooseCertificateActivity::class.java)
                        startActivityForResult(intent, REQUEST_CODE_CHOOSE_LOGO)
                    } else {
                        Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                .show()
                    }
                }

                override fun onError(e: Throwable) {}
                override fun onComplete() {}
            })
        }

        viewDataBinding.textViewStart.setOnClickListener {
            val datePickerFragment = DatePickerFragment(true, Calendar.getInstance().timeInMillis)
            datePickerFragment.show(activity.fragmentManager, "datePicker")
        }
        viewDataBinding.textViewEnd.setOnClickListener {
            if (!viewDataBinding.textViewStart.text.toString().isNullOrBlank()) {
                val datePickerFragment = DatePickerFragment(false, getLongTimeFromStringDate(viewDataBinding.textViewStart.text.toString()))
                datePickerFragment.show(activity.fragmentManager, "datePicker")
            } else {
                Functions.showToastShortMessage(activity, "Please set Start Date first")
            }
        }
        viewDataBinding.editTextCountry.setOnClickListener {
            val intent = Intent(activity, NameCardRemarkActivity::class.java)
            intent.putExtra("country", true)
            startActivityForResult(intent, REQUEST_COUNTRY)
        }
        viewDataBinding.editTextIndustry.setOnClickListener {
            val intent = Intent(activity, NameCardRemarkActivity::class.java)
            intent.putExtra("industry", true)
            startActivityForResult(intent, REQUEST_INDUSTRY)
        }

        viewDataBinding.textViewDeleteJob.setOnClickListener {
            Functions.showAlertDialogYesNo(activity, "", getString(R.string.warn_delete_job), object : CallbackAlertDialogListener {
                override fun onClickOk() {
                    viewModel.deleteJob()
                    updateAlert = Utils.showAlert(activity)
                    updateAlert.show()
                }

                override fun onClickCancel() {
                }

            })

        }
    }

    @SuppressLint("ValidFragment")
    inner class DatePickerFragment(val isStart: Boolean, val date: Long?) : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            if (isStart) {
                if (!viewDataBinding.textViewStart.text.toString().isNullOrBlank()) {
                    c.timeInMillis = getLongTimeFromStringDate(viewDataBinding.textViewStart.text.toString())!!
                }
            } else {
                if (!viewDataBinding.textViewEnd.text.toString().isNullOrBlank()) {
                    c.timeInMillis = getLongTimeFromStringDate(viewDataBinding.textViewEnd.text.toString())!!
                }
            }
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(activity, this, year, month, day)
            date?.let {
                if (isStart) {
                    datePickerDialog.datePicker.maxDate = date
                } else {
                    datePickerDialog.datePicker.minDate = date
                    datePickerDialog.datePicker.maxDate = c.timeInMillis
                }
            }
            // Create a new instance of DatePickerDialog and return it
            return datePickerDialog
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            val currentFormat = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
            val newFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dayString = if (day.toString().length == 1) "0" + day else day.toString()
            val monthLiteral = month + 1
            val monthString = if (monthLiteral.toString().length == 1) "0" + monthLiteral else monthLiteral.toString()
            val selectedDateString = "$dayString $monthString $year"
            try {
                val d = currentFormat.parse(selectedDateString)
                if (isStart) {
                    viewDataBinding.textViewStart.apply {
                        text = newFormat.format(d)
                        visibility = View.VISIBLE
                    }
                } else {
                    viewDataBinding.textViewEnd.apply {
                        text = newFormat.format(d)
                        visibility = View.VISIBLE
                    }
                }
            } catch (ex: ParseException) {
                Log.e("NCard", "Unable to parse date")
            }
        }
    }

    fun getLongTimeFromStringDate(startDate: String): Long? {
        val startDateFromat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        try {
            val parse = startDateFromat.parse(startDate)
            return parse?.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CHOOSE_MEDIA -> {
                    if (data?.clipData != null) (0 until data.clipData.itemCount)
                            .mapTo(hashMapMediaFileList) { data.clipData.getItemAt(it).uri }
                    else if (data?.data != null) hashMapMediaFileList.add(data.data)
                    mediaFileList.clear()
                    mediaFileList.addAll(hashMapMediaFileList)
                    mediaAdapter.replace(mediaFileList)
                    mediaAdapter.notifyDataSetChanged()
                    viewDataBinding.editTextJobDesc.requestFocus()
                }
                REQUEST_CODE_CHOOSE_LOGO -> {
                    if (data != null) {
                        hashMapLogoFileList.addAll(data.getIntegerArrayListExtra("url"))
                        logoFileList.clear()
                        logoFileList.addAll(hashMapLogoFileList)
                        logoAdapter.replace(logoFileList)
                        logoAdapter.notifyDataSetChanged()
                        viewDataBinding.editTextJobDesc.requestFocus()
                    }
                }
                REQUEST_COUNTRY -> viewDataBinding.editTextCountry.apply {
                    text = data?.getStringExtra("country") ?: ""
                    visibility = View.VISIBLE
                }
                REQUEST_INDUSTRY -> viewDataBinding.editTextIndustry.apply {
                    text = data?.getStringExtra("industry") ?: ""
                    visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }

            R.id.menu_item_save -> {
                //check if mandatory field filled in
                if (viewDataBinding.editTextJobRole.text.isBlank() || viewDataBinding.editTextCompanyName.text.isBlank()
                        || viewDataBinding.editTextIndustry.text.isBlank() || viewDataBinding.editTextEmail.text.isBlank()
                        || viewDataBinding.textViewStart.text.isBlank())
                    notEnoughFieldAlert = Utils.showAlert(activity, getString(R.string.fill_in_all_info_to_submit))
                else {
                    updateAlert = Utils.showAlert(activity)
                    updateAlert.show()
                    val mediaFilePathsList = ArrayList<String>()
                    mediaFileList.forEach {
                        if (it is Uri && Utils.getPath(it, activity) != null)
                            mediaFilePathsList.add(Utils.getPath(it, activity)!!)
                        else mediaFilePathsList.add(it as String)
                    }
                    val logoFilePathsList = ArrayList<String>()
                    logoFileList.forEach {
                        logoFilePathsList.add(it.toString())
                    }
                    if (arguments.getBoolean(ARGUMENT_IS_UPDATING))
                        viewModel.updateJob(mediaFilePathsList, logoFilePathsList, viewDataBinding.textViewStart.text.toString(),
                                viewDataBinding.textViewEnd.text.toString())
                    else
                        viewModel.createJob(mediaFilePathsList, logoFilePathsList, viewDataBinding.textViewStart.text.toString(),
                                viewDataBinding.textViewEnd.text.toString())
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARGUMENT_JOB = "JOB"
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"
        private const val ARGUMENT_IS_UPDATING = "IS_UPDATING"
        private const val REQUEST_CODE_CHOOSE_MEDIA = 23
        private const val REQUEST_CODE_CHOOSE_LOGO = 24
        private const val REQUEST_COUNTRY = 102
        private const val REQUEST_INDUSTRY = 103

        fun newInstance(job: Job?, isUpdating: Boolean, nameCard: NameCard?) = EditMyJobFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_JOB, job)
                putParcelable(ARGUMENT_NAME_CARD, nameCard)
                putBoolean(ARGUMENT_IS_UPDATING, isUpdating)
            }
        }

    }
}