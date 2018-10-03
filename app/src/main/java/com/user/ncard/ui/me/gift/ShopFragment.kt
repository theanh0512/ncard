package com.user.ncard.ui.me.gift

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bigbangbutton.editcodeview.EditCodeView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentShopBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.me.ewallet.ForgetPasswordFragment
import com.user.ncard.ui.me.ewallet.roundTo2DecimalPlaces
import com.user.ncard.util.Utils
import com.user.ncard.vo.DisplayCategory
import com.user.ncard.vo.GiftItem
import com.user.ncard.vo.Product
import javax.inject.Inject


class ShopFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GiftViewModel
    private lateinit var viewDataBinding: FragmentShopBinding

    var processingAlert: AlertDialog? = null
    lateinit var inputPasswordAlert: AlertDialog
    lateinit var wrongPasswordAlert: AlertDialog
    lateinit var itemPurchasedAlert: AlertDialog
    lateinit var lackOfMoneyAlert: AlertDialog
    lateinit var noStockAlert: AlertDialog
    lateinit var lowStockAlert: AlertDialog

    var currentPassword: String? = null
    var currentBalance: Double? = null

    val displayCategoryList = ArrayList<DisplayCategory>()
    lateinit var displayCategoryAdapter: DisplayCategoryAdapter
    lateinit var buyAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_shop, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.shop, R.color.colorWhite200)
        return viewDataBinding.root
    }

    //as returning back to the MyGiftFragment will result exception, we start new activity instead
    override fun onResume() {
        super.onResume()
        if (view == null) {
            return
        }

        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                activity.finish()
                startActivity(Intent(activity, MyGiftActivity::class.java))
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GiftViewModel::class.java)
        viewModel.start.value = true
        processingAlert = Utils.showAlert(activity)
        displayCategoryAdapter = DisplayCategoryAdapter(object : DisplayCategoryAdapter.OnClickCallBack {
            override fun onClick(displayCategory: DisplayCategory) {
            }
        }, object : DisplayCategoryAdapter.OnItemBuyClickCallBack {
            override fun onClick(giftItem: GiftItem) {
                showDialogMessage(giftItem)
            }
        }, object : DisplayCategoryAdapter.OnItemClickCallBack {
            override fun onClick(giftItem: GiftItem) {
                val productDetailFragment = ProductDetailFragment.newInstance(giftItem)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, productDetailFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        })
        viewModel.itemsList.observe(this@ShopFragment, Observer { itemsList ->
            if (itemsList != null) {
                processingAlert?.cancel()
                displayCategoryList.clear()
            }
            itemsList?.forEach { giftItem ->
                giftItem.categories?.forEach { category ->
                    //if the category has been added to the list, add the item to the gift list
                    val filteredList = displayCategoryList.filter { it.category == category }
                    if (filteredList.isNotEmpty()) {
                        filteredList[0].itemList.add(giftItem)
                    } else {
                        val giftItems = ArrayList<GiftItem>()
                        giftItems.add(giftItem)
                        displayCategoryList.add(DisplayCategory(category, giftItems))
                    }
                }
            }
            displayCategoryAdapter.replace2(displayCategoryList)
            displayCategoryAdapter.notifyDataSetChanged()
        })
        viewDataBinding.recyclerViewCategory.apply {
            adapter = displayCategoryAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        viewModel.walletInfo.observe(this@ShopFragment, Observer {
            if (it != null) {
                viewModel.getShopItem.value = true
                currentPassword = it.walletPassword
                currentBalance = it.balance.amount.toDouble()
            }
        })
        viewModel.createOrderResponse.observe(this@ShopFragment, Observer {
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
                activity.finish()
                startActivity(Intent(activity, MyGiftActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val REQUEST_CODE_SELECT_SOURCE = 55
    }
}