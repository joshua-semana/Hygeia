package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.hygeia.databinding.ActMainBinding

class ActMain : AppCompatActivity() {
    private lateinit var bind : ActMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
//            actMainBotNavigation.setupWithNavController(
//                containerMain.getFragment<NavHostFragment>().navController
//            )
        }

    }
}