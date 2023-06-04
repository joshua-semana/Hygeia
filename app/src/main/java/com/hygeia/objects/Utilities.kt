package com.hygeia.objects

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.*
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hygeia.R
import com.hygeia.classes.ButtonType
import java.text.DecimalFormat

object Utilities {
    val emailPattern = "(?i)^[A-Z\\d._%+-]+@[A-Z\\d.-]+\\.[A-Z]{2,}\$".toRegex()
    val phoneNumberPattern = "^\\+639\\d{9}\$|^09\\d{9}\$".toRegex()
    val passwordPattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\\d)(?=.*?[#?!@\$%^&*-]).{8,}\$".toRegex()

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
    fun formatNumber(balance: Any?): String {
        return when (balance) {
            0 -> "₱0.00"
            else -> DecimalFormat("₱#,##0.00").format(balance)
        }
    }

    fun clearTextFields(vararg textFields: EditText) {
        for (textField in textFields){
            textField.text.clear()
        }
    }
    fun clearTextError(vararg textInputLayouts: TextInputLayout) {
        for (textInputLayout in textInputLayouts){
            textInputLayout.isErrorEnabled = false
        }
    }
    fun showRequiredTextField(vararg inputs: Pair<TextInputEditText, TextInputLayout>) {
        for (input in inputs) {
            if (input.first.text!!.isEmpty()) {
                input.second.error = "Required*"
            }
        }
    }
    fun showRequiredComboBox(vararg inputs: Pair<AutoCompleteTextView, TextInputLayout>) {
        for (input in inputs) {
            if (input.first.text!!.isEmpty()) {
                input.second.error = "Required*"
            }
        }
    }
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

    fun dlgTermsAndConditions(context: Context): Dialog {
        val dialog = Dialog(context)
        with(dialog) {
            setCancelable(false)
            setContentView(R.layout.dlg_terms_and_conditions)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val btnDlgTermsPrimary = dialog.findViewById<Button>(R.id.btnDlgTermsPrimary)

        btnDlgTermsPrimary.setOnClickListener {
            dialog.dismiss()
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
            lblDlgInfoEmoji.text = Emoji.Success
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_positive_1)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_create_account)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_try_it_now)
        }

        if (content == "success update password") {
            lblDlgInfoEmoji.text = Emoji.Success
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_positive_2)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_update_password)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_great)
        }

        if (content == "no internet") {
            lblDlgInfoEmoji.text = Emoji.NoInternet
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_1)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_no_internet)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_okay)
        }

        if (content == "empty field") {
            lblDlgInfoEmoji.text = Emoji.Error
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_3)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_empty_field)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_got_it)
        }

        if (content == "machine offline or in use") {
            lblDlgInfoEmoji.text = Emoji.Error
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_1)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_machine_offline_or_in_use)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_okay)
        }

        if (content == "QR code is not registered") {
            lblDlgInfoEmoji.text = Emoji.Exception
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_1)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_qr_code_not_registered)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_okay)
        }
        if (content == "insufficient funds") {
            lblDlgInfoEmoji.text = Emoji.Error
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_1)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_insufficient_funds)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_okay)
        }

        if (content == "empty cart") {
            lblDlgInfoEmoji.text = Emoji.Error
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_3)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_empty_cart)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_okay)
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
            lblDlgInfoEmoji.text = Emoji.Trivia
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

        lblDlgInfoEmoji.text = Emoji.Exception
        lblDlgInfoTitle.text = context.getString(R.string.dlg_title_negative_2)
        lblDlgInfoBody.text = errorMessage
        btnDlgInfoPrimary.text = context.getString(R.string.btn_okay)

        btnDlgInfoPrimary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    @SuppressLint("SetTextI18n")
    fun dlgConfirmation(context: Context, content: String, onButtonClicked: (type: ButtonType) -> Unit): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_confirmation)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lblDlgConfirmEmoji = dialog.findViewById<TextView>(R.id.lblDlgConfirmEmoji)
        val lblDlgConfirmTitle = dialog.findViewById<TextView>(R.id.lblDlgConfirmTitle)
        val lblDlgConfirmBody = dialog.findViewById<TextView>(R.id.lblDlgConfirmBody)
        val btnDlgConfirmPrimary = dialog.findViewById<Button>(R.id.btnDlgConfirmPrimary)
        val btnDlgConfirmSecondary = dialog.findViewById<Button>(R.id.btnDlgConfirmSecondary)

        lblDlgConfirmEmoji.text = Emoji.Confirmation
        lblDlgConfirmTitle.text = context.getString(R.string.dlg_title_confirmation)
        btnDlgConfirmSecondary.text = context.getString(R.string.btn_no)

        if (content == "going back") {
            lblDlgConfirmBody.text = context.getString(R.string.dlg_body_going_back)
            btnDlgConfirmPrimary.text = "${btnDlgConfirmPrimary.text}, go back"
            btnDlgConfirmPrimary.setBackgroundColor(context.getColor(R.color.accent_500))
        }

        if (content == "send money") {
            lblDlgConfirmBody.text = context.getString(R.string.dlg_send_money)
            btnDlgConfirmPrimary.text = "${btnDlgConfirmPrimary.text}"
            btnDlgConfirmPrimary.setBackgroundColor(context.getColor(R.color.accent_500))
        }

        if (content == "purchase") {
            lblDlgConfirmBody.text = context.getString(R.string.dlg_purchase)
            btnDlgConfirmPrimary.text = "${btnDlgConfirmPrimary.text}"
            btnDlgConfirmPrimary.setBackgroundColor(context.getColor(R.color.accent_500))
        }

        if (content == "log out") {
            lblDlgConfirmBody.text = context.getString(R.string.dlg_body_log_out)
            btnDlgConfirmPrimary.text = "${btnDlgConfirmPrimary.text}, log out"
            btnDlgConfirmPrimary.setBackgroundColor(context.getColor(R.color.accent_500))
        }

        btnDlgConfirmPrimary.setOnClickListener {
            onButtonClicked(ButtonType.PRIMARY)
            dialog.dismiss()
        }

        btnDlgConfirmSecondary.setOnClickListener {
            onButtonClicked(ButtonType.SECONDARY)
            dialog.dismiss()
        }

        return dialog
    }
}
