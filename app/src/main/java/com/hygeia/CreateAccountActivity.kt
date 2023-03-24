package com.hygeia

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.hygeia.databinding.ActivityCreateAccountBinding
import java.util.*

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var bind: ActivityCreateAccountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = Firebase.auth

        bind.btnBackCreateAccount.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }
        bind.btnContinueCreateAccount.setOnClickListener{
            val uid = UUID.randomUUID().toString().uppercase()
            val gender = bind.cmbGender.selectedItem.toString()
            val email = bind.txtNewEmail.text.toString()
            val firstname = bind.txtUsername.text.toString()
            val birthdate = bind.txtNewBirthDate.text.toString()
            val lastname = bind.txtNewLastName.text.toString()
            val phone = bind.txtNewPhoneNumber.text.toString()
            val password = bind.txtNewPassword.text.toString()
            val role = "standard"

            database = FirebaseDatabase.getInstance().getReference("Users")
            val newUser = User(uid, gender, firstname, lastname, birthdate, email, phone, password, role)
            database.child(uid).setValue(newUser).addOnSuccessListener {
                Toast.makeText(this, "Successfully registered", Toast.LENGTH_SHORT).show()
                resetTextFields()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }
        bind.tglShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                bind.txtNewPassword.transformationMethod = null
                bind.txtNewPassword.setSelection(bind.txtNewPassword.text.length)
            } else {
                bind.txtNewPassword.transformationMethod = PasswordTransformationMethod()
                bind.txtNewPassword.setSelection(bind.txtNewPassword.text.length)
            }
        }

        bind.txtNewBirthDate.setOnClickListener { bind.btnCalendar.performClick() }
        bind.btnCalendar.setOnClickListener { showDatePickerDialog() }
    }

    private fun resetTextFields(){
        bind.txtNewBirthDate.text.clear()
        bind.txtNewEmail.text.clear()
        bind.txtUsername.text.clear()
        bind.cmbGender.setSelection(1)
        bind.txtNewLastName.text.clear()
        bind.txtNewPassword.text.clear()
        bind.txtNewPhoneNumber.text.clear()
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
