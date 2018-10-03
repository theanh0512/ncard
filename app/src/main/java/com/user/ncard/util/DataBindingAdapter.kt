package com.user.ncard.util

import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.DateUtils
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.views.RelativeTimeTextView
import com.user.ncard.ui.me.ewallet.roundTo2DecimalPlaces
import com.user.ncard.ui.me.gift.ItemAdapter
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("imageRes")
fun setImageResource(view: ImageView, resId: Int) {
    if (resId == Constants.BIGGEST_INT || resId == 0) view.setImageDrawable(null)
    else
        Glide.with(view.context).load(resId).into(view)
}

@BindingAdapter("imageUri")
fun setImageUri(view: ImageView, uri: Uri?) {
    if (uri == null) {
        view.setImageDrawable(null)
    } else {
        Glide.with(view.context).load(uri).into(view)
    }
}

@BindingAdapter("imageSource")
fun setImageAny(view: ImageView, source: Any?) {
    if (source == null) {
        view.setImageDrawable(null)
    } else {
        Glide.with(view.context).load(source).into(view)
    }
}

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, url: String?) {
    if (url == null) {
        view.setImageDrawable(view.context.getDrawable(R.drawable.image_place_holder))
    } else {
        Glide.with(view.context).load(url).apply(RequestOptions
                .circleCropTransform()
                .placeholder(R.drawable.image_place_holder)
                .fallback(R.drawable.image_place_holder)
                .error(R.drawable.image_place_holder)).into(view)
    }
}

@BindingAdapter("nameCardImageUrl")
fun setNameCardImageUrl(view: ImageView, url: String?) {
    if (url == null) {
        view.setImageDrawable(view.context.getDrawable(R.drawable.ic_name_card_holder))
    } else {
        Glide.with(view.context).load(url).apply(RequestOptions
                .circleCropTransform()
                .placeholder(R.drawable.ic_name_card_holder)
                .fallback(R.drawable.ic_name_card_holder)
                .error(R.drawable.ic_name_card_holder)).into(view)
    }
}

@BindingAdapter("imageUrlRectangle")
fun setImageUrlRectangle(view: ImageView, url: String?) {
    if (url == null) {
        view.setImageDrawable(null)
    } else {
        Glide.with(view.context).load(url).apply(RequestOptions
                .noTransformation()
                .placeholder(R.drawable.image_place_holder)
                .fallback(R.drawable.image_place_holder)
                .error(R.drawable.image_place_holder)).into(view)
    }
}

@BindingAdapter("imageUrlFront")
fun setImageUrlFront(view: ImageView, url: String?) {
    if (url == null) {
        view.setImageDrawable(null)
    } else {
        Glide.with(view.context).load(url).apply(RequestOptions
                .noTransformation()
                .placeholder(R.drawable.ic_card_front)
                .fallback(R.drawable.ic_card_front)
                .error(R.drawable.ic_card_front)).into(view)
    }
}

@BindingAdapter("imageUrlBack")
fun setImageUrlBack(view: ImageView, url: String?) {
    if (url == null) {
        view.setImageDrawable(null)
    } else {
        Glide.with(view.context).load(url).apply(RequestOptions
                .noTransformation()
                .placeholder(R.drawable.ic_card_back)
                .fallback(R.drawable.ic_card_back)
                .error(R.drawable.ic_card_back)).into(view)
    }
}

@BindingAdapter("imageUrlList")
fun setImageUrlList(view: ImageView, urlList: List<String>?) {
    if (urlList == null || urlList.isEmpty()) {
        view.setImageDrawable(null)
    } else {
        Glide.with(view.context).load(urlList[0]).apply(RequestOptions
                .noTransformation()
                .placeholder(R.drawable.image_place_holder)
                .fallback(R.drawable.image_place_holder)
                .error(R.drawable.image_place_holder)).into(view)
    }
}

@BindingAdapter("itemList")
fun setItemList(view: RecyclerView, itemList: ArrayList<GiftItem>?) {
    val itemAdapter = ItemAdapter(object : ItemAdapter.OnClickCallback {
        override fun onClick(item: GiftItem) {

        }
    }, object : ItemAdapter.onBuyClickCallBack {
        override fun onClick(item: GiftItem) {

        }

    })
    view.apply {
        adapter = itemAdapter
        layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
    }
    if (itemList != null) {
        itemAdapter.replace(itemList)
        itemAdapter.notifyDataSetChanged()
    }
}

@BindingAdapter("android:layout_marginStart")
fun setStartMargin(view: View, startMargin: Float) {
    val layoutParams = view.layoutParams as MarginLayoutParams
    layoutParams.marginStart = Math.round(startMargin)
    view.layoutParams = layoutParams
}

