package com.user.ncard.ui.card.namecard

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.view.*
import com.user.ncard.BuildConfig
import com.user.ncard.R
import com.user.ncard.databinding.FragmentFullscreenImageViewBinding
import com.user.ncard.util.Constants
import com.user.ncard.util.Utils
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class FullScreenImageViewFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentFullscreenImageViewBinding
    var shareActionProvider: ShareActionProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_fullscreen_image_view, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.empty_string, R.color.transparent)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageSource = arguments.get(ARGUMENT_LOGO_SOURCE) as ImageSource
        when {
            imageSource.resourceId != Constants.BIGGEST_INT -> viewDataBinding.source = imageSource.resourceId
            imageSource.url.isNotEmpty() -> viewDataBinding.source = imageSource.url
            else -> viewDataBinding.source = imageSource.uri
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_share, menu)
        val item = menu?.findItem(R.id.menu_item_share)
        shareActionProvider = MenuItemCompat.getActionProvider(item) as ShareActionProvider
        shareActionProvider?.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        val imageSource = arguments.get(ARGUMENT_LOGO_SOURCE) as ImageSource
        when {
            imageSource.uri != null -> {
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageSource.uri)
                shareActionProvider?.setShareIntent(shareIntent)
            }
            imageSource.resourceId != Constants.BIGGEST_INT -> {
                val uri = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + imageSource.resourceId)
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareActionProvider?.setShareIntent(shareIntent)
            }
            else -> {
                GetBitmapTask(activity.applicationContext, shareActionProvider).execute(imageSource.url)
            }
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

    class GetBitmapTask(val context: Context, val shareActionProvider: ShareActionProvider?) : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg params: String?): Bitmap? {
            return try {
                val url = URL(params[0])
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            if (bitmap != null) {
                val path = MediaStore.Images.Media.insertImage(context.contentResolver,
                        bitmap, "Image Description", null)
                val uri = Uri.parse(path)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareActionProvider?.setShareIntent(shareIntent)
            }
        }
    }

    companion object {
        private const val ARGUMENT_LOGO_SOURCE = "IMAGE"

        fun newInstance(image: ImageSource) = FullScreenImageViewFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_LOGO_SOURCE, image)
            }
        }

    }
}