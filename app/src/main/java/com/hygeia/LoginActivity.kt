package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import com.hygeia.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var bind: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.tglShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                bind.txtPassword.transformationMethod = null
                bind.tglShowPassword.setBackgroundResource(R.drawable.ic_visibility_off)
                bind.txtPassword.setSelection(bind.txtPassword.text.length)
            } else {
                bind.txtPassword.transformationMethod = PasswordTransformationMethod()
                bind.tglShowPassword.setBackgroundResource(R.drawable.ic_visibility)
                bind.txtPassword.setSelection(bind.txtPassword.text.length)
            }
        }
    }
}