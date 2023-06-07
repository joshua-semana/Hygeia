package com.hygeia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.hygeia.databinding.ActMainBinding
import com.hygeia.objects.UserManager
import android.view.KeyEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.hygeia.fragments.FrgMainHome

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
            val navUsers = actMainBotNavigation.menu.findItem(R.id.frgMainUserAccounts)

            when (UserManager.role) {
                "super admin" -> {
                    navAdmin.isVisible = false
                    navUsers.isVisible = true
                }
                "admin" -> {
                    navAdmin.isVisible = true
                    navUsers.isVisible = false
                }
                "standard" -> {
                    navAdmin.isVisible = false
                    navUsers.isVisible = false
                }
            }
        }
    }
}