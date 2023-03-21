package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ToggleButton

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val txtNewPassword = findViewById<EditText>(R.id.txtNewPassword)
        val tglShowPassword = findViewById<ToggleButton>(R.id.tglShowPassword)

        tglShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                txtNewPassword.transformationMethod = null
                tglShowPassword.setBackgroundResource(R.drawable.ic_visibility_off)
                txtNewPassword.setSelection(txtNewPassword.text.length)
            } else {
                txtNewPassword.transformationMethod = PasswordTransformationMethod()
                tglShowPassword.setBackgroundResource(R.drawable.ic_visibility)
                txtNewPassword.setSelection(txtNewPassword.text.length)
            }
        }
    }
}