@BindingAdapter("date")
fun setText(textView: TextView, charSequence: CharSequence?) {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val newFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
    try {
        val d = simpleDateFormat.parse(charSequence.toString())
        textView.text = newFormat.format(d)
    } catch (ex: ParseException) {
        Log.e("NCard", "Unable to parse date")
    }
}

@BindingAdapter("dateJob")
fun setTextDate(textView: TextView, charSequence: CharSequence?) {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val newFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    try {
        val d = simpleDateFormat.parse(charSequence.toString())
        textView.text = newFormat.format(d)
    } catch (ex: ParseException) {
        Log.e("NCard", "Unable to parse date")
    }
}

@BindingAdapter("dateDMY")
fun setTextDMY(textView: TextView, charSequence: CharSequence?) {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val newFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    try {
        val d = simpleDateFormat.parse(charSequence.toString())
        textView.text = newFormat.format(d)
    } catch (ex: ParseException) {
        Log.e("NCard", "Unable to parse date")
    }
}

@BindingAdapter("dateTime")
fun setTextDateTime(textView: TextView, charSequence: CharSequence?) {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val newFormat = SimpleDateFormat("MMM dd yyyy HH:mm", Locale.getDefault())
    simpleDateFormat.timeZone = TimeZone.getTimeZone(RelativeTimeTextView.TIME_ZONE)
    try {
        val d = simpleDateFormat.parse(charSequence.toString())
        textView.text = newFormat.format(d)
    } catch (ex: ParseException) {
        Log.e("NCard", "Unable to parse date")
    }
}

@BindingAdapter("jobDateTime")
fun setTextJobDate(textView: TextView, job: Job?) {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val newFormat = SimpleDateFormat("yyyy.MM", Locale.getDefault())
    simpleDateFormat.timeZone = TimeZone.getTimeZone(RelativeTimeTextView.TIME_ZONE)
    try {
        if (job != null) {
            val from = newFormat.format(simpleDateFormat.parse(job.from))
            val to = if (job.to == null || job.to!!.isEmpty()) textView.context.getString(R.string.present) else newFormat.format(simpleDateFormat.parse(job.to))
            textView.text = textView.context.getString(R.string.display_job_time, from, to)
        }
    } catch (ex: ParseException) {
        Log.e("NCard", "Unable to parse date")
    }
}

@BindingAdapter("formatTransactionAmount")
fun setAmount(textView: TextView, charSequence: CharSequence?) {
    if (charSequence != null) {
        val amount = if (charSequence.contains("-")) charSequence.subSequence(1, charSequence.lastIndex + 1).toString() else charSequence.toString()
        if (charSequence.contains("-")) textView.text =
                textView.context.getString(R.string.display_transaction_amount_minus,
                        textView.context.getString(R.string.display_balance, amount.toDouble().roundTo2DecimalPlaces().toString()))
        else textView.text =
                textView.context.getString(R.string.display_transaction_amount_plus,
                        textView.context.getString(R.string.display_balance, amount.toDouble().roundTo2DecimalPlaces().toString()))
    }
}

@BindingAdapter("formatStatus")
fun setStatus(textView: TextView, transaction: TransactionLog?) {
    val context = textView.context
    val status = transaction?.status
    val type = transaction?.type
    when (type) {
        "transfer" -> textView.text = when (status) {
            EWalletTransactionStatusType.REFUNDED.status -> context.getText(R.string.credit_status_refunded)
            EWalletTransactionStatusType.ONHOLD.status -> context.getText(R.string.succeeded)
            else -> ""
        }
        "withdraw" -> textView.text = when (status) {
            EWalletTransactionStatusType.REFUNDED.status -> context.getText(R.string.credit_status_refunded)
            else -> ""
        }
        else -> textView.text = context.getText(R.string.empty_string)
    }
}

@BindingAdapter("formatStatusLog", "showSender")
fun setStatusLog(textView: TextView, transaction: TransactionLogDetail?, showSender: Boolean?) {
    val context = textView.context
    val status = transaction?.status
    val type = transaction?.type
    when (type) {
        "transfer" -> textView.text = when (status) {
            EWalletTransactionStatusType.REFUNDED.status -> context.getText(R.string.credit_status_refunded)
            else -> if (showSender != null && showSender) context.getText(R.string.successful) else if (EWalletTransactionStatusType.ONHOLD.status == status) context.getText(R.string.succeeded) else context.getText(R.string.successful)
        }
        "withdraw" -> textView.text = when (status) {
            EWalletTransactionStatusType.REFUNDED.status -> context.getText(R.string.credit_status_refunded)
            else -> context.getText(R.string.succeeded)
        }
        else -> textView.text = context.getText(R.string.successful)
    }
}

