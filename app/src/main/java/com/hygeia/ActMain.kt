package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.hygeia.databinding.ActMainBinding
import com.hygeia.objects.UserManager

class ActMain : AppCompatActivity() {
    private lateinit var bind : ActMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            actMainBotNavigation.setupWithNavController(
                containerMain.getFragment<NavHostFragment>().navController
            )

            val navAdmin = actMainBotNavigation.menu.findItem(R.id.frgMainAdminTools)
            if (UserManager.role == "admin") {
                navAdmin.isVisible = true
            } else if (UserManager.role == "standard") {
                navAdmin.isVisible = false
            }
        }

    }
}