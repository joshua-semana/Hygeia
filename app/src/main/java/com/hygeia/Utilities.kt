package com.hygeia

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.net.*
import android.view.LayoutInflater
import android.widget.Toast
import com.hygeia.databinding.DlgMessageBinding

object Utilities {

    val emailPattern = "[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+".toRegex()
    val phoneNumberPattern = "^(09)\\d{9}$".toRegex()
    val passwordPattern = "^(?=.[A-Z])(?=.[a-z])(?=.\\d)(?=.[@$!%?&])[A-Za-z\\d@$!%?&]{8,}$".toRegex()

    fun Context.msg(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun msgDlg(context: Context, dialogIcon: String, dialogTitle: String, dialogContent: String) {
        val bindDlg = DlgMessageBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context).apply{
            setCancelable(false)
            setView(bindDlg.root)
        }
        val shape = GradientDrawable().apply { cornerRadius = 30f }
        val dialog = builder.create()
        with(dialog) {
            window?.setBackgroundDrawable(shape)
            with(bindDlg) {
                if (dialogIcon == "no-wifi") imgDialogLogo.setImageResource(R.drawable.ic_wifi_not_connected)
                else if (dialogIcon == "success") imgDialogLogo.setImageResource(R.drawable.ic_check)
                lblDialogTitle.text = dialogTitle
                lblDialogBody.text = dialogContent
                btnDialogOkay.setOnClickListener { dismiss() }
            }
            show()
        }
    }
}