package com.hygeia.objects

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.*
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.Date
import com.hygeia.R
import com.hygeia.classes.ButtonType
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

object Utilities {
    val emailPattern = "(?i)^[A-Z\\d._%+-]+@[A-Z\\d.-]+\\.[A-Z]{2,}\$".toRegex()
    val phoneNumberPattern = "^\\+639\\d{9}\$|^09\\d{9}\$".toRegex()
    val passwordPattern = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\\d)(?=.*?[#?!@\$%^&*-]).{8,}\$".toRegex()

    val transactionRef = FirebaseFirestore.getInstance().collection("Transactions")

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

        if (content == "success send money") {
            lblDlgInfoEmoji.text = Emoji.Success
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_positive_2)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_send_money)
            btnDlgInfoPrimary.text = context.getString(R.string.btn_great)
        }

        if (content == "success purchase") {
            lblDlgInfoEmoji.text = Emoji.Purchase
            lblDlgInfoTitle.text = context.getString(R.string.dlg_title_purchase_complete)
            lblDlgInfoBody.text = context.getString(R.string.dlg_body_purchase)
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

    @SuppressLint("SetTextI18n")
    fun dlgTransactionDetails(context: Context, transactionID: String): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dlg_transaction_details)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lblDlgTransactionEmoji = dialog.findViewById<TextView>(R.id.lblDlgTransactionEmoji)
        //val lblDlgTransactionTitle = dialog.findViewById<TextView>(R.id.lblDlgTransactionTitle)
        val lblDlgTransactionBody = dialog.findViewById<TextView>(R.id.lblDlgTransactionBody)
        val lblDlgTransactionTotalAmount = dialog.findViewById<TextView>(R.id.lblDlgTransactionTotalAmount)
        val lblDescTransactionIdentifier = dialog.findViewById<TextView>(R.id.lblDescTransactionIdentifier)
        val lblDescTransactionDate = dialog.findViewById<TextView>(R.id.lblDescTransactionDate)
        val lblDescTransactionTime = dialog.findViewById<TextView>(R.id.lblDescTransactionTime)
        val lblDescTransactionReference = dialog.findViewById<TextView>(R.id.lblDescTransactionReference)
        val lblDescTransactionItemsTitle = dialog.findViewById<TextView>(R.id.lblDescTransactionItemsTitle)
        val lblDescTransactionItemsContent = dialog.findViewById<TextView>(R.id.lblDescTransactionItemsContent)
        val divider2 = dialog.findViewById<View>(R.id.divider2)
        val btnDlgTransactionPrimary = dialog.findViewById<Button>(R.id.btnDlgTransactionPrimary)

        lblDlgTransactionEmoji.text = Emoji.Receipt

        transactionRef.document(transactionID.trim()).get().addOnSuccessListener { data ->

            val timestamp: Timestamp = data.get("Date Created") as Timestamp
            val date: java.util.Date = timestamp.toDate()
            val formatDate = SimpleDateFormat("MMMM d, y", Locale.getDefault())
            val dateString = formatDate.format(date)
            val formatTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeString = formatTime.format(date)

            lblDlgTransactionBody.text = "Here are all the details for this transaction."
            lblDescTransactionDate.text = "Transaction Date: $dateString"
            lblDescTransactionTime.text = "Transaction Time: $timeString"
            lblDescTransactionReference.text = "Reference No.: ${data.get("Reference Number")}"

            if (data.getString("Type") == "Send Money") {
                lblDlgTransactionTotalAmount.text = "- ${formatNumber(data.get("Amount"))}"
                lblDescTransactionIdentifier.text = "Receiver No.: ${data.get("Number")}"
            } else if (data.getString("Type") == "Receive Money") {
                lblDlgTransactionTotalAmount.text = "+ ${formatNumber(data.get("Amount"))}"
                lblDescTransactionIdentifier.text = "Sender No.: ${data.get("Number")}"
            } else if (data.getString("Type") == "Purchase") {
                divider2.visibility = View.VISIBLE
                lblDescTransactionItemsTitle.visibility = View.VISIBLE
                lblDescTransactionItemsContent.visibility = View.VISIBLE

                lblDlgTransactionTotalAmount.text = "- ${formatNumber(data.get("Amount"))}"
                lblDescTransactionIdentifier.text = "Vendo No.: ${data.get("Vendo")}"

                data.reference.collection("Items").get().addOnSuccessListener { dataItems ->
                    val items = mutableListOf<String>()

                    for (document in dataItems) {
                        val name = document.getString("Name")
                        val price = document.getString("Price")
                        val quantity = document.getLong("Quantity")
                        val total = price!!.toDouble() * quantity!!.toDouble()
                        items.add("${quantity}pc(s) $name - ${formatNumber(total)}")
                    }
                    val formattedText = items.joinToString("\n")
                    lblDescTransactionItemsContent.text = formattedText.dropLast(0)
                }
            }
        }

        btnDlgTransactionPrimary.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}
