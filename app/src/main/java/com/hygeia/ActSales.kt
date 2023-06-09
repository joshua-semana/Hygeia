package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.databinding.ActSalesBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.Utilities
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.util.Pair
import com.google.firebase.Timestamp
import com.hygeia.objects.Utilities.formatCredits

class ActSales : AppCompatActivity() {
    private lateinit var bind: ActSalesBinding
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var transactionsRef = db.collection("Transactions")

    private val dateFormat = SimpleDateFormat("MMMM d, y", Locale.getDefault())

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActSalesBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActSales)
        setContentView(bind.root)

        populateDescription()
        getTotalEarningsForDay(Timestamp(Calendar.getInstance().time))

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setSelection(
                    Pair(
                        MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                    )
                )
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        with(bind) {
            btnDate.setOnClickListener {
                loading.show()
                datePicker.show(supportFragmentManager, "Date")
                datePicker.addOnPositiveButtonClickListener { selectedDate ->
                    loading.dismiss()
                    if (dateFormat.format(Date(selectedDate)) == dateFormat.format(Calendar.getInstance().time)) {
                        lblDate.text = "Date: Today"
                    } else {
                        lblDate.text = "Date: ${dateFormat.format(Date(selectedDate))}"
                    }
                    getTotalEarningsForDay(Timestamp(Date(selectedDate)))
                }
                datePicker.addOnNegativeButtonClickListener { loading.dismiss() }
                datePicker.addOnCancelListener { loading.dismiss() }
            }

            btnMultiDate.setOnClickListener {
                loading.show()
                dateRangePicker.show(supportFragmentManager, "Date Range")
                dateRangePicker.addOnPositiveButtonClickListener { selection ->
                    loading.dismiss()
                    val startDate = selection.first ?: 0L
                    val endDate = selection.second ?: 0L
                    lblDate.text = "Date: ${dateFormat.format(Date(startDate))} - ${
                        dateFormat.format(
                            Date(endDate)
                        )
                    }"
                    getTotalEarningsForDays(Timestamp(Date(startDate)), Timestamp(Date(endDate)))
                }
                dateRangePicker.addOnNegativeButtonClickListener { loading.dismiss() }
                dateRangePicker.addOnCancelListener { loading.dismiss() }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateDescription() {
        bind.lblDescVendoID.text = "Vendo No. ${MachineManager.name}"
        bind.lblDescVendoLocation.text = "Located at ${MachineManager.location}"
        bind.lblDate.text = "Date: Today"
    }

    @SuppressLint("SetTextI18n")
    private fun getTotalEarningsForDay(selectedDate: Timestamp) {
        loading.show()
        val query = transactionsRef.whereEqualTo("Vendo", MachineManager.name)
            .whereEqualTo("Type", "Purchase")
            .whereGreaterThanOrEqualTo("Date Created", selectedDate.startOfDay())
            .whereLessThan("Date Created", selectedDate.endOfDay())

        query.get().apply {
            addOnSuccessListener { data ->
                loading.dismiss()
                var totalAmount = 0.0
                var totalProducts = 0.0

                if (data.isEmpty) {
                    bind.lblTotalEarningsAmount.text = formatCredits(0)
                    bind.lblTotalItemsSoldAmount.text = "None"
                } else {
                    for (document in data) {
                        val amount = document.getDouble("Amount")
                        val quantity = document.getDouble("Number")
                        totalAmount += amount!!
                        totalProducts += quantity!!
                    }
                    bind.lblTotalEarningsAmount.text = formatCredits(totalAmount)
                    bind.lblTotalItemsSoldAmount.text = "${totalProducts.toInt()} product(s)"
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getTotalEarningsForDays(firstDate: Timestamp, secondDate: Timestamp) {
        loading.show()
        val query = transactionsRef.whereEqualTo("Vendo", MachineManager.name)
            .whereEqualTo("Type", "Purchase")
            .whereGreaterThanOrEqualTo("Date Created", firstDate.startOfDay())
            .whereLessThan("Date Created", secondDate.endOfDay())

        query.get().apply {
            addOnSuccessListener { data ->
                loading.dismiss()
                var totalAmount = 0.0
                var totalProducts = 0.0

                if (data.isEmpty) {
                    bind.lblTotalEarningsAmount.text = formatCredits(0)
                    bind.lblTotalItemsSoldAmount.text = "None"
                } else {
                    for (document in data) {
                        val amount = document.getDouble("Amount")
                        val quantity = document.getDouble("Number")
                        totalAmount += amount!!
                        totalProducts += quantity!!
                    }
                    bind.lblTotalEarningsAmount.text = formatCredits(totalAmount)
                    bind.lblTotalItemsSoldAmount.text = "${totalProducts.toInt()} product(s)"
                }
            }
        }
    }

    private fun Timestamp.startOfDay(): Timestamp {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = this@startOfDay.seconds * 1000 + this@startOfDay.nanoseconds / 1000000
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(calendar.time)
    }

    private fun Timestamp.endOfDay(): Timestamp {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = this@endOfDay.seconds * 1000 + this@endOfDay.nanoseconds / 1000000
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return Timestamp(calendar.time)
    }
}