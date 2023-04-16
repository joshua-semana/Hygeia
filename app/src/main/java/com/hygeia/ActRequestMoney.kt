package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hygeia.databinding.ActRequestMoneyBinding

class ActRequestMoney : AppCompatActivity() {
    private lateinit var bind : ActRequestMoneyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActRequestMoneyBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}