package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hygeia.databinding.ActSendMoneyBinding

class ActSendMoney : AppCompatActivity() {
    private lateinit var bind : ActSendMoneyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActSendMoneyBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {

            //NAVIGATION
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }

    }
}