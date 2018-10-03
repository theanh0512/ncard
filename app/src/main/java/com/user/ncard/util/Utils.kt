package com.user.ncard.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.databinding.ObservableField
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException
import com.user.ncard.R
import com.user.ncard.SingleLiveEvent
import com.user.ncard.api.AppAuthenticationHandler
import com.user.ncard.ui.catalogue.Constants
import com.user.ncard.ui.catalogue.S3TransferUtil.*
import com.user.ncard.ui.catalogue.utils.CallbackAlertDialogListener
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.OnDialogOkClick
import com.user.ncard.util.ext.getColorFromResId
import java.io.File
import java.lang.Exception
import java.lang.Long
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Pham on 8/9/2017.
 */

object Utils {
    fun setWindowsWithBackgroundDrawable(activity: Activity, drawable: Int) {
        val window = activity.window
        val background = ResourcesCompat.getDrawable(activity.resources, drawable, null)
        window.statusBarColor = activity.getColorFromResId(R.color.colorDarkBlue)
        //        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //        window.setStatusBarColor(activity.getResources().getColor(R.color.color_transparent));
        //        window.setNavigationBarColor(activity.getResources().getColor(R.color.color_transparent));
        window.setBackgroundDrawable(background)
    }

    fun setWindowsWithBackgroundColor(activity: Activity, color: Int) {
        val window = activity.window
        window.decorView.setBackgroundColor(color)
        window.statusBarColor = activity.getColorFromResId(R.color.colorDarkBlue)
    }

