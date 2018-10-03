package com.user.ncard.ui.card.namecard

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.rd.PageIndicatorView
import com.user.ncard.R
import com.user.ncard.vo.NameCard
import kotlinx.android.synthetic.main.activity_name_card_detail.*


class NameCardDetailActivity : AppCompatActivity() {
    var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_card_detail)

        if (supportActionBar == null) {
            setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
        }
        var nameCard: NameCard? = null
        var profileUri: Uri? = null
        var frontUri: Uri? = null
        var backUri: Uri? = null
        var logoUriList: ArrayList<ImageSource>? = null
        var mediaUriList: ArrayList<ImageSource>? = null
        if (intent.hasExtra("namecard"))
            nameCard = intent.getParcelableExtra("namecard")
        //if has extra profileUri then this is preview. else, it is NameCardDetail
        if (intent.hasExtra("profileUri")) {
            profileUri = intent.getParcelableExtra("profileUri")
            frontUri = intent.getParcelableExtra("frontUri")
            backUri = intent.getParcelableExtra("backUri")
            logoUriList = intent.getParcelableArrayListExtra("logoUri")
            mediaUriList = intent.getParcelableArrayListExtra("mediaUri")
        }

        Glide.with(this).asBitmap().load(nameCard?.backgroundUrl).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                val drawable = BitmapDrawable(applicationContext.resources, resource)
                relativeLayout.background = drawable
            }
        })

        val toolbar = supportActionBar
        toolbar?.setDisplayHomeAsUpEnabled(true)

        val pager = findViewById<ViewPager>(R.id.viewPager)
        val pageIndicatorView = findViewById<PageIndicatorView>(R.id.pageIndicatorView)
        pager.adapter = NameCardDetailAdapter(supportFragmentManager, nameCard, profileUri, logoUriList, mediaUriList, frontUri, backUri)
        pageIndicatorView.setViewPager(pager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!intent.hasExtra("isFromUserJob")) {
            val inflater = menuInflater
            inflater.inflate(R.menu.menu_more, menu)
            menuItem = menu?.findItem(R.id.menu_item_more)
            menuItem?.isVisible = !intent.hasExtra("profileUri")
            val actionView = menuItem?.actionView
            val isMyNameCard = intent.hasExtra("isMyNameCard")
            actionView?.setOnClickListener {
                val intentNameCardMore = Intent(this@NameCardDetailActivity, NameCardMoreActivity::class.java)
                //for navigation in in name card more fragment
                intentNameCardMore.putExtra("namecard", intent.getParcelableExtra<NameCard>("namecard"))
                intentNameCardMore.putExtra("isMyNameCard", isMyNameCard)
                intentNameCardMore.putExtra("fromJobIcon", intent.getBooleanExtra("fromJobIcon", false))
                startActivity(intentNameCardMore)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}