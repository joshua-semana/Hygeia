package com.hygeia

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.hygeia.databinding.ActivityCreateAccountPart1Binding
import java.util.*

class CreateAccountPart1 : AppCompatActivity() {
    private lateinit var bind: ActivityCreateAccountPart1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCreateAccountPart1Binding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.txtNewBirthDate.setOnClickListener { bind.btnCalendar.performClick() }
        bind.btnCalendar.setOnClickListener { showDatePickerDialog() }

        with(bind) {
            //ELEMENT BEHAVIOR
            layoutCreateAccountPart1.setOnClickListener {
                getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                layoutCreateAccountPart1.requestFocus()
                currentFocus?.clearFocus()
            }

            //VALIDATION
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    btnContinueCreateAccountToPart2.isEnabled = txtNewFirstName.text.isNotEmpty() and
                            txtNewLastName.text.isNotEmpty() and
                            txtNewBirthDate.text.isNotEmpty()
                }
            }

            txtNewFirstName.addTextChangedListener(textWatcher)
            txtNewLastName.addTextChangedListener(textWatcher)
            txtNewBirthDate.addTextChangedListener(textWatcher)
        }
        //NAVIGATION
        bind.btnBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        bind.btnContinueCreateAccountToPart2.setOnClickListener {
            startActivity(Intent(this, CreateAccountPart2::class.java))
        }
    }
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val calMonth = calendar.get(Calendar.MONTH)
        val calDay = calendar.get(Calendar.DAY_OF_MONTH)
        val calYear = calendar.get(Calendar.YEAR)
        val dlgDatePicker = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val date = (dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
                bind.txtNewBirthDate.setText(date)
            },
            calYear, calMonth, calDay
        )
        dlgDatePicker.show()
    }
    private fun resetTextFields(){
        with(bind) {
            cmbGender.setSelection(1)
            txtNewFirstName.text.clear()
            txtNewLastName.text.clear()
            txtNewBirthDate.text.clear()
        }
    }
}
