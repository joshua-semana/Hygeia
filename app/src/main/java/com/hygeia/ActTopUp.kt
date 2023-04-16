package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hygeia.databinding.ActTopUpBinding

class ActTopUp : AppCompatActivity() {
    private lateinit var bind : ActTopUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActTopUpBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            //NAVIGATION
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}