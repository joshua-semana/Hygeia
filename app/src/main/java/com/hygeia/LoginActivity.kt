package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ToggleButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //val txtEmail = findViewById<EditText>(R.id.txtEmail)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)
        val tglShowPassword = findViewById<ToggleButton>(R.id.tglShowPassword)
        //val btnLogin = findViewById<Button>(R.id.btnLogin)

        tglShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                txtPassword.transformationMethod = null
                tglShowPassword.setBackgroundResource(R.drawable.ic_visibility_off)
                txtPassword.setSelection(txtPassword.text.length)
            } else {
                txtPassword.transformationMethod = PasswordTransformationMethod()
                tglShowPassword.setBackgroundResource(R.drawable.ic_visibility)
                txtPassword.setSelection(txtPassword.text.length)
            }
        }

//        fun updateButtonLoginState() {
//            btnLogin.isEnabled = txtEmail.text.isNotEmpty() && txtPassword.text.isNotEmpty()
//        }
//
//        txtEmail.addTextChangedListener { updateButtonLoginState() }
//        txtPassword.addTextChangedListener { updateButtonLoginState() }
    }
}