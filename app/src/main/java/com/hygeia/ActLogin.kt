package com.hygeia

import android.annotation.SuppressLint
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
import com.hygeia.objects.Utilities.dlgError
import com.hygeia.objects.Utilities.msg
import com.hygeia.objects.Utilities.showRequiredTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ActLogin : AppCompatActivity() {
    private lateinit var bind: ActLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")

    private var emailAddress = ""
    private var phoneNumber = ""

    @SuppressLint("SetTextI18n")
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
                        validateInputs(
                            txtEmailOrPhoneNumber.text?.trim().toString(),
                            txtPassword.text.toString()
                        )
                    } else {
                        clearTextError(txtLayoutEmailOrPhoneNumber, txtLayoutPassword)
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
                clearTextError(txtLayoutEmailOrPhoneNumber, txtLayoutPassword)
                clearTextFields(txtEmailOrPhoneNumber, txtPassword)
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

    private fun validateInputs(input: String, password: String) {
        clearTextError(bind.txtLayoutEmailOrPhoneNumber, bind.txtLayoutPassword)
        loading.show()
        with(bind) {
            lifecycleScope.launch(Dispatchers.Main) {
                if (input.matches(emailPattern)) {
                    getEmailAddress(input)
                    if (emailAddress.isNotEmpty()) {
                        attemptEmailLogin(emailAddress, password)
                    } else {
                        loading.dismiss()
                        txtLayoutEmailOrPhoneNumber.error =
                            getString(R.string.error_email_registered)
                    }
                } else if (input.matches(phoneNumberPattern)) {
                    getPhoneNumber(input)
                    if (phoneNumber.isNotEmpty()) {
                        attemptPhoneNumberLogin(phoneNumber, password)
                    } else {
                        loading.dismiss()
                        txtLayoutEmailOrPhoneNumber.error =
                            getString(R.string.error_phone_registered)
                    }
                } else {
                    loading.dismiss()
                    txtLayoutEmailOrPhoneNumber.error =
                        getString(R.string.error_email_password_format)
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
        isUserLockedOut(email) { isLockedOut ->
            if (isLockedOut) {
                loading.dismiss()
                this@ActLogin.msg("Account locked. Try again later.")
            } else {
                auth.signInWithEmailAndPassword(email, password).apply {
                    addOnSuccessListener { id ->
                        val uid = id.user!!.uid
                        userRef.document(uid).get().addOnSuccessListener { data ->

                            val failedAttemptsRef =
                                FirebaseFirestore.getInstance().collection("failedAttempts")
                            failedAttemptsRef.document(emailAddress).delete()

                            UserManager.setUserInformation(data)
                            clearTextFields(bind.txtEmailOrPhoneNumber, bind.txtPassword)
                            if (UserManager.isEnabled == true && UserManager.isOnline == false) {
                                userRef.document(uid).update("isOnline", true)
                                    .addOnSuccessListener {
                                        loading.dismiss()
                                        startActivity(
                                            Intent(
                                                applicationContext,
                                                ActMain::class.java
                                            )
                                        )
                                    }
                            } else if (UserManager.isEnabled == false) {
                                loading.dismiss()
                                dlgStatus(this@ActLogin, "user disabled").show()
                            } else if (UserManager.isOnline == true) {
                                loading.dismiss()
                                dlgStatus(this@ActLogin, "user already active").show()
                            }
                        }
                    }
                    addOnFailureListener {
                        loading.dismiss()
                        handleFailedLoginAttempt(email)
                        isUserLockedOut(email) { isLockedOut ->
                            if (isLockedOut) {
                                loading.dismiss()
                                this@ActLogin.msg("Account locked. Try again later.")
                            } else {
                                bind.txtLayoutPassword.error =
                                    getString(R.string.error_password_incorrect)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun attemptPhoneNumberLogin(number: String, password: String) {
        userRef.whereEqualTo("phoneNumber", number).get().addOnSuccessListener { data ->
            for (item in data.documents) {
                emailAddress = item.get("email").toString()
            }
            isUserLockedOut(emailAddress) { isLockedOut ->
                if (isLockedOut) {
                    loading.dismiss()
                    this@ActLogin.msg("Account locked. Try again later.")
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val querySnapshot =
                                userRef.whereEqualTo("phoneNumber", number).get().await()
                            if (!querySnapshot.isEmpty) {
                                val documentSnapshot = querySnapshot.documents[0]
                                if (documentSnapshot.getString("password") == password) {

                                    val failedAttemptsRef =
                                        FirebaseFirestore.getInstance().collection("failedAttempts")
                                    failedAttemptsRef.document(emailAddress).delete()

                                    val document =
                                        userRef.document(documentSnapshot.id).get().await()
                                    UserManager.setUserInformation(document)
                                    withContext(Dispatchers.Main) {
                                        clearTextFields(
                                            bind.txtEmailOrPhoneNumber,
                                            bind.txtPassword
                                        )
                                        if (UserManager.isEnabled == true && UserManager.isOnline == false) {
                                            userRef.document(documentSnapshot.id)
                                                .update("isOnline", true).await()
                                            startActivity(
                                                Intent(
                                                    applicationContext,
                                                    ActMain::class.java
                                                )
                                            )
                                            loading.dismiss()
                                        } else if (UserManager.isEnabled == false) {
                                            loading.dismiss()
                                            dlgStatus(this@ActLogin, "user disabled").show()
                                        } else if (UserManager.isOnline == true) {
                                            loading.dismiss()
                                            dlgStatus(this@ActLogin, "user already active").show()
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        loading.dismiss()
                                        handleFailedLoginAttempt(
                                            documentSnapshot.getString("email").toString()
                                        )
                                        isUserLockedOut(
                                            documentSnapshot.getString("email").toString()
                                        ) { isLockedOut ->
                                            if (isLockedOut) {
                                                loading.dismiss()
                                                this@ActLogin.msg("Account locked. Try again later.")
                                            } else {
                                                bind.txtLayoutPassword.error =
                                                    getString(R.string.error_password_incorrect)
                                            }
                                        }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    loading.dismiss()
                                    bind.txtLayoutEmailOrPhoneNumber.error =
                                        getString(R.string.error_phone_registered)
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                loading.dismiss()
                                dlgError(this@ActLogin, e.toString()).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleFailedLoginAttempt(identifier: String) {
        val failedAttemptsRef = FirebaseFirestore.getInstance().collection("failedAttempts")
        val userAttemptsRef = failedAttemptsRef.document(identifier)

        userAttemptsRef.get().addOnSuccessListener { snapshot ->
            val currentAttempts = snapshot.getLong("attempts") ?: 0
            val updatedAttempts = currentAttempts + 1

            userAttemptsRef.set(mapOf("attempts" to updatedAttempts))
                .addOnSuccessListener {
                    val maxAttempts = 5
                    if (updatedAttempts >= maxAttempts) {
                        val lockoutDurationMillis = 2 * 60 * 1000 // 2 minutes
                        val unlockTimeMillis = System.currentTimeMillis() + lockoutDurationMillis
                        userAttemptsRef.update("unlockTime", unlockTimeMillis)
                    }
                }
        }
    }

    private fun isUserLockedOut(email: String, callback: (Boolean) -> Unit) {
        val failedAttemptsRef = FirebaseFirestore.getInstance().collection("failedAttempts")
        val userAttemptsRef = failedAttemptsRef.document(email)

        userAttemptsRef.get().addOnSuccessListener { snapshot ->
            val unlockTime = snapshot.getLong("unlockTime")
            val isLockedOut = unlockTime != null && unlockTime > System.currentTimeMillis()
            callback(isLockedOut)
        }
    }
}