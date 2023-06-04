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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.classes.ButtonType
import com.hygeia.databinding.ActSendMoneyBinding
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.formatNumber
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.objects.Utilities.phoneNumberPattern
import com.hygeia.objects.Utilities.showRequiredTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

import java.text.SimpleDateFormat
import java.util.*

class ActSendMoney : AppCompatActivity() {
    private lateinit var bind: ActSendMoneyBinding
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")
    private var transactionRef = db.collection("Transactions")

    private val balance = formatNumber(UserManager.balance)
    private var phoneNumber = ""

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val usedNumbers = mutableSetOf<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActSendMoneyBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActSendMoney)
        setContentView(bind.root)

        with(bind) {
            textWatcher(txtAmount)
            bind.txtLayoutAmount.helperText = "You have $balance in your wallet."
            btnContinue.setOnClickListener {
                clearTextError(txtLayoutPhoneNumber, txtLayoutAmount)
                if (isInternetConnected(this@ActSendMoney)) {
                    if (inputsAreNotEmpty()) {
                        loading.show()
                        phoneNumber =
                            (bind.txtLayoutPhoneNumber.prefixText.toString() + bind.txtPhoneNumber.text.toString()).trim()
                        lifecycleScope.launch(Dispatchers.Main) {
                            val amountText = txtAmount.text.toString()
                            val amount = amountText.toDoubleOrNull()
                            if (amount != null) {
                                if (inputsAreCorrect(amount)) {
                                    dlgConfirmation(this@ActSendMoney, "send money") {
                                        if (it == ButtonType.PRIMARY) {
                                            lifecycleScope.launch(Dispatchers.Main) {
                                                sendMoney(amount)
                                            }
                                        } else {
                                            loading.dismiss()
                                        }
                                    }.show()
                                }
                            }
                        }
                    } else {
                        loading.dismiss()
                        showRequiredTextField(
                            txtPhoneNumber to txtLayoutPhoneNumber,
                            txtAmount to txtLayoutAmount
                        )
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

    private suspend fun inputsAreCorrect(amount: Double): Boolean {
        var inputErrorCount = 0
        with(bind) {
            if (phoneNumber.matches(phoneNumberPattern)) {
                val phoneNumberExists = getPhoneNumberExistenceOf(phoneNumber)
                if (!phoneNumberExists) {
                    inputErrorCount++
                    txtLayoutPhoneNumber.error = getString(R.string.error_phone_registered)
                }
                if (phoneNumber == UserManager.phoneNumber) {
                    inputErrorCount++
                    txtLayoutPhoneNumber.error = getString(R.string.error_phone_user_manager)
                }
            } else {
                inputErrorCount++
                txtLayoutPhoneNumber.error = getString(R.string.error_phone_format)
            }
            if (UserManager.balance.toString().toDouble() < amount) {
                inputErrorCount++
                bind.txtLayoutAmount.error = "You only have $balance in your wallet."
            }
        }
        return inputErrorCount == 0
    }

    private suspend fun getPhoneNumberExistenceOf(phoneNumber: String): Boolean {
        val query = userRef.whereEqualTo("phoneNumber", phoneNumber).get().await()
        return !query.isEmpty
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
                    userRef.document(getReceiverId).update("balance", newReceiverBalance)
                    userRef.document(UserManager.uid!!).get().addOnSuccessListener { data ->
                        UserManager.updateUserBalance(data)
                    }
                    transaction()
                }
        }
    }

    private fun transaction() {
        val senderData = hashMapOf(
            "Amount" to bind.txtAmount.text.toString().toDouble(),
            "Date Created" to Timestamp(Date()),
            "Number" to phoneNumber,
            "Reference Number" to getReceiptId(),
            "Type" to "Send Money",
            "User Reference" to UserManager.uid,
        )

        transactionRef.document().set(senderData)
            .addOnSuccessListener {
                val receiverData = hashMapOf(
                    "Amount" to bind.txtAmount.text.toString().toDouble(),
                    "Date Created" to Timestamp(Date()),
                    "Number" to UserManager.phoneNumber,
                    "Reference Number" to senderData["Reference Number"],
                    "Type" to "Receive Money",
                    "User Reference" to "",
                )
                getReceiverId { receiverId ->
                    receiverData["User Reference"] = receiverId
                    transactionRef.document().set(receiverData)
                }

                loading.dismiss()
                dlgStatus(this@ActSendMoney, "success send money").apply {
                    setOnDismissListener {
                        finish()
                    }
                    show()
                }
            }
    }

    private fun getReceiverId(callback: (String) -> Unit) {
        userRef.whereEqualTo("phoneNumber", phoneNumber).get().addOnSuccessListener {
            val getReceiverId = it.documents[0].id
            callback(getReceiverId)
        }
    }

    private fun inputsAreNotEmpty(): Boolean {
        return when {
            bind.txtPhoneNumber.text!!.isEmpty() -> false
            bind.txtAmount.text!!.isEmpty() -> false
            else -> true
        }
    }

    private fun getRandomFourDigits(): Int {
        val random = Random()
        var randomNum = random.nextInt(10000)
        while (usedNumbers.contains(randomNum)) {
            randomNum = random.nextInt(10000)
        }
        usedNumbers.add(randomNum)
        return randomNum
    }

    private fun getReceiptId(): String {
        val currentDate = dateFormat.format(Date())
        val randomFourDigits = getRandomFourDigits()

        return "$currentDate${String.format("%04d", randomFourDigits)}"
    }

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
