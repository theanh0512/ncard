package com.user.ncard.ui.card.namecard

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.tbruyelle.rxpermissions2.RxPermissions
import com.user.ncard.MainActivity
import com.user.ncard.R
import com.user.ncard.databinding.FragmentEditNameCardBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.Utils
import com.user.ncard.vo.NameCard
import io.reactivex.disposables.Disposable
import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class EditNameCardFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: EditNameCardViewModel
    private lateinit var viewDataBinding: FragmentEditNameCardBinding
    private var nameCard: NameCard? = null
    private lateinit var updateNameCardAlert: AlertDialog
    val mediaFileList = ArrayList<Any>()
    val hashMapMediaFileList = HashSet<Any>()
    lateinit var mediaAdapter: SelectedPhotosAdapter
    var confirmAlert: AlertDialog? = null

    val logoFileList = ArrayList<Any>()
    val hashMapLogoFileList = HashSet<Any>()
    lateinit var logoAdapter: SelectedPhotosAdapter
    var profileUri: Uri? = null
    var frontUri: Uri? = null
    var backUri: Uri? = null
    var backgroundUrl = Utils.provideBackgroundUrl(0)
    lateinit var getPhotoAlert: AlertDialog
    var fileFront: File? = null
    var fileBack: File? = null
    var uriFront: Uri? = null
    var uriBack: Uri? = null
    var uriProfile: Uri? = null
    var fileProfile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_edit_name_card, container, false)!!

        initBackgroundAndToolbar()

        return viewDataBinding.root
    }

    private fun initBackgroundAndToolbar() {
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
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).text = getString(R.string.edit_name_card)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EditNameCardViewModel::class.java)
        nameCard = arguments?.getParcelable(ARGUMENT_NAME_CARD)
        viewDataBinding.viewmodel = viewModel
        if (nameCard != null) {
            val frontUriScanned = arguments?.getSerializable(ARGUMENT_FRONT_URI) as URI?
            if (frontUriScanned != null) {
                nameCard?.frontUrl = frontUriScanned.rawPath
                frontUri = Uri.parse(frontUriScanned.toString())
            }
            viewModel.nameCard.set(nameCard)
        } else viewModel.nameCard.set(NameCard(0, "", "", "", "", "",
                "", "", null, "", "", "", Utils.provideBackgroundUrl(0),
                "", "", "", "", "", "", null, null, "", "", "", "",
                "", "", "", false, "", "", 0, 0, ""))

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
        if (nameCard != null) {
            if (!nameCard?.companyLogoUrl.isNullOrEmpty())
                Glide.with(context).load(nameCard?.companyLogoUrl).into(viewDataBinding.imageViewProfile)
            if (nameCard?.mediaUrls?.isNotEmpty() == true) {
                hashMapMediaFileList.addAll(nameCard?.mediaUrls ?: ArrayList())
                mediaFileList.addAll(hashMapMediaFileList)
                mediaAdapter.replace(mediaFileList)
                mediaAdapter.notifyDataSetChanged()
            }
            if (nameCard?.certUrls?.isNotEmpty() == true) {
                hashMapLogoFileList.addAll(nameCard?.certUrls ?: ArrayList())
                logoFileList.addAll(hashMapLogoFileList)
                logoAdapter.replace(logoFileList)
                logoAdapter.notifyDataSetChanged()
            }
        }

        viewModel.createNameCardSuccess.observe(this@EditNameCardFragment, android.arch.lifecycle.Observer {
            updateNameCardAlert.cancel()
            if (arguments.getBoolean(ARGUMENT_IS_MY_NAME_CARD)) {
                if (arguments.getBoolean(ARGUMENT_IS_AFTER_UPDATE)) {
                    fragmentManager.popBackStack("EditMyJobFragment", 1)
                } else {
                    if (fragmentManager.backStackEntryCount > 0)
                        fragmentManager.popBackStack()
                    else NavUtils.navigateUpFromSameTask(activity)
                }
            } else {
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        })
    }

    private fun initUI() {
        viewDataBinding.recyclerViewMedia.apply {
            adapter = mediaAdapter
            setHasFixedSize(true)
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
            val intent = Intent(activity, ChooseCertificateActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_LOGO)
        }

        viewDataBinding.imageViewProfile.setOnClickListener {
            showDialogMessage(false, true)
        }

        viewDataBinding.imageViewCardFront.setOnClickListener {
            showDialogMessage(true, false)
        }

        viewDataBinding.imageViewCardBack.setOnClickListener {
            showDialogMessage(false, false)
        }

        viewDataBinding.textViewChooseCardBackground.setOnClickListener {
            val rxPermissions = RxPermissions(activity)
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object : io.reactivex.Observer<Boolean> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(t: Boolean) {
                    if (t) {
                        val intent = Intent(activity, ChooseBackgroundActivity::class.java)
                        startActivityForResult(intent, REQUEST_BACKGROUND)
                    } else {
                        Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(e: Throwable) {}
                override fun onComplete() {}
            })
        }
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
                REQUEST_CODE_CHOOSE_PROFILE -> {
                    profileUri = data?.data
                    Glide.with(context).load(profileUri).into(viewDataBinding.imageViewProfile)
                }
                REQUEST_CHOOSE_IMAGE_FRONT -> {
                    frontUri = data?.data
                    Glide.with(context).load(frontUri).into(viewDataBinding.imageViewCardFront)
                }
                REQUEST_CHOOSE_IMAGE_BACK -> {
                    backUri = data?.data
                    Glide.with(context).load(backUri).into(viewDataBinding.imageViewCardBack)
                }
                REQUEST_TAKE_PICTURE_FRONT -> {
                    frontUri = uriFront
                    Glide.with(context).load(frontUri).into(viewDataBinding.imageViewCardFront)
                }
                REQUEST_TAKE_PICTURE_BACK -> {
                    backUri = uriBack
                    Glide.with(context).load(backUri).into(viewDataBinding.imageViewCardBack)
                }
                REQUEST_CODE_TAKE_PICTURE_PROFILE -> {
                    profileUri = uriProfile
                    Glide.with(context).load(profileUri).into(viewDataBinding.imageViewProfile)
                }
                REQUEST_BACKGROUND -> {
                    if (data != null) {
                        backgroundUrl = data.getStringExtra("url")
                        viewModel.nameCard.get()?.backgroundUrl = backgroundUrl
                        Glide.with(activity).load(backgroundUrl).into(viewDataBinding.imageViewCardBackground)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_name_card, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun showConfirmAlert(activity: Activity, message: String): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            if (arguments.getBoolean(ARGUMENT_IS_AFTER_UPDATE)) {
                fragmentManager.popBackStack("EditMyJobFragment", 1)
            } else {
                if (fragmentManager.backStackEntryCount > 0)
                    fragmentManager.popBackStack()
                else NavUtils.navigateUpFromSameTask(activity)
            }
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        builder.setTitle(R.string.cardline)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (confirmAlert == null) confirmAlert = showConfirmAlert(activity, getString(R.string.are_you_sure_to_quit))
                else confirmAlert?.show()
                return true
            }

            R.id.menu_item_save -> {
                if (viewDataBinding.editTextName.text.isBlank() || viewDataBinding.editTextCompany.text.isBlank()
                        || viewDataBinding.editTextRole.text.isBlank() || viewDataBinding.editTextEmail.text.isBlank())
                    Utils.showAlert(activity, getString(R.string.fill_in_all_info_to_submit))
                else {
                    updateNameCardAlert = Utils.showAlert(activity)
                    updateNameCardAlert.show()

                    var profilePath: String? = null
                    if (fileProfile != null) profilePath = fileProfile?.path
                    else if (profileUri != null) profilePath = Utils.getPath(profileUri!!, activity)

                    var frontPath: String? = null
                    if (fileFront != null) frontPath = fileFront?.path
                    else if (frontUri != null) frontPath = Utils.getPath(frontUri!!, activity)

                    var backPath: String? = null
                    if (fileBack != null) backPath = fileBack?.path
                    else if (backUri != null) backPath = Utils.getPath(backUri!!, activity)

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
                        viewModel.updateNameCard(profilePath ?: "", mediaFilePathsList,
                                logoFilePathsList, arguments.getBoolean(ARGUMENT_IS_MY_NAME_CARD), frontPath ?: "", backPath ?: "")
                    else
                        viewModel.createNameCard(profilePath ?: "", mediaFilePathsList,
                                logoFilePathsList, arguments.getBoolean(ARGUMENT_IS_MY_NAME_CARD), frontPath ?: "", backPath ?: "")
                }
            }
            R.id.menu_item_preview -> {
                val intent = Intent(activity, NameCardDetailActivity::class.java)
                intent.putExtra("namecard", viewModel.nameCard.get())
                intent.putExtra("profileUri", profileUri)
                intent.putExtra("frontUri", frontUri)
                intent.putExtra("backUri", backUri)
                val logoImageSource = ArrayList<ImageSource>()
                logoFileList.forEach {
                    when (it) {
                        is Uri -> logoImageSource.add(ImageSource(it, ""))
                        is String -> logoImageSource.add(ImageSource(null, it))
                        is Int -> logoImageSource.add(ImageSource(null, "", it))
                    }
                }
                val mediaImageSource = ArrayList<ImageSource>()
                mediaFileList.forEach {
                    if (it is Uri) mediaImageSource.add(ImageSource(it, ""))
                    else if (it is String) mediaImageSource.add(ImageSource(null, it))
                }
                intent.putExtra("logoUri", logoImageSource)
                intent.putExtra("mediaUri", mediaImageSource)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialogMessage(isFront: Boolean, isProfile: Boolean) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_get_photo, null)
        builder.setView(v)
        val takePhotoButton = v.findViewById<Button>(R.id.buttonTakePhoto)
        takePhotoButton.setOnClickListener {
            getPhotoAlert.cancel()
            takePicture(isFront, isProfile)
        }
        val chooseFromGalleryButton = v.findViewById<Button>(R.id.buttonChooseFromGallery)
        chooseFromGalleryButton.setOnClickListener {
            getPhotoAlert.cancel()
            chooseFromGallery(isFront, isProfile)
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

    private fun chooseFromGallery(isFront: Boolean, isProfile: Boolean) {
        val rxPermissions = RxPermissions(activity)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object : io.reactivex.Observer<Boolean> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: Boolean) {
                if (t) {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    if (isProfile) {
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_CHOOSE_PROFILE)
                    } else {
                        if (isFront)
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE_FRONT)
                        else startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE_BACK)
                    }
                } else {
                    Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG).show()
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }

    private fun takePicture(isFront: Boolean, isProfile: Boolean) {
        val rxPermissions = RxPermissions(activity)
        rxPermissions.request(Manifest.permission.CAMERA).subscribe(object : io.reactivex.Observer<Boolean> {
            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: Boolean) {
                if (t) {
                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePicture.resolveActivity(activity.packageManager) != null) {
                        try {
                            if (isProfile) fileProfile = createImageFile()
                            else {
                                if (isFront) fileFront = createImageFile()
                                else fileBack = createImageFile()
                            }
                        } catch (ex: IOException) {
                            // Error occurred while creating the File
                        }
                        if (isProfile) {
                            if (fileProfile != null) {
                                uriProfile = FileProvider.getUriForFile(activity, context.applicationContext.packageName + ".file.provider", fileProfile)
                                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, uriProfile)
                                startActivityForResult(takePicture, REQUEST_CODE_TAKE_PICTURE_PROFILE)
                            }
                        } else {
                            if (fileFront != null && isFront) {
                                uriFront = FileProvider.getUriForFile(activity, context.applicationContext.packageName + ".file.provider", fileFront)
                                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, uriFront)
                                startActivityForResult(takePicture, REQUEST_TAKE_PICTURE_FRONT)

                            } else if (fileBack != null && !isFront) {
                                uriBack = FileProvider.getUriForFile(activity, context.applicationContext.packageName + ".file.provider", fileBack)
                                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, uriBack)
                                startActivityForResult(takePicture, REQUEST_TAKE_PICTURE_BACK)
                            }
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

    private fun createImageFile(): File {
        // Create an image fileFront name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm").format(Date());
        val imageFileName = "JPEG_" + timeStamp + "_";
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        val image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        )
        return image
    }

    companion object {
        private const val ARGUMENT_NAME_CARD = "USER"
        private const val ARGUMENT_IS_AFTER_UPDATE = "AFTER_UPDATE_JOB"
        private const val ARGUMENT_IS_UPDATING = "IS_UPDATING"
        private const val ARGUMENT_IS_MY_NAME_CARD = "IS_MY_NAME_CARD"
        private const val ARGUMENT_FRONT_URI = "FRONT_URI"
        private const val REQUEST_CODE_CHOOSE_MEDIA = 23
        private const val REQUEST_CODE_CHOOSE_LOGO = 24
        private const val REQUEST_CODE_CHOOSE_PROFILE = 25
        private const val REQUEST_CODE_TAKE_PICTURE_PROFILE = 26
        private const val REQUEST_BACKGROUND = 28
        private const val REQUEST_TAKE_PICTURE_FRONT = 120
        private const val REQUEST_CHOOSE_IMAGE_FRONT = 121
        private const val REQUEST_TAKE_PICTURE_BACK = 122
        private const val REQUEST_CHOOSE_IMAGE_BACK = 123

        fun newInstance(nameCard: NameCard?, isUpdating: Boolean, isMyNameCard: Boolean, frontUriScanned: URI?, isAfterUpdateJob: Boolean = false) = EditNameCardFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_NAME_CARD, nameCard)
                putSerializable(ARGUMENT_FRONT_URI, frontUriScanned)
                putBoolean(ARGUMENT_IS_UPDATING, isUpdating)
                putBoolean(ARGUMENT_IS_AFTER_UPDATE, isAfterUpdateJob)
                putBoolean(ARGUMENT_IS_MY_NAME_CARD, isMyNameCard)
            }
        }

    }
}