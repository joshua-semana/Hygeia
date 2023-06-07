package com.hygeia

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hygeia.objects.UserManager
import androidx.activity.OnBackPressedCallback
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.classes.ButtonType
import com.hygeia.databinding.ActRequestMoneyBinding
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.isInternetConnected
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class ActRequestMoney : AppCompatActivity() {
    private lateinit var bind: ActRequestMoneyBinding
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")
    private var transactionRef = db.collection("Transactions")

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val usedNumbers = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActRequestMoneyBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActRequestMoney)
        setContentView(bind.root)

        with(bind) {
            btnContinue.setOnClickListener {
                clearTextError(
                    txtLayoutAmount, txtLayoutPassword, txtLayoutConfirmPassword
                )
                if (isInternetConnected(this@ActRequestMoney)) {
                    if (!inputsAreEmpty()) {
                        if (txtPassword.text.toString() == UserManager.password) {
                            if (txtConfirmPassword.text.toString() == UserManager.password) {
                                Utilities.dlgConfirmation(this@ActRequestMoney, "request money") {
                                    if (it == ButtonType.PRIMARY) {
                                        saveTransaction()
                                    }
                                }.show()
                            } else {
                                txtLayoutConfirmPassword.error =
                                    getString(R.string.error_password_matched)
                            }
                        } else {
                            txtLayoutPassword.error = getString(R.string.error_password_incorrect)
                        }
                    } else {
                        Utilities.showRequiredTextField(
                            txtAmount to txtLayoutAmount,
                            txtPassword to txtLayoutPassword,
                            txtConfirmPassword to txtLayoutConfirmPassword
                        )
                    }
                } else {
                    Utilities.dlgStatus(this@ActRequestMoney, "no internet").show()
                }
            }
        }

        //NAVIGATION
        bind.btnBack.setOnClickListener {
            onBackBtnPressed()
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackBtnPressed()
            }
        })
    }

    private fun saveTransaction() {
        loading.show()
        val transaction = hashMapOf(
            "Amount" to bind.txtAmount.text.toString().toDouble(),
            "Date Created" to Timestamp(Date()),
            "Number" to null,
            "Reference Number" to getReceiptId(),
            "Type" to "Request",
            "User Reference" to UserManager.uid,
        )

        transactionRef.document().set(transaction).addOnSuccessListener {
            updateBalance()
        }
    }

    private fun updateBalance() {
        val currentBalance = UserManager.balance.toString().toDouble()
        val newAddition = bind.txtAmount.text.toString().toDouble()
        userRef.document(UserManager.uid!!).update(
            "balance", currentBalance + newAddition
        ).addOnSuccessListener {
            loading.dismiss()
            Utilities.dlgStatus(this@ActRequestMoney, "success request money").apply {
                setOnDismissListener {
                    finish()
                }
                show()
            }
        }
    }

    private fun getReceiptId(): String {
        val currentDate = dateFormat.format(Date())
        val randomFourDigits = getRandomFourDigits()

        return "$currentDate${String.format("%04d", randomFourDigits)}"
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

    private fun onBackBtnPressed() {
        if (inputsAreEmpty()) {
            this.finish()
        } else {
            Utilities.dlgConfirmation(this, "going back") {
                if (it == ButtonType.PRIMARY) {
                    this.finish()
                }
            }.show()
        }
    }

    private fun inputsAreEmpty(): Boolean {
        return when {
            bind.txtAmount.text!!.isNotEmpty() -> false
            bind.txtPassword.text!!.isNotEmpty() -> false
            bind.txtConfirmPassword.text!!.isNotEmpty() -> false
            else -> true
        }
    }
}