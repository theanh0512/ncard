package com.user.ncard.ui.card.namecard

import android.Manifest
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.tbruyelle.rxpermissions2.RxPermissions
import com.user.ncard.R
import com.user.ncard.databinding.FragmentNameCardBackBinding
import com.user.ncard.vo.NameCard
import io.reactivex.disposables.Disposable

class NameCardBackFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentNameCardBackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_name_card_back, container, false)!!
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val namecard = arguments.getParcelable<NameCard>(ARGUMENT_NAME_CARD)
        val imageUri = arguments.getParcelable<Uri>(ARGUMENT_IMAGE_URI)
        if (imageUri != null)
            Glide.with(activity).load(arguments.getParcelable(ARGUMENT_IMAGE_URI)).into(viewDataBinding.imageView)
        else Glide.with(activity).load(namecard.backUrl).into(viewDataBinding.imageView)
        viewDataBinding.textView55.text = namecard.company
        viewDataBinding.imageView.setOnClickListener {
            val rxPermissions = RxPermissions(activity)
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object : io.reactivex.Observer<Boolean> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(t: Boolean) {
                    if (t) {
                        val intent = Intent(activity, FullScreenImageViewActivity::class.java)
                        if (imageUri != null)
                            intent.putExtra("uri", imageUri)
                        else intent.putExtra("url", namecard.backUrl)
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
    }

    companion object {
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"
        private const val ARGUMENT_IMAGE_URI = "IMAGE_URI"

        fun newInstance(nameCard: NameCard?, imageUri: Uri?) = NameCardBackFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_NAME_CARD, nameCard)
                putParcelable(ARGUMENT_IMAGE_URI, imageUri)
            }
        }

    }
}