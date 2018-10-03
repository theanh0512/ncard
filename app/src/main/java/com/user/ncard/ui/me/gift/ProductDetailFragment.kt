package com.user.ncard.ui.me.gift

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bigbangbutton.editcodeview.EditCodeView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentProductDetailBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.me.ewallet.ForgetPasswordFragment
import com.user.ncard.ui.me.ewallet.roundTo2DecimalPlaces
import com.user.ncard.util.Utils
import com.user.ncard.vo.GiftItem
import com.user.ncard.vo.Product
import kotlinx.android.synthetic.main.fragment_product_detail.*
import java.util.*
import javax.inject.Inject

class ProductDetailFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GiftViewModel
    private lateinit var viewDataBinding: FragmentProductDetailBinding
    lateinit var buyAlert: AlertDialog
    var processingAlert: AlertDialog? = null
    lateinit var inputPasswordAlert: AlertDialog
    lateinit var wrongPasswordAlert: AlertDialog
    lateinit var itemPurchasedAlert: AlertDialog
    lateinit var lackOfMoneyAlert: AlertDialog
    lateinit var noStockAlert: AlertDialog
    lateinit var lowStockAlert: AlertDialog

    var currentPassword: String? = null
    var currentBalance: Double? = null

    var productImagesPagerAdapter: ProductImagesPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_product_detail, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.product_detail, R.color.colorWhite246)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GiftViewModel::class.java)
        viewModel.start.value = true
        viewDataBinding.gift = arguments.getSerializable(ARGUMENT_ITEM) as GiftItem

        viewDataBinding.buttonBuy.setOnClickListener {
            showDialogMessage(arguments.getSerializable(ARGUMENT_ITEM) as GiftItem)
        }

        viewModel.walletInfo.observe(this@ProductDetailFragment, Observer {
            if (it != null) {
                viewModel.getShopItem.value = true
                currentPassword = it.walletPassword
                currentBalance = it.balance.amount.toDouble()
            }
        })

        viewModel.createOrderResponse.observe(this@ProductDetailFragment, Observer {
            if (it != null) {
                processingAlert?.cancel()
                if (it.statusAndMessage != null) {
                    Utils.showAlert(activity, it.statusAndMessage.message)
                    viewModel.getShopItem.value = true
                } else {
                    itemPurchasedAlert = Utils.showAlertWithCheckIcon(activity, getString(R.string.purchase_gift_success))
                    currentBalance = it.balance.amount.toDouble()
                    object : CountDownTimer(1000.toLong(), 1000.toLong()) {
                        override fun onFinish() {
                            itemPurchasedAlert.cancel()
                        }

                        override fun onTick(millisUntilFinished: Long) {
                        }

                    }.start()
                }
            }
        })

        if (viewDataBinding.gift?.imageUrls != null && viewDataBinding.gift?.imageUrls?.isNotEmpty()!!) {
            initListImages(viewDataBinding.gift?.imageUrls!!)
        }
    }

    internal var currentPage = 0
    internal var timer: Timer? = null

    fun initListImages(listImages: List<String>) {
        productImagesPagerAdapter = ProductImagesPagerAdapter(activity, listImages)
        viewPager?.setAdapter(productImagesPagerAdapter)

        /*After setting the adapter use the timer */
        val handler = Handler()
        val Update = Runnable {
            if (currentPage > listImages.size - 1) {
                currentPage = 0
            }
            viewPager?.setCurrentItem(currentPage++, true)
        }

        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentPage = position
                currentPage++
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        timer = Timer() // This will create a new Thread
        timer?.schedule(object : TimerTask() { // task to be scheduled

            override fun run() {
                handler.post(Update)
            }
        }, 500, 3000)
        pagerIndicator?.setCount(listImages.size)
        productImagesPagerAdapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    private fun showDialogMessage(giftItem: GiftItem) {
        val price = giftItem.price
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_buy_gift, null)
        builder.setView(v)
        var amount = 1
        var totalPrice = price?.amount!!
        val textViewTotalPrice = v.findViewById<TextView>(R.id.textViewAmount)
        textViewTotalPrice.text = getString(R.string.display_balance, totalPrice.roundTo2DecimalPlaces().toString())

        val minusButton = v.findViewById<ImageView>(R.id.imageViewMinus)
        val plusButton = v.findViewById<ImageView>(R.id.imageViewPlus)
        val textViewAmount = v.findViewById<TextView>(R.id.textViewTotalAmount)
        textViewAmount.text = amount.toString()
        minusButton.setOnClickListener {
            if (amount > 1) {
                amount--
                textViewAmount.text = amount.toString()
                totalPrice -= price.amount
                textViewTotalPrice.text = getString(R.string.display_balance, totalPrice.roundTo2DecimalPlaces().toString())
            }
        }
        plusButton.setOnClickListener {
            amount++
            textViewAmount.text = amount.toString()
            totalPrice += price.amount
            textViewTotalPrice.text = getString(R.string.display_balance, totalPrice.roundTo2DecimalPlaces().toString())
        }

        val textViewCancel = v.findViewById<TextView>(R.id.textViewCancel)
        textViewCancel.setOnClickListener {
            buyAlert.cancel()
        }
        val buttonMakePayment = v.findViewById<Button>(R.id.buttonMakePayment)
        buttonMakePayment.setOnClickListener {
            buyAlert.cancel()
            if (!giftItem.inStock!!) {
                noStockAlert = Utils.showAlert(activity, getString(R.string.sorry_out_of_stock))
            } else if (giftItem.stockQuantity!! < amount) {
                lowStockAlert = Utils.showAlert(activity, getString(R.string.only_item_left, giftItem.stockQuantity))
            } else {
                val products = ArrayList<Product>()
                products.add(Product(giftItem.id, amount))
                if (currentPassword != null) {
                    showDialogPasswordMessage(products, totalPrice)
                } else {
                    Functions.showAlertDialog(activity, "", getString(R.string.warn_credit_transfer_no_pass))
                }
            }
        }

        buyAlert = builder.create()
        val window: Window = buyAlert.window
        val attributes = window.attributes
        attributes.gravity = Gravity.BOTTOM
        attributes.y = 50
        attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = attributes
        window.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        buyAlert.show()

    }

    private fun showDialogPasswordMessage(products: List<Product>, totalPrice: Double) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_change_password, null)
        builder.setView(v)
        val editCodeView = v.findViewById<EditCodeView>(R.id.editTextPassword)
        editCodeView.setEditCodeListener { code ->
            if (currentPassword != null) {
                if (code == currentPassword) {
                    inputPasswordAlert.cancel()
                    if (totalPrice > currentBalance!!) {
                        lackOfMoneyAlert = Utils.showAlert(activity,
                                getString(R.string.not_enough_balance_to_request_transaction))
                    } else {
                        viewModel.createOrder(products, currentPassword!!)
                        processingAlert = Utils.showAlert(activity)
                    }
                } else {
                    inputPasswordAlert.cancel()
                    wrongPasswordAlert = Utils.showAlert(activity, getString(R.string.wrong_password))
                    object : CountDownTimer(2000.toLong(), 1000.toLong()) {
                        override fun onFinish() {
                            wrongPasswordAlert.cancel()
                        }

                        override fun onTick(millisUntilFinished: Long) {
                        }

                    }.start()
                }
            }
        }
        val forgetPasswordTextView = v.findViewById<TextView>(R.id.textViewForgetPassword)
        forgetPasswordTextView.setOnClickListener {
            inputPasswordAlert.cancel()
            val forgetPasswordFragment = ForgetPasswordFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, forgetPasswordFragment)
                    .addToBackStack("PaymentSecurityFragment")
                    .commitAllowingStateLoss()
        }
        inputPasswordAlert = builder.create()
        val window: Window = inputPasswordAlert.window
        val attributes = window.attributes
        attributes.gravity = Gravity.CENTER
        attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = attributes
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        inputPasswordAlert.show()

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

    companion object {
        private const val ARGUMENT_ITEM = "ITEM"

        fun newInstance(giftItem: GiftItem) = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARGUMENT_ITEM, giftItem)
            }
        }

    }
}