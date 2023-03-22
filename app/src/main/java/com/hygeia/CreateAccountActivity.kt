package com.hygeia

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import com.hygeia.databinding.ActivityCreateAccountBinding
import java.util.*

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var bind: ActivityCreateAccountBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.tglShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                bind.txtNewPassword.transformationMethod = null
                bind.tglShowPassword.setBackgroundResource(R.drawable.ic_visibility_off)
                bind.txtNewPassword.setSelection(bind.txtNewPassword.text.length)
            } else {
                bind.txtNewPassword.transformationMethod = PasswordTransformationMethod()
                bind.tglShowPassword.setBackgroundResource(R.drawable.ic_visibility)
                bind.txtNewPassword.setSelection(bind.txtNewPassword.text.length)
            }
        }

        bind.btnCalendar.setOnClickListener {
            showDatePickerDialog()
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
}