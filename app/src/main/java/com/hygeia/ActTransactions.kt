package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hygeia.databinding.ActTransactionsBinding

class ActTransactions : AppCompatActivity() {
    private lateinit var bind : ActTransactionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActTransactionsBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            //NAVIGATION
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}