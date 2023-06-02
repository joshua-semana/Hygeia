package com.hygeia

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.objects.Utilities.clearTextFields
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.dlgLoading
import com.hygeia.objects.Utilities.emailPattern
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.databinding.ActLoginBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities.phoneNumberPattern
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.showRequiredTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ActLogin : AppCompatActivity() {
    private lateinit var bind: ActLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")

    private var emailAddress = ""
    private var phoneNumber = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        bind = ActLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        loading = dlgLoading(this@ActLogin)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(bind.root)

        with(bind) {
            //MAIN FUNCTIONS
            btnLogin.setOnClickListener {
                if (isInternetConnected(applicationContext)) {
                    if (inputsAreNotEmpty()) {
                        validateInputs(txtEmailOrPhoneNumber.text?.trim().toString(), txtPassword.text.toString())
                    } else {
                        clearTextError(txtLayoutEmail, txtLayoutPassword)
                        dlgStatus(this@ActLogin, "empty field").show()
                        showRequiredTextField(
                            txtEmailOrPhoneNumber to txtLayoutEmailOrPhoneNumber,
                            txtPassword to txtLayoutPassword
                        )
                    }
                } else {
                    dlgStatus(this@ActLogin, "no internet").show()
                }
            }

            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                }
                mainLayout.requestFocus()
                currentFocus?.clearFocus()
            }

            //NAVIGATION
            btnForgotPassword.setOnClickListener {
                startActivity(Intent(this@ActLogin, ActForgotPassword::class.java))
                clearTextErrors()
                clearTextFields(txtEmailOrPhoneNumber, txtPassword)
            }
            btnCreateAccount.setOnClickListener {
                startActivity(Intent(this@ActLogin, ActCreateAccount::class.java))
                clearTextError(txtLayoutEmail, txtLayoutPassword)
                clearTextFields(txtEmail,txtPassword)
            }
        }
    }
    private fun inputsAreNotEmpty(): Boolean {
        return when {
            bind.txtEmailOrPhoneNumber.text!!.isEmpty() -> false
            bind.txtPassword.text!!.isEmpty() -> false
            else -> true
        }
    }

    private fun clearTextErrors() {
        bind.txtLayoutEmailOrPhoneNumber.isErrorEnabled = false
        bind.txtLayoutPassword.isErrorEnabled = false
    }
    private fun validateInputs(email: String, password: String) {
        clearTextError(bind.txtLayoutEmail, bind.txtLayoutPassword)
        loading.show()
        with(bind) {
            lifecycleScope.launch(Dispatchers.Main) {
                if (input.matches(emailPattern)) {
                    getEmailAddress(input)
                    if (emailAddress.isNotEmpty()) {
                        attemptEmailLogin(emailAddress, password)
                    } else {
                        loading.dismiss()
                        txtLayoutEmailOrPhoneNumber.error = getString(R.string.error_email_registered)
                    }
                }
                else if (input.matches(phoneNumberPattern)) {
                    getPhoneNumber(input)
                    if (phoneNumber.isNotEmpty()) {
                        attemptPhoneNumberLogin(phoneNumber, password)
                    } else {
                        loading.dismiss()
                        txtLayoutEmailOrPhoneNumber.error = getString(R.string.error_phone_registered)
                    }
                }
                else {
                    loading.dismiss()
                    txtLayoutEmailOrPhoneNumber.error = getString(R.string.error_email_password_format)
                }
            }
        }
    }

    private suspend fun getEmailAddress(email: String) {
        val query = userRef.whereEqualTo("email", email).get().await()
        emailAddress = if (query.isEmpty) "" else email
    }

    private suspend fun getPhoneNumber(number: String) {
        var sanitizedNumber = number
        if (number.startsWith("0")) {
            sanitizedNumber = "+63" + number.substring(1)
        }
        val query = userRef.whereEqualTo("phoneNumber", sanitizedNumber).get().await()
        phoneNumber = if (query.isEmpty) "" else sanitizedNumber
    }

    private fun attemptEmailLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).apply {
            addOnSuccessListener {
                userRef.document(it.user!!.uid).get().addOnSuccessListener { data ->
                    loading.dismiss()
                    UserManager.setUserInformation(data)
                    clearTextFields(bind.txtEmailOrPhoneNumber, bind.txtPassword)
                    startActivity(Intent(applicationContext, ActMain::class.java))
                }
            }
            addOnFailureListener {
                loading.dismiss()
                bind.txtLayoutPassword.error = getString(R.string.error_password_incorrect)
            }
        }
    }
    private suspend fun attemptPhoneNumberLogin(number: String, password: String) {
        val query = userRef.whereEqualTo("phoneNumber", number).get().await()
        if (query.documents[0].getString("password") == password) {
            userRef.document(query.documents[0].id).get().addOnSuccessListener { data ->
                loading.dismiss()
                UserManager.setUserInformation(data)
                clearTextFields(bind.txtEmailOrPhoneNumber, bind.txtPassword)
                startActivity(Intent(applicationContext, ActMain::class.java))
            }
        } else {
            loading.dismiss()
            bind.txtLayoutPassword.error = getString(R.string.error_password_incorrect)
        }
    }
}