    fun setUpActionBar(activity: Activity, toolbar: Toolbar, title: Int, color: Int = R.color.colorDarkerWhite) {
        Utils.setWindowsWithBackgroundColor(activity, ContextCompat.getColor(activity, color))
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
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
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).setText(title)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }
    }

    fun getAuthenticationHandler(sharedPreferenceHelper: SharedPreferenceHelper,
                                 event: SingleLiveEvent<Void>, username: String, password: String,
                                 error: ObservableField<String>, failureEvent: SingleLiveEvent<Void>, userNotConfirmEvent: SingleLiveEvent<Void>): AuthenticationHandler {
        return object : AuthenticationHandler {
            override fun onSuccess(cognitoUserSession: CognitoUserSession, device: CognitoDevice?) {
                Log.e("NCard", "Auth Success")
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL, username)
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_SESSION, cognitoUserSession.idToken.jwtToken)
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_USERNAME, cognitoUserSession.accessToken.username)
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.EXPIRATION_TIME_LONG, cognitoUserSession.accessToken.expiration.time)
                Log.e("NCard", "token: " + sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_SESSION))
                event.call()
            }

            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, username: String?) {
                Locale.setDefault(Locale.US)
                val authenticationDetails = AuthenticationDetails(username, password, null)
                authenticationContinuation.setAuthenticationDetails(authenticationDetails)
                authenticationContinuation.continueTask()
            }

            override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {
            }

            override fun onFailure(e: Exception) {
                error.set(AppHelper.formatException(e))
                if (e is UserNotConfirmedException) userNotConfirmEvent.call()
                else failureEvent.call()
            }

            override fun authenticationChallenge(continuation: ChallengeContinuation) {
                /**
                 * For Custom authentication challenge, implement your logic to present challenge to the
                 * userList and pass the userList's responses to the continuation.
                 */
            }
        }
    }

    @JvmStatic
    @Synchronized
    fun getToken(sharedPreferenceHelper: SharedPreferenceHelper, callback: AppAuthenticationHandler?) {
        if (sharedPreferenceHelper.getLong(SharedPreferenceHelper.Key.EXPIRATION_TIME_LONG) < System.currentTimeMillis()) {
            AppHelper.getPool()
                    .getUser(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL))
                    .getSessionInBackground(object : AuthenticationHandler {
                        override fun onSuccess(cognitoUserSession: CognitoUserSession, device: CognitoDevice?) {
                            Log.e("NCard", "refresh Auth Success")
                            sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_SESSION, cognitoUserSession.idToken.jwtToken)
                            sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_USERNAME, cognitoUserSession.accessToken.username)
                            sharedPreferenceHelper.put(SharedPreferenceHelper.Key.EXPIRATION_TIME_LONG, cognitoUserSession.accessToken.expiration.time)
                            Log.e("NCard", "refresh token: " + sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_SESSION))
                            val _token = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_SESSION)
                            // Add new header to rejected request and retry it
                            callback?.onSuccessAuthentication(cognitoUserSession, device)
                        }

                        override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, username: String?) {
                            authenticationContinuation.continueTask()
                        }

                        override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {
                        }

                        override fun onFailure(e: Exception) {
                            e.printStackTrace()
                        }

                        override fun authenticationChallenge(continuation: ChallengeContinuation) {
                        }
                    })
        } else {
            callback?.onSuccessAuthentication(null, null)
        }
    }

    fun backAction(activity: Activity) {
        if (activity.fragmentManager.backStackEntryCount > 0)
            activity.fragmentManager.popBackStack()
        else NavUtils.navigateUpFromSameTask(activity)
    }

    fun showAlert(activity: Activity): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_progress, null)
        builder.setView(v)
        val sendingRequestAlert = builder.create()
        sendingRequestAlert.show()
        return sendingRequestAlert
    }

    fun showAlert(activity: Activity, message: String): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.cancel() }
        builder.setTitle(R.string.cardline)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    fun showAlertWithCheckIcon(activity: Activity, message: Int): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setTitle(R.string.cardline)
        builder.setIcon(R.drawable.check_black)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    fun showAlertDialogYesNo(context: Context?, title: String, message: String, callback: CallbackAlertDialogListener?) {
        if (context != null && !TextUtils.isEmpty(message)) {
            val alertDialogBuilder = android.app.AlertDialog.Builder(
                    context)
            Functions.hideSoftKeyboard(context as Activity?)

            if (!TextUtils.isEmpty(title)) {
                alertDialogBuilder.setTitle(title)
            }
            alertDialogBuilder
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, { dialog, _ ->
                        callback?.onClickOk()
                        dialog.dismiss()
                    })
                    .setNegativeButton(R.string.no, { dialog, _ ->
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        callback?.onClickCancel()
                        dialog.cancel()
                    })

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    fun showAlertDialogWithOk(context: Context?, title: String, message: String, callback: OnDialogOkClick?) {
        if (context != null && !TextUtils.isEmpty(message)) {
            val alertDialogBuilder = android.app.AlertDialog.Builder(
                    context)
            Functions.hideSoftKeyboard(context as Activity?)

            if (!TextUtils.isEmpty(title)) {
                alertDialogBuilder.setTitle(title)
            }
            alertDialogBuilder
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, { dialog, _ ->
                        callback?.onOkClick()
                        dialog.dismiss()
                    })

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    fun showAlertWithCheckIcon(activity: Activity, message: String): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setTitle(R.string.cardline)
        builder.setIcon(R.drawable.check_black)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

    fun createImageFile(activity: Activity): File {
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

    fun getFilePathInS3NameCard(key: String, fileName: String): String {
        return Constants.PATH_NCARD + key + fileName
    }

    fun provideBackgroundUrl(number: Int): String {
        return when (number) {
            0 -> "https://s3-ap-northeast-1.amazonaws.com/ncard-assets/nameCard/background/c69dc51d-8e32-4a3a-88da-2004912a648c.png"
            1 -> "https://s3-ap-northeast-1.amazonaws.com/ncard-assets/nameCard/background/13294b43-8cc6-4129-8164-496b323058f8.png"
            2 -> "https://s3-ap-northeast-1.amazonaws.com/ncard-assets/nameCard/background/ac2abc3d-5f7e-4dfd-b7bd-9f2d9776ed1f.png"
            3 -> "https://s3-ap-northeast-1.amazonaws.com/ncard-assets/nameCard/background/1c6aae31-7b10-4c59-8796-8771cef33915.png"
            else -> "https://s3-ap-northeast-1.amazonaws.com/ncard-assets/nameCard/background/9300f361-b3a4-441f-93c3-edd24d6e9ed5.png"
        }
    }

    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    @Throws(URISyntaxException::class)
    fun getPath(uri: Uri, activity: Activity): String? {
        var fileUri = uri
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (DocumentsContract.isDocumentUri(activity.applicationContext, fileUri)) {
            when {
                isExternalStorageDocument(fileUri) -> {
                    val docId = DocumentsContract.getDocumentId(fileUri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
                isDownloadsDocument(fileUri) -> {
                    val id = DocumentsContract.getDocumentId(fileUri)
                    fileUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id)!!)
                }
                isMediaDocument(fileUri) -> {
                    val docId = DocumentsContract.getDocumentId(fileUri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    fileUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                }
            }
        }
        if ("content".equals(fileUri.scheme, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor?
            try {
                cursor = activity.contentResolver
                        .query(fileUri, projection, selection, selectionArgs, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
                cursor.close()
            } catch (e: Exception) {
            }

        } else if ("file".equals(fileUri.scheme, ignoreCase = true)) {
            return fileUri.path
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /* I cannot see logs output by Log.d on some of my phones, then
     *  this is an option to use Log.e
      * */
    val USE_ERROR_LOG = true

    fun Log(tag: String, message: String) {
        if (USE_ERROR_LOG) {
            Log.e(tag, message)
        } else {
            Log.d(tag, message)
        }
    }

    val nationalityList = arrayOf(
            "Afghan",
            "Albanian",
            "Algerian",
            "American",
            "Andorian",
            "Angolian",
            "Anguillan",
            "Antiguans",
            "Argentinean",
            "Armenian",
            "Australian",
            "Austrian",
            "Azerbaijani",
            "Bahamian",
            "Bahraini",
            "Bangladeshi",
            "Barbadian",
            "Barbudans",
            "Batswana",
            "Belarusian",
            "Belgian",
            "Belizean",
            "Beninese",
            "Bhutanese",
            "Bolivian",
            "Bosnian",
            "Brazilian",
            "British",
            "Bruneian",
            "Bulgarian",
            "Burkinabe",
            "Burmese",
            "Burundian",
            "Cambodian",
            "Cameroonian",
            "Canadian",
            "Cape Verdean",
            "Central African",
            "Chadian",
            "Chilean",
            "Chinese",
            "Colombian",
            "Comoran",
            "Congolese",
            "Costa Rican",
            "Croatian",
            "Cuban",
            "Cypriot",
            "Czech",
            "Danish",
            "Djibouti",
            "Dominican",
            "Dutch",
            "East Timorese",
            "Ecuadorean",
            "Egyptian",
            "Emirian",
            "Equatorial Guinean",
            "Eritrean",
            "Estonian",
            "Ethiopian",
            "Fijian",
            "Filipino",
            "Finnish",
            "French",
            "Gabonese",
            "Gambian",
            "Georgian",
            "German",
            "Ghanaian",
            "Greek",
            "Grenadian",
            "Guatemalan",
            "Guinea-Bissauan",
            "Guinean",
            "Guyanese",
            "Haitian",
            "Herzegovinian",
            "Honduran",
            "Hungarian",
            "I-Kiribati",
            "Icelander",
            "Indian",
            "Indonesian",
            "Iranian",
            "Iraqi",
            "Irish",
            "Israeli",
            "Italian",
            "Ivorian",
            "Jamaican",
            "Japanese",
            "Jordanian",
            "Kazakhstani",
            "Kenyan",
            "Kittian and Nevisian",
            "Kuwaiti",
            "Kyrgyz",
            "Laotian",
            "Latvian",
            "Lebanese",
            "Liberian",
            "Libyan",
            "Liechtensteiner",
            "Lithuanian",
            "Luxembourger",
            "Macedonian",
            "Malagasy",
            "Malawian",
            "Malaysian",
            "Maldivan",
            "Malian",
            "Maltese",
            "Marshallese",
            "Mauritanian",
            "Mauritian",
            "Mexican",
            "Micronesian",
            "Moldovan",
            "Monacan",
            "Mongolian",
            "Moroccan",
            "Mosotho",
            "Motswana",
            "Mozambican",
            "Namibian",
            "Nauruan",
            "Nepalese",
            "New Zealander",
            "Nicaraguan",
            "Nigerian",
            "Nigerien",
            "North Korean",
            "Northern Irish",
            "Norwegian",
            "Omani",
            "Pakistani",
            "Palauan",
            "Panamanian",
            "Papua New Guinean",
            "Paraguayan",
            "Peruvian",
            "Polish",
            "Portuguese",
            "Qatari",
            "Romanian",
            "Russian",
            "Rwandan",
            "Saint Lucian",
            "Salvadoran",
            "Samoan",
            "San Marinese",
            "Sao Tomean",
            "Saudi",
            "Scottish",
            "Senegalese",
            "Serbian",
            "Seychellois",
            "Sierra Leonean",
            "Singaporean",
            "Slovakian",
            "Slovenian",
            "Solomon Islander",
            "Somali",
            "South African",
            "South Korean",
            "Spanish",
            "Sri Lankan",
            "Sudanese",
            "Surinamer",
            "Swazi",
            "Swedish",
            "Swiss",
            "Syrian",
            "Taiwanese",
            "Tajik",
            "Tanzanian",
            "Thai",
            "Togolese",
            "Tongan",
            "Trinidadian/Tobagonian",
            "Tunisian",
            "Turkish",
            "Tuvaluan",
            "Ugandan",
            "Ukrainian",
            "Uruguayan",
            "Uzbekistani",
            "Venezuelan",
            "Vietnamese",
            "Welsh",
            "Yemenite",
            "Zambian",
            "Zimbabwean").asList()
}
