package com.hygeia

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.ui.setupWithNavController
import com.hygeia.databinding.ActMainBinding
import com.hygeia.objects.UserManager
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.firestore.FirebaseFirestore

class ActMain : AppCompatActivity() {
    private lateinit var bind : ActMainBinding

    private var db = FirebaseFirestore.getInstance()
    private var machineRef = db.collection("Machines")

    private val channelId = "recovery_channel_id"
    private val notificationId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        createNotificationChannel()

        machineRef.document("H0aATlx5WgNrn6C0YmgT").collection("Products")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) return@addSnapshotListener

                for (document in snapshot!!.documents) {
                    val data = document.get("Quantity")
                    if (data.toString() == "0") {
                        val name = document.get("Name").toString()
                        if (UserManager.role == "admin") {
                            showRestockNotification(name)
                        }
                    }
                }
            }


        with(bind) {

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.containerMain) as NavHostFragment
            val navController = navHostFragment.navController

            when (UserManager.role) {
                "super admin" -> {
                    actMainBotNavigation.inflateMenu(R.menu.menu_for_super)
                    navHostFragment.navController.graph = navController.navInflater.inflate(R.navigation.nav_for_super)
                }
                "admin" -> {
                    actMainBotNavigation.inflateMenu(R.menu.menu_for_admin)
                    navHostFragment.navController.graph = navController.navInflater.inflate(R.navigation.nav_for_admin)
                }
                "standard" -> {
                    actMainBotNavigation.inflateMenu(R.menu.menu_for_standard)
                    navHostFragment.navController.graph = navController.navInflater.inflate(R.navigation.nav_for_standard)
                }
            }
            actMainBotNavigation.setupWithNavController(
                containerMain.getFragment<NavHostFragment>().navController
            )
        }
    }

    private fun showRestockNotification(productName: String) {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_hygeia)
            .setContentTitle("Product Restock Needed")
            .setContentText("The $productName in Vendo No. 2023-0003 needs to be restocked.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(this@ActMain)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Product Restock"
            val descriptionText = "Channel for product restock notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onResume() {
        super.onResume()
        UserManager.isOnAnotherActivity = false
        UserManager.setUserOnline()
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            if (!UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        } else {
            if (!UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        }
    }
}