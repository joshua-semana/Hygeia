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
    private var notificationId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        createNotificationChannel()
        
        machineRef.whereEqualTo("isEnabled", true).addSnapshotListener { value, _ ->
            for (machine in value!!.documents) {
                val productsRef = machine.reference.collection("Products").whereLessThan("Quantity", 5)
                productsRef.addSnapshotListener { childValue, _ ->
                    for (product in childValue!!.documents) {
                        val machineName = machine.get("Name").toString()
                        val machineLocation = machine.get("Location").toString()
                        val productName = product.get("Name").toString()
                        val productQuantity = product.get("Quantity").toString()
                        val alertMessage = "The stock of item \"$productName\" in Vendo No. $machineName located at $machineLocation is running low. The current stock level is $productQuantity. Please take immediate action to restock this item to ensure availability for customers."
                        if (UserManager.role == "admin") {
                            notificationId++
                            showRestockNotification(alertMessage)
                        }
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

    private fun showRestockNotification(message: String) {
        val expandedNotificationStyle = NotificationCompat.BigTextStyle()
            .bigText(message)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_hygeia)
            .setContentTitle("Inventory Alert")
            .setContentText(message)
            .setStyle(expandedNotificationStyle)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(this@ActMain)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
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