@BindingAdapter("formatType")
fun setType(textView: TextView, type: String?) {
    when (type) {
        "transfer" -> textView.text = "Transfer"
        "deposit" -> textView.text = "Deposit"
        else -> textView.text = "Payment"
    }
}

@Suppress("DEPRECATION")
@BindingAdapter("fromHtml")
fun setTextFromHtml(textView: TextView, charSequence: CharSequence?) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        textView.text = Html.fromHtml(charSequence.toString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        textView.text = Html.fromHtml(charSequence.toString())
    }
}

@BindingAdapter("userFilter")
fun setTextFromFilter(textView: TextView, userFilter: UserFilter?) {
    if (userFilter != null) {
        var count = userFilter.country.size + userFilter.gender.size + userFilter.industry.size + userFilter.nationality.size
        textView.text = count.toString()
        textView.visibility = if (count == 0) View.INVISIBLE else View.VISIBLE
    } else textView.visibility = View.INVISIBLE
}

@BindingAdapter("qrCodeContent")
fun setQRCodeContent(view: ImageView, username: String?) {
    if (username.isNullOrEmpty()) {
        view.setImageDrawable(null)
    } else {
        view.setImageBitmap(generateQRBitMap(username!!, Color.BLACK))
    }
}

@BindingAdapter("qrCodeContentBlue")
fun setQRCodeContentBlue(view: ImageView, username: String?) {
    if (username.isNullOrEmpty()) {
        view.setImageDrawable(null)
    } else {
        view.setImageBitmap(generateQRBitMap(username!!, view.context.getColorFromResId(R.color.colorDarkBlue)))
    }
}

private fun generateQRBitMap(content: String, color: Int): Bitmap? {

    val hints = HashMap<EncodeHintType, ErrorCorrectionLevel>()

    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)

    val qrCodeWriter = QRCodeWriter()

    try {
        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {

                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) color else Color.WHITE)
            }
        }

        return bmp
    } catch (e: WriterException) {
        e.printStackTrace()
    }

    return null
}

@BindingAdapter("app:likes")
fun setLikesText(textView: TextView, likes: List<CatalogueLike>) {
    if (likes?.isNotEmpty()) {
        var string = ""
        likes.forEachIndexed { index, item ->
            string += item.ownerName + if (index == likes.size - 1) "" else ", "
        }
        textView.text = string
    }
}

@BindingAdapter("app:day")
fun setDayText(textView: TextView, date: String) {
    if (date?.isNotEmpty()) {
        textView.text = DateUtils.convertToDay(date)
    }
}

@BindingAdapter("app:month")
fun setMonthText(textView: TextView, date: String) {
    if (date?.isNotEmpty()) {
        textView.text = DateUtils.convertToMonth(date)
    }
}

@BindingAdapter("app:timeAgo")
fun setTimeAgo(textView: RelativeTimeTextView, time: String?) {
    if (time != null) {
        textView.setReferenceTime(time)
    } else {
        textView.text = ""
    }
}

@BindingAdapter("app:displayName")
fun setDisplayName(textView: TextView, user: User?) {
    if (user != null) {
        textView.text = textView.context.getString(R.string.display_name, user.firstName, user.lastName)
    } else {
        textView.text = ""
    }
}

@BindingAdapter("app:likeList")
fun setImageLike(imv: ImageView, imageLikes: List<CatalogueLike>) {
    if (imageLikes != null && imageLikes?.isNotEmpty() && Functions.isLike(SharedPreferenceHelper(imv.context), imageLikes)) {
        //DrawableCompat.setTint(imv.getDrawable(),  imv.context.getResources().getColor(R.color.colorDarkBlue))
        imv.setImageResource(R.drawable.ic_liked)
    } else {
        //DrawableCompat.setTint(imv.getDrawable(), imv.context.getResources().getColor(R.color.colorBlack))
        imv.setImageResource(R.drawable.ic_like)
    }
}

@BindingAdapter("android:layout_marginTop")
fun setLayoutMarginTop(view: View, margin: Float) {
    val resources = view.context.resources
    val metrics = resources.displayMetrics
    val marginInPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margin, metrics)
    val layoutParams = view.layoutParams as MarginLayoutParams
    layoutParams.setMargins(layoutParams.leftMargin, Math.round(marginInPixel),
            layoutParams.rightMargin, layoutParams.bottomMargin)
    view.layoutParams = layoutParams
}


