package com.user.ncard.ui.card.namecard

import android.Manifest
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import com.user.ncard.R
import com.user.ncard.databinding.FragmentNameCardLogoBinding
import com.user.ncard.util.Constants
import com.user.ncard.vo.NameCard
import io.reactivex.disposables.Disposable

class NameCardLogoFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentNameCardLogoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_name_card_logo, container, false)!!
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val logoAdapter = NameCardFromUrlAdapter(object : NameCardFromUrlAdapter.OnClickCallback {
            override fun onClick(url: String) {
                val rxPermissions = RxPermissions(activity)
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object : io.reactivex.Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(t: Boolean) {
                        if (t) {
                            val intent = Intent(activity, FullScreenImageViewActivity::class.java)
                            intent.putExtra("url", url)
                            startActivity(intent)
                        } else {
                            Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                    .show()
                        }
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}
                })
            }
        })
        val logoFromUriAdapter = ImageFromUriAdapter(object : ImageFromUriAdapter.OnClickCallback {
            override fun onClick(source: Any) {
                val rxPermissions = RxPermissions(activity)
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object : io.reactivex.Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(t: Boolean) {
                        if (t) {
                            val intent = Intent(activity, FullScreenImageViewActivity::class.java)
                            when (source) {
                                is Uri -> intent.putExtra("uri", source)
                                is String -> intent.putExtra("url", source)
                                is Int -> intent.putExtra("resourceId", source)
                            }
                            startActivity(intent)
                        } else {
                            Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                    .show()
                        }
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}
                })
            }
        })
        viewDataBinding.recyclerViewLogo.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 3)
        }
        val nameCard: NameCard = arguments.getParcelable(ARGUMENT_NAME_CARD)
        viewDataBinding.namecard = nameCard
        val uriList = arguments.getParcelableArrayList<ImageSource>(ARGUMENT_LOGO_URI)
        if (uriList != null) {
            logoFromUriAdapter.replace(uriList.map { imageSource ->
                when {
                    imageSource.resourceId != Constants.BIGGEST_INT -> imageSource.resourceId
                    imageSource.url.isNotEmpty() -> imageSource.url
                    else -> imageSource.uri
                }
            })
            viewDataBinding.recyclerViewLogo.adapter = logoFromUriAdapter
            logoFromUriAdapter.notifyDataSetChanged()
        } else if (nameCard.certUrls != null && nameCard.certUrls!!.isNotEmpty()) {
            logoAdapter.replace(nameCard.certUrls)
            viewDataBinding.recyclerViewLogo.adapter = logoAdapter
            logoAdapter.notifyDataSetChanged()
        }

    }

    companion object {
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"
        private const val ARGUMENT_LOGO_URI = "LOGO"

        fun newInstance(nameCard: NameCard?, logoUriList: ArrayList<ImageSource>?) = NameCardLogoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_NAME_CARD, nameCard)
                putParcelableArrayList(ARGUMENT_LOGO_URI, logoUriList)
            }
        }

    }
}