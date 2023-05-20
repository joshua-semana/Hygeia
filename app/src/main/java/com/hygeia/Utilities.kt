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
    private const val emoException = "😱"
    private const val emoTrivia = "🤯"

    val greetings = hashMapOf(
        "Filipino" to "Mabuhay!",
        "English" to "Hello!",
        "Spanish" to "Hola!",
        "French" to "Bonjour!",
        "Italian" to "Salve!",
        "Mandarin" to "Nín Hǎo!",
        "Arabic" to "Asalaam Alaikum!",
        "Japanese" to "Konnichiwa!",
        "Korean" to "Annyeong!",
        "Hindi" to "Namaste!",
        "Vietnamese" to "Xin Chào!",
    )

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

    fun dlgStatus(context: Context, content: String): Dialog {
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
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_positive_1)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_create_account)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_try_it_now)
        }

        if (content == "success update password") {
            lblDlgInfoEmoji.text = emoSuccess
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_positive_2)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_update_password)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_great)
        }

        if (content == "no internet") {
            lblDlgInfoEmoji.text = emoNoInternet
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_1)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_no_internet)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_okay)
        }

        if (content == "empty field") {
            lblDlgInfoEmoji.text = emoError
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_3)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_empty_field)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_got_it)
        }

        if (content == "going back") {
            lblDlgInfoEmoji.text = emoException
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_2)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_going_back)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_sure)
        }

        btnDlgInfoPrimary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    fun dlgInformation(context: Context, info1: String): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_message)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lblDlgInfoEmoji = dialog.findViewById<TextView>(R.id.lblDlgInfoEmoji)
        val lblDlgInfoTitle = dialog.findViewById<TextView>(R.id.lblDlgInfoTitle)
        val lblDlgInfoBody = dialog.findViewById<TextView>(R.id.lblDlgInfoBody)
        val btnDlgInfoPrimary = dialog.findViewById<Button>(R.id.btnDlgInfoPrimary)

        if (greetings.containsKey(info1)) {
            val message = "The word '${greetings[info1]}' is the $info1 term for the word 'Hello!'"
            lblDlgInfoEmoji.text = emoTrivia
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_trivia)
            lblDlgInfoBody.text = message
            btnDlgInfoPrimary.text = context.getString(R.string.btn_got_it)
        }

        btnDlgInfoPrimary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    fun dlgError(context: Context, message: String): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_message)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val errorMessage = "Error exception message: $message"
        val lblDlgInfoEmoji = dialog.findViewById<TextView>(R.id.lblDlgInfoEmoji)
        val lblDlgInfoTitle = dialog.findViewById<TextView>(R.id.lblDlgInfoTitle)
        val lblDlgInfoBody = dialog.findViewById<TextView>(R.id.lblDlgInfoBody)
        val btnDlgInfoPrimary = dialog.findViewById<Button>(R.id.btnDlgInfoPrimary)

        lblDlgInfoEmoji.text = emoException
        lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_2)
        lblDlgInfoBody.text = errorMessage
        btnDlgInfoPrimary.text = context.getString(R.string.btn_okay)

        btnDlgInfoPrimary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}
