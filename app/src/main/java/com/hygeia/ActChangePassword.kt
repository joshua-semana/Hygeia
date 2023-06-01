package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hygeia.databinding.ActChangePasswordBinding

class ActChangePassword : AppCompatActivity() {
    private lateinit var bind : ActChangePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActChangePasswordBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {

            //NAVIGATION
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}