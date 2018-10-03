package com.user.ncard.ui.card.namecard

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentSetRemarkNameCardBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.discovery.DiscoveryNavigation
import com.user.ncard.util.Utils
import com.user.ncard.vo.NameCard
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class SetRemarkNameCardFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: SetRemarkNameCardViewModel
    private lateinit var viewDataBinding: FragmentSetRemarkNameCardBinding
    lateinit var nameCard: NameCard
    lateinit var updateRemarkAlert: AlertDialog
    lateinit var genderAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_set_remark_name_card, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.remark)

        initSelectors()

        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SetRemarkNameCardViewModel::class.java)
        nameCard = arguments.getParcelable(ARGUMENT_NAME_CARD)
        viewDataBinding.viewmodel = viewModel
        viewDataBinding.viewmodel.apply {
            remark.set(nameCard.remark)
            gender.set(nameCard.gender ?: "")
            country.set(nameCard.country ?: "")
            nationality.set(nameCard.nationality ?: "")
            industry.set(nameCard.industry ?: "")
            if (!nameCard.birthday.isNullOrEmpty()) {
                val newFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val serverFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                try {
                    val d = serverFormat.parse(nameCard.birthday)
                    birthday.set(newFormat.format(d))
                } catch (ex: ParseException) {
                    Log.e("NCard", "Unable to parse date")
                }
            }
        }
        viewModel.successEvent.observe(this@SetRemarkNameCardFragment, Observer {
            updateRemarkAlert.cancel()
            fragmentManager.popBackStack()
        })
    }

    private fun initSelectors() {
        viewDataBinding.gender = object : DiscoveryNavigation(getString(R.string.gender), 0) {
            override fun onClick(view: View) {
                showDialogMessage()
            }
        }
        viewDataBinding.industry = object : DiscoveryNavigation(getString(R.string.industry), 0) {
            override fun onClick(view: View) {
                val intent = Intent(activity, NameCardRemarkActivity::class.java)
                intent.putExtra("industry", true)
                startActivityForResult(intent, REQUEST_INDUSTRY)
            }
        }
        viewDataBinding.nationality = object : DiscoveryNavigation(getString(R.string.nationality), 0) {
            override fun onClick(view: View) {
                val intent = Intent(activity, NameCardRemarkActivity::class.java)
                intent.putExtra("nationality", true)
                startActivityForResult(intent, REQUEST_NATIONALITY)
            }
        }
        viewDataBinding.country = object : DiscoveryNavigation(getString(R.string.country), 0) {
            override fun onClick(view: View) {
                val intent = Intent(activity, NameCardRemarkActivity::class.java)
                intent.putExtra("country", true)
                startActivityForResult(intent, REQUEST_COUNTRY)
            }
        }
        viewDataBinding.birthday = object : DiscoveryNavigation(getString(R.string.birthday), 0) {
            override fun onClick(view: View) {
                val datePickerFragment = DatePickerFragment()
                datePickerFragment.show(activity.fragmentManager, "datePicker")
            }

            @SuppressLint("ValidFragment")
            inner class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

                override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                    // Use the current date as the default date in the picker
                    val c = Calendar.getInstance()
                    val year = c.get(Calendar.YEAR)
                    val month = c.get(Calendar.MONTH)
                    val day = c.get(Calendar.DAY_OF_MONTH)

                    // Create a new instance of DatePickerDialog and return it
                    return DatePickerDialog(activity, this, year, month, day)
                }

                override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
                    val currentFormat = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
                    val newFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val dayString = if (day.toString().length == 1) "0" + day else day.toString()
                    val monthLiteral = month + 1
                    val monthString = if (monthLiteral.toString().length == 1) "0" + monthLiteral else monthLiteral.toString()
                    val selectedDateString = "$dayString $monthString $year"
                    try {
                        val d = currentFormat.parse(selectedDateString)
                        viewDataBinding.textViewBirthday.apply {
                            text = newFormat.format(d)
                            visibility = View.VISIBLE
                        }
                    } catch (ex: ParseException) {
                        Log.e("NCard", "Unable to parse date")
                    }

                }
            }
        }
    }

    private fun showDialogMessage() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_choose_gender, null)
        builder.setView(v)
        val numberPicker = v.findViewById<NumberPicker>(R.id.picker)
        numberPicker.apply {
            minValue = 0
            maxValue = 1
            displayedValues = arrayOf("male", "female")
        }
        val textViewCancel = v.findViewById<TextView>(R.id.textViewCancel)
        textViewCancel.setOnClickListener {
            genderAlert.cancel()
        }
        val textViewDone = v.findViewById<TextView>(R.id.textViewDone)
        textViewDone.setOnClickListener {
            genderAlert.cancel()
            viewDataBinding.textViewGender.apply {
                text = if (numberPicker.value == 0) "male" else "female"
                visibility = View.VISIBLE
            }
        }
        genderAlert = builder.create()
        val window: Window = genderAlert.window
        val attributes = window.attributes
        attributes.gravity = Gravity.BOTTOM
        attributes.y = 50
        attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = attributes
        window.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        genderAlert.show()

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_save -> {
                updateRemarkAlert = Utils.showAlert(activity)
                viewModel.updateRemark(nameCard)
            }
            android.R.id.home -> {
                if (fragmentManager.backStackEntryCount > 0)
                    fragmentManager.popBackStack()
                else NavUtils.navigateUpFromSameTask(activity)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_NATIONALITY -> viewDataBinding.textViewNationality.apply {
                    text = data?.getStringExtra("nationality") ?: ""
                    visibility = View.VISIBLE
                }
                REQUEST_COUNTRY -> viewDataBinding.textViewCountry.apply {
                    text = data?.getStringExtra("country") ?: ""
                    visibility = View.VISIBLE
                }
                REQUEST_INDUSTRY -> viewDataBinding.textViewIndustry.apply {
                    text = data?.getStringExtra("industry") ?: ""
                    visibility = View.VISIBLE
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"
        private const val REQUEST_NATIONALITY = 101
        private const val REQUEST_COUNTRY = 102
        private const val REQUEST_INDUSTRY = 103

        fun newInstance(nameCard: NameCard) = SetRemarkNameCardFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_NAME_CARD, nameCard)
            }
        }
    }
}