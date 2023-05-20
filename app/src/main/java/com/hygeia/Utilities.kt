package com.hygeia

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

object Utilities {
    val emailPattern = "(?i)^[A-Z\\d._%+-]+@[A-Z\\d.-]+\\.[A-Z]{2,}\$".toRegex()
    val phoneNumberPattern = "^\\+639\\d{9}\$|^09\\d{9}\$".toRegex()
    val passwordPattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\\d)(?=.*?[#?!@\$%^&*-]).{8,}\$".toRegex()

    private const val emoSuccess = "🥳"
    private const val emoError = "🧐"
    private const val emoNoInternet = "😵"
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

    fun dlgLoading(context: Context): Dialog {
        val dialog = Dialog(context)
        with(dialog) {
            setCancelable(false)
            setContentView(R.layout.dlg_loading)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return dialog
    }

    fun dlgInformation(context: Context, content: String): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_message)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lblDlgInfoEmoji = dialog.findViewById<TextView>(R.id.lblDlgInfoEmoji)
        val lblDlgInfoTitle = dialog.findViewById<TextView>(R.id.lblDlgInfoTitle)
        val lblDlgInfoBody = dialog.findViewById<TextView>(R.id.lblDlgInfoBody)
        val btnDlgInfoPrimary = dialog.findViewById<Button>(R.id.btnDlgInfoPrimary)

        if (content == "success create account") {
            lblDlgInfoEmoji.text = emoSuccess
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_create_account)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_create_account)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_try_it_now)
        }

        if (content == "success update password") {
            lblDlgInfoEmoji.text = emoSuccess
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_update_password)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_update_password)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_great)
        }

        if (content == "no internet") {
            lblDlgInfoEmoji.text = emoNoInternet
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_no_internet)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_no_internet)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_okay)
        }

        if (content == "empty field") {
            lblDlgInfoEmoji.text = emoError
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_empty_field)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_empty_field)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_got_it)
        }

        btnDlgInfoPrimary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}
