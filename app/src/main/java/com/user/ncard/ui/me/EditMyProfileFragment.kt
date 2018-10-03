package com.user.ncard.ui.me

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import com.user.ncard.R
import com.user.ncard.databinding.FragmentEditMyProfileBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.namecard.NameCardRemarkActivity
import com.user.ncard.util.Utils
import com.user.ncard.vo.User
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_edit_my_profile.*
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class EditMyProfileFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: EditMyProfileViewModel
    private lateinit var viewDataBinding: FragmentEditMyProfileBinding
    private var user: User? = null
    private lateinit var updateNameCardAlert: AlertDialog
    var profileUri: Uri? = null
    lateinit var genderAlert: AlertDialog
    var mandatoryFieldAlert: AlertDialog? = null
    var uriProfile: Uri? = null
    var fileProfile: File? = null
    lateinit var getPhotoAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_edit_my_profile, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.summary_info)

        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EditMyProfileViewModel::class.java)
        user = arguments?.getParcelable(ARGUMENT_USER)
        viewDataBinding.viewmodel = viewModel
        if (user != null) viewModel.user.set(user)

        initUI()

        //edit the current name card where the profile picture was uploaded
        if (user != null) {
            if (!user?.profileImageUrl.isNullOrEmpty())
                Glide.with(context).load(user?.profileImageUrl).apply(RequestOptions
                        .circleCropTransform()
                        .placeholder(R.drawable.image_place_holder)
                        .fallback(R.drawable.image_place_holder)
                        .error(R.drawable.image_place_holder)).into(viewDataBinding.imageViewProfile)
        }

        viewModel.updateInfoSuccess.observe(this@EditMyProfileFragment, android.arch.lifecycle.Observer {
            updateNameCardAlert.cancel()
            if (fragmentManager.backStackEntryCount > 0)
                fragmentManager.popBackStack()
            else NavUtils.navigateUpFromSameTask(activity)
        })
    }

    private fun initUI() {

        viewDataBinding.imageViewProfile.setOnClickListener {
            showDialogGetPhoto()
        }
        viewDataBinding.editTextGender.setOnClickListener {
            showDialogMessage()
        }
        viewDataBinding.editTextBirthday.setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.show(activity.fragmentManager, "datePicker")
        }
        viewDataBinding.editTextCountry.setOnClickListener {
            val intent = Intent(activity, NameCardRemarkActivity::class.java)
            intent.putExtra("country", true)
            startActivityForResult(intent, REQUEST_COUNTRY)
        }
        viewDataBinding.editTextNationality.setOnClickListener {
            val intent = Intent(activity, NameCardRemarkActivity::class.java)
            intent.putExtra("nationality", true)
            startActivityForResult(intent, REQUEST_NATIONALITY)
        }
    }

    @SuppressLint("ValidFragment")
    inner class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog =DatePickerDialog(activity, this, year, month, day)
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis

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
                viewDataBinding.editTextBirthday.apply {
                    text = newFormat.format(d)
                    visibility = View.VISIBLE
                }
            } catch (ex: ParseException) {
                Log.e("NCard", "Unable to parse date")
            }
        }
    }

    private fun showDialogGetPhoto() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_get_photo, null)
        builder.setView(v)
        val takePhotoButton = v.findViewById<Button>(R.id.buttonTakePhoto)
        takePhotoButton.setOnClickListener {
            getPhotoAlert.cancel()
            takePicture()
        }
        val chooseFromGalleryButton = v.findViewById<Button>(R.id.buttonChooseFromGallery)
        chooseFromGalleryButton.setOnClickListener {
            getPhotoAlert.cancel()
            chooseFromGallery()
        }
        val cancelButton = v.findViewById<Button>(R.id.buttonCancel)
        cancelButton.setOnClickListener {
            getPhotoAlert.cancel()
        }
        getPhotoAlert = builder.create()
        val window: Window = getPhotoAlert.window
        val attributes = window.attributes
        attributes.gravity = Gravity.BOTTOM
        attributes.y = 124
        attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = attributes
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        getPhotoAlert.show()
    }

    private fun chooseFromGallery() {
        val rxPermissions = RxPermissions(activity)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object : io.reactivex.Observer<Boolean> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: Boolean) {
                if (t) {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_CHOOSE_PROFILE)

                } else {
                    Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG).show()
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }

    private fun takePicture() {
        val rxPermissions = RxPermissions(activity)
        rxPermissions.request(Manifest.permission.CAMERA).subscribe(object : io.reactivex.Observer<Boolean> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: Boolean) {
                if (t) {
                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePicture.resolveActivity(activity.packageManager) != null) {
                        try {
                            fileProfile = Utils.createImageFile(activity)
                        } catch (ex: IOException) {
                            // Error occurred while creating the File
                        }
                        if (fileProfile != null) {
                            uriProfile = FileProvider.getUriForFile(activity, context.applicationContext.packageName + ".file.provider", fileProfile)
                            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, uriProfile)
                            startActivityForResult(takePicture, REQUEST_CODE_TAKE_PICTURE_PROFILE)
                        }
                    }

                } else {
                    Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG).show()
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }

    private fun showDialogMessage() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_choose_gender, null)
        builder.setView(v)
        val numberPicker = v.findViewById<NumberPicker>(R.id.picker)
        numberPicker.apply {
            minValue = 0
            maxValue = 1
            displayedValues = arrayOf("male", "female")
        }
        val textViewCancel = v.findViewById<TextView>(R.id.textViewCancel)
        textViewCancel.setOnClickListener {
            genderAlert.cancel()
        }
        val textViewDone = v.findViewById<TextView>(R.id.textViewDone)
        textViewDone.setOnClickListener {
            genderAlert.cancel()
            viewDataBinding.editTextGender.apply {
                text = if (numberPicker.value == 0) "male" else "female"
                visibility = View.VISIBLE
            }
        }
        genderAlert = builder.create()
        val window: Window = genderAlert.window
        val attributes = window.attributes
        attributes.gravity = Gravity.BOTTOM
        attributes.y = 50
        attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = attributes
        window.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        genderAlert.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CHOOSE_PROFILE -> {
                    profileUri = data?.data
                    Glide.with(context).load(profileUri).apply(RequestOptions
                            .circleCropTransform()
                            .placeholder(R.drawable.image_place_holder)
                            .fallback(R.drawable.image_place_holder)
                            .error(R.drawable.image_place_holder)).into(viewDataBinding.imageViewProfile)
                }
                REQUEST_CODE_TAKE_PICTURE_PROFILE -> {
                    profileUri = uriProfile
                    Glide.with(context).load(profileUri).apply(RequestOptions
                            .circleCropTransform()
                            .placeholder(R.drawable.image_place_holder)
                            .fallback(R.drawable.image_place_holder)
                            .error(R.drawable.image_place_holder)).into(viewDataBinding.imageViewProfile)
                }
                REQUEST_NATIONALITY -> viewDataBinding.editTextNationality.apply {
                    text = data?.getStringExtra("nationality") ?: ""
                    visibility = View.VISIBLE
                }
                REQUEST_COUNTRY -> viewDataBinding.editTextCountry.apply {
                    text = data?.getStringExtra("country") ?: ""
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
                if (fragmentManager.backStackEntryCount > 0)
                    fragmentManager.popBackStack()
                else NavUtils.navigateUpFromSameTask(activity)
                return true
            }

            R.id.menu_item_save -> {
                if (viewDataBinding.editTextFirstName.text.isBlank() || viewDataBinding.editTextLastName.text.isBlank()) {
                    if (mandatoryFieldAlert == null) mandatoryFieldAlert = Utils.showAlert(activity, getString(R.string.mandatory_field_remind))
                    else mandatoryFieldAlert?.show()
                } else {
                    var profilePath: String? = null
                    if (fileProfile != null) profilePath = fileProfile?.path
                    else if (profileUri != null) profilePath = Utils.getPath(profileUri!!, activity)

                    updateNameCardAlert = Utils.showAlert(activity)
                    updateNameCardAlert.show()
                    viewModel.updateUser(profilePath ?: "", editTextBirthday.text?.toString())
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARGUMENT_USER = "USER"
        private const val REQUEST_CODE_CHOOSE_PROFILE = 25
        private const val REQUEST_CODE_TAKE_PICTURE_PROFILE = 26
        private const val REQUEST_NATIONALITY = 101
        private const val REQUEST_COUNTRY = 102

        fun newInstance(user: User) = EditMyProfileFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_USER, user)
            }
        }

    }
}