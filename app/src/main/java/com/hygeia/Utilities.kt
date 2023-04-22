package com.hygeia

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.*
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.hygeia.databinding.DlgMessageBinding

object Utilities {

    val emailPattern = "(?i)^[A-Z\\d._%+-]+@[A-Z\\d.-]+\\.[A-Z]{2,}\$".toRegex()
    val phoneNumberPattern = "^\\+639\\d{9}\$|^09\\d{9}\$".toRegex()
    val passwordPattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\\d)(?=.*?[#?!@\$%^&*-]).{8,}\$".toRegex()
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

    fun dlgMessage(context: Context, dialogIcon: String, dialogTitle: String, dialogContent: String, dialogOkay: String): Dialog {
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
                //DISPLAY
                if (dialogIcon == "no-wifi") imgDialogLogo.setImageResource(R.drawable.ic_wifi_off)
                else if (dialogIcon == "success") imgDialogLogo.setImageResource(R.drawable.ic_success)
                lblDialogTitle.text = dialogTitle
                lblDialogBody.text = dialogContent
                btnDialogPrimary.text = dialogOkay

                //MAIN FUNCTIONS
                btnDialogPrimary.setOnClickListener {
                    if (dialog != null && dialog.isShowing) {
                        dismiss()
                    }
                }
            }
        }
        return dialog
    }

    fun dlgNoInternet(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_message)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val imgDialogLogo = dialog.findViewById<ImageView>(R.id.imgDialogLogo)
        val lblDialogTitle = dialog.findViewById<TextView>(R.id.lblDialogTitle)
        val lblDialogBody = dialog.findViewById<TextView>(R.id.lblDialogBody)
        val btnDialogPrimary = dialog.findViewById<Button>(R.id.btnDialogPrimary)

        imgDialogLogo.setImageResource(R.drawable.ic_wifi_off)
        imgDialogLogo.setBackgroundResource(R.drawable.bg_circle_50)
        lblDialogTitle.text = context.getString(R.string.dlg_title_wifi)
        lblDialogBody.text = context.getString(R.string.dlg_body_wifi)
        btnDialogPrimary.text = context.getString(R.string.btn_okay)

        btnDialogPrimary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    fun dlgRequiredFields(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_message)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val imgDialogLogo = dialog.findViewById<ImageView>(R.id.imgDialogLogo)
        val lblDialogTitle = dialog.findViewById<TextView>(R.id.lblDialogTitle)
        val lblDialogBody = dialog.findViewById<TextView>(R.id.lblDialogBody)
        val btnDialogPrimary = dialog.findViewById<Button>(R.id.btnDialogPrimary)

        imgDialogLogo.setImageResource(R.drawable.ic_warning)
        imgDialogLogo.setBackgroundResource(R.drawable.bg_circle_danger_50)
        lblDialogTitle.text = context.getString(R.string.dlg_title_required_fields)
        lblDialogBody.text = context.getString(R.string.dlg_body_required_fields)
        btnDialogPrimary.text = context.getString(R.string.btn_okay)

        btnDialogPrimary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}