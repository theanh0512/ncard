package com.user.ncard.ui.catalogue.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.util.ArrayMap
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.bilibili.boxing.utils.BoxingExecutor
import com.google.gson.Gson
import com.user.ncard.R
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.CatalogueLike
import java.net.URLConnection
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import com.user.ncard.ui.catalogue.Constants
import com.user.ncard.ui.catalogue.S3TransferUtil
import com.user.ncard.ui.landing.LandingPageActivity
import com.user.ncard.vo.ChatDialog
import com.user.ncard.vo.Friend
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by trong-android-dev on 7/11/17.
 */
class Functions {
    companion object {

        fun isLike(sharedPreferenceHelper: SharedPreferenceHelper, likes: List<CatalogueLike>): Boolean {
            val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
            if (likes.isNotEmpty()) {
                val filter = likes.filter {
                    it?.ownerId == userId
                }
                if (filter != null && filter.isNotEmpty()) {
                    return true
                }
            }
            return false
        }

        fun getFullName(sharedPreferenceHelper: SharedPreferenceHelper): String {
            val firstName = sharedPreferenceHelper.getString(
                    SharedPreferenceHelper.Key.CURRENT_USER_FIRST_NAME, "")
            val lastName = sharedPreferenceHelper.getString(
                    SharedPreferenceHelper.Key.CURRENT_USER_LAST_NAME, "")
            if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
                return "null"
            } else {
                return "$firstName $lastName"
            }
        }

