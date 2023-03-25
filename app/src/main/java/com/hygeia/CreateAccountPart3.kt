package com.hygeia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import com.hygeia.databinding.ActivityCreateAccountPart3Binding

class CreateAccountPart3 : AppCompatActivity() {
    private lateinit var bind : ActivityCreateAccountPart3Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCreateAccountPart3Binding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            //ELEMENT BEHAVIOR
            bind.tglShowPassword.setOnCheckedChangeListener{ _, isChecked ->
                if (isChecked) {
                    bind.txtReviewPassword.transformationMethod = null
                } else {
                    bind.txtReviewPassword.transformationMethod = PasswordTransformationMethod()
                }
            }
        }

        //NAVIGATION
        bind.btnBackToCreateAccountPart2.setOnClickListener {
            startActivity(Intent(this, CreateAccountPart2::class.java))
        }
    }
}