package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hygeia.databinding.ActNotificationsBinding

class ActNotifications : AppCompatActivity() {
    private lateinit var bind : ActNotificationsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActNotificationsBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            //NAVIGATION
            btnBack.setOnClickListener{
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}