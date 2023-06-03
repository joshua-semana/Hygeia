package com.hygeia

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.hygeia.objects.UserManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.databinding.ActSendMoneyBinding
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.formatNumber
import com.hygeia.objects.Utilities.isInternetConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.DecimalFormat

class ActSendMoney : AppCompatActivity() {
    private lateinit var bind: ActSendMoneyBinding
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")

    private val balance = formatNumber(UserManager.balance)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActSendMoneyBinding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            textWatcher(txtAmount)
            bind.txtLayoutAmount.helperText = "You have $balance in your wallet."
            btnContinue.setOnClickListener {
                if (isInternetConnected(this@ActSendMoney)) {

                    lifecycleScope.launch(Dispatchers.Main) {
                        val amountText = txtAmount.text.toString()
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null) {
                            sendMoney(amount)
                        }
                    }
                } else {
                    dlgStatus(this@ActSendMoney, "no internet").show()
                }
            }
            mainLayout.setOnClickListener {
                getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                }
                mainLayout.requestFocus()
                currentFocus?.clearFocus()
            }
            //NAVIGATION
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private suspend fun sendMoney(amount: Double) {
        val senderQuery = userRef.whereEqualTo("phoneNumber", UserManager.phoneNumber).get().await()
        val getSenderBalance = senderQuery.documents[0].get("balance").toString().toDouble()
        val getSenderId = senderQuery.documents[0].id
        val phoneNumber =
            (bind.txtLayoutPhoneNumber.prefixText.toString() + bind.txtPhoneNumber.text.toString()).trim()
        val receiverQuery = userRef.whereEqualTo("phoneNumber", phoneNumber).get().await()
        val getReceiverBalance = receiverQuery.documents[0].get("balance").toString().toDouble()
        val getReceiverId = receiverQuery.documents[0].id

        if (getSenderBalance >= amount) {
            val newSenderBalance = getSenderBalance - amount
            val newReceiverBalance = getReceiverBalance + amount

            userRef.document(getSenderId).update("balance", newSenderBalance)
                .addOnSuccessListener {
                    userRef.document(UserManager.uid!!).get().addOnSuccessListener { data ->
                        UserManager.updateUserBalance(data)
                    }
                }
            userRef.document(getReceiverId).update("balance", newReceiverBalance)

        }
    }

    //    private fun inputsAreNotEmpty() : Boolean{
//        return when{
//            bind.txtPhoneNumber.text!!.isEmpty() -> false
//
//        }
//    }
    private fun textWatcher(textField: EditText) {
        textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                val validInput = UserManager.balance.toString().toDouble()
                val userInput = input.toDoubleOrNull() ?: 0.0
                if (userInput > validInput) {
                    bind.txtLayoutAmount.error = "You only have $balance in your wallet."
                } else {
                    bind.txtLayoutAmount.helperText = "You have $balance in your wallet."
                    clearTextError(bind.txtLayoutAmount)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