        fun getMyChatId(sharedPreferenceHelper: SharedPreferenceHelper): Int {
            val chatId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CHAT_USER_ID, -1)
            return chatId
        }

        fun getMyId(sharedPreferenceHelper: SharedPreferenceHelper): Int {
            val id = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID, -1)
            return id
        }

        fun showLogMessage(tag: String?, message: String?) {
            //if (Constants.DEBUG && tag != null && message != null) {
            Log.d(tag, message)
        }

        fun showErrorLogMessage(tag: String?, message: String?) {
            //if (Constants.DEBUG && tag != null && message != null) {
            Log.e(tag, message)
        }

        fun hideSoftKeyboard(activity: Activity?) {
            activity?.let {
                activity.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

                // Check if no view has focus:
                try {
                    val view = activity?.currentFocus
                    if (view != null) {
                        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                } catch (e: Exception) {
                    //            Crashlytics.log(e.getMessage());
                }
            }

        }

        fun getKeHash(activity: Activity): String {
            // Add code to print out the key hash
            var keyHash = ""
            try {
                val info = activity.packageManager.getPackageInfo(
                        activity.packageName, PackageManager.GET_SIGNATURES)
                for (signature in info.signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    Log.d("KeyHash:", android.util.Base64.encodeToString(
                            md.digest(), android.util.Base64.DEFAULT))
                    keyHash = android.util.Base64.encodeToString(md.digest(),
                            android.util.Base64.DEFAULT)
                }
            } catch (e: PackageManager.NameNotFoundException) {

            } catch (e: NoSuchAlgorithmException) {

            }

            return keyHash
        }

        fun showToastLongMessage(context: Context?, message: String?) {
            if (context != null && !TextUtils.isEmpty(message))
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        fun showToastShortMessage(context: Context?, message: String?) {
            if (context != null && !TextUtils.isEmpty(message))
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun showSnackbarLongMessage(layout: View?, message: String?) {
            if (layout != null && !TextUtils.isEmpty(message)) {
                if (layout.context as Activity != null) {
                    hideSoftKeyboard(layout.context as Activity)
                }
                // Functions.showLogMessage("Trong", message)
                val snackbar = Snackbar
                        .make(layout, message!!, Snackbar.LENGTH_LONG)
                        .setAction(R.string.ok, View.OnClickListener { })
                val snackbarView = snackbar.getView()
                val tv = snackbarView.findViewById<View>(android.support.design.R.id.snackbar_text) as TextView
                tv.maxLines = 3
                snackbar.show()
            }
        }

        fun showSnackbarShortMessage(layout: View?, message: String?) {
            if (layout != null && !TextUtils.isEmpty(message)) {
                if (layout.context as Activity != null) {
                    hideSoftKeyboard(layout.context as Activity)
                }
                val snackbar = Snackbar
                        .make(layout, message!!, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.ok, View.OnClickListener { })
                val snackbarView = snackbar.getView()
                val tv = snackbarView.findViewById<View>(android.support.design.R.id.snackbar_text) as TextView
                tv.maxLines = 3
                snackbar.show()
            }
        }

        fun showAlertDialog(context: Context?, title: String, message: String?) {
            if (context != null && !TextUtils.isEmpty(message)) {
                val alertDialogBuilder = AlertDialog.Builder(
                        context)
                hideSoftKeyboard((context as Activity?)!!)

                if (!TextUtils.isEmpty(title)) {
                    alertDialogBuilder.setTitle(title)
                }
                alertDialogBuilder
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id -> dialog.dismiss() })
                /*.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });*/

                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }

        fun showAlertDialog(context: Context?, title: String, message: String, callback: CallbackAlertDialogListener?) {
            if (context != null && !TextUtils.isEmpty(message)) {
                val alertDialogBuilder = AlertDialog.Builder(
                        context)
                hideSoftKeyboard(context as Activity?)

                if (!TextUtils.isEmpty(title)) {
                    alertDialogBuilder.setTitle(title)
                }
                alertDialogBuilder
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
                            if (callback != null) {
                                callback!!.onClickOk()
                            }
                            dialog.dismiss()
                        })
                /*.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });*/

                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }

        fun showAlertDialogYesNo(context: Context?, title: String, message: String, callback: CallbackAlertDialogListener?) {
            if (context != null && !TextUtils.isEmpty(message)) {
                val alertDialogBuilder = AlertDialog.Builder(
                        context)
                hideSoftKeyboard(context as Activity?)

                if (!TextUtils.isEmpty(title)) {
                    alertDialogBuilder.setTitle(title)
                }
                alertDialogBuilder
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
                            if (callback != null) {
                                callback!!.onClickOk()
                            }
                            dialog.dismiss()
                        })
                        .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener { dialog, id ->
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            if (callback != null) {
                                callback!!.onClickCancel()
                            }
                            dialog.cancel()
                        })

                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }

        fun isImageFile(path: String): Boolean {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            return mimeType != null && mimeType!!.startsWith("image")
        }

        fun isVideoFile(path: String): Boolean {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            return mimeType != null && mimeType!!.startsWith("video")
        }

        fun isAudioFile(path: String): Boolean {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            return mimeType != null && mimeType!!.startsWith("audio")
        }

        @JvmStatic
        fun getDirectory(): File {
            val name = Constants.FOLDER_NAME_TEMP

            val f = File(Environment.getExternalStorageDirectory(), name)
            if (!f.exists()) {
                f.mkdirs()
            }

            return f
        }

        /**
         * Empty the target file or folder
         */
        fun deleteFile(fileOrDirectory: File) {
            if (fileOrDirectory.isDirectory) {
                for (file in fileOrDirectory.listFiles()!!) {
                    deleteFile(file)
                }
            }
            fileOrDirectory.delete()
        }

        /**
         * Save image/video to MediaStore.
         */
        fun saveMediaStore(cr: ContentResolver?, path: String, type: Int) {
            BoxingExecutor.getInstance().runWorker(Runnable {
                if (cr != null && !TextUtils.isEmpty(path)) {
                    val values = ContentValues()
                    if (type == S3TransferUtil.TYPE_IMAGES) {
                        values.put(MediaStore.Images.Media.TITLE, System.currentTimeMillis().toInt())
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        values.put(MediaStore.Images.Media.DATA, path)
                        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    } else {
                        values.put(MediaStore.Video.Media.TITLE, System.currentTimeMillis().toInt())
                        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                        values.put(MediaStore.Video.Media.DATA, path)
                        cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                    }
                }
            })

        }

        fun sparrArrToArr(list: SparseArray<Friend>): List<Friend>? {
            val result: MutableList<Friend> = ArrayList<Friend>()
            if (list != null && list.size() > 0) {
                for (i in 0 until list.size()) {
                    result.add(list.valueAt(i))
                }

                return result
            }
            return null
        }

        fun arrayMapToArr(list: ArrayMap<String, ChatDialog>): List<ChatDialog>? {
            val result: MutableList<ChatDialog> = ArrayList<ChatDialog>()
            if (list != null && list.size > 0) {
                for (i in 0 until list.size) {
                    result.add(list.valueAt(i))
                }

                return result
            }
            return null
        }

        fun getGoogleMapStatic(lat: String?, lng: String?, context: Context): String {
            val urlMap =
                    String.format("https://maps.googleapis.com/maps/api/staticmap?center=%s ,%s&zoom=16&size=400x400&markers=color:blue|label:S|%s,%s&key=%s", lat, lng, lat, lng, context.getString(R.string.api_key))
            return urlMap
        }

        fun openMap(context: Context, _lat: String?, _lng: String?, label: String?) {
            if (_lat != null && _lat?.isNotEmpty() && _lng != null && _lng?.isNotEmpty()) {
                val uri = String.format(Locale.ENGLISH, "geo:%s,%s?z=15&q=%s,%s(%s)", _lat,
                        _lng, _lat, _lng,
                        if (label == null) "" else label)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                context.startActivity(intent)
            }
        }

        @JvmStatic
        fun formatTimeSecondsToMinutes(totalSecs: Int): String {
            var totalSecs = totalSecs
            val minutes = TimeUnit.SECONDS.toMinutes(totalSecs.toLong())
            totalSecs -= TimeUnit.MINUTES.toSeconds(minutes).toInt()
            val seconds = TimeUnit.SECONDS.toSeconds(totalSecs.toLong())
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

        @JvmStatic
        fun stringForTime(timeMs: Int): String {
            val mFormatBuilder: StringBuilder
            val mFormatter: Formatter
            mFormatBuilder = StringBuilder()
            mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
            val totalSeconds = timeMs / 1000

            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60 % 60
            val hours = totalSeconds / 3600

            mFormatBuilder.setLength(0)
            return if (hours > 0) {
                mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
            } else {
                mFormatter.format("%02d:%02d", minutes, seconds).toString()
            }
        }

        fun setClipboard(context: Context, text: String) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
                clipboard.text = text
            } else {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Text", text)
                clipboard.primaryClip = clip
            }
        }

        fun openPhoneCall(context: Context, phoneNumnber: String) {
            val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumnber))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }

        fun openMessage(context: Context, phoneNumnber: String) {
            val sendIntent = Intent(Intent.ACTION_VIEW)
            sendIntent.data = Uri.parse("sms:")
            context.startActivity(sendIntent)
        }

        fun getMessageFromRetrofitException(errorResponse: Throwable): String? {
            if (errorResponse is HttpException) {
                val httpException = (errorResponse as HttpException).response().errorBody()?.string()
                val json: JSONObject = JSONObject(httpException)
                val message: String = json?.get("message") as String
                return if (message != null) message else httpException
            }
            return "Oops, Something went wrong"
        }

        fun navigateToOnLandingActivity(context: Context?) {
            if (context != null) {
                val intentToLaunch = Intent(context, LandingPageActivity::class.java)
                intentToLaunch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intentToLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intentToLaunch)
            }
        }
    }
}