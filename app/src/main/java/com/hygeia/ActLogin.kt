package com.hygeia

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
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
import com.hygeia.objects.OTPManager
import com.hygeia.objects.UserManager
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        validateInputs(txtEmail.text.toString(), txtPassword.text.toString())
                    } else {
                        clearTextErrors()
                        dlgStatus(this@ActLogin, "empty field").show()
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
                clearTextFields(txtEmail,txtPassword)
            }
            btnCreateAccount.setOnClickListener {
                startActivity(Intent(this@ActLogin, ActCreateAccount::class.java))
                clearTextErrors()
            }
        }
    }

    private fun inputsAreNotEmpty(): Boolean {
        return when {
            bind.txtEmail.text!!.isEmpty() -> false
            bind.txtPassword.text!!.isEmpty() -> false
            else -> true
        }
    }

    private fun clearTextErrors() {
        bind.txtLayoutEmail.isErrorEnabled = false
        bind.txtLayoutPassword.isErrorEnabled = false
    }

    private fun validateInputs(email: String, password: String) {
        clearTextErrors()
        loading.show()
        with(bind) {
            if (email.matches(emailPattern)) {
                lifecycleScope.launch(Dispatchers.Main) {
                    getEmailAddress(email)
                    if (emailAddress.isNotEmpty()) {
                        attemptLogin(email, password)
                    } else {
                        loading.dismiss()
                        txtLayoutEmail.error = getString(R.string.error_email_registered)
                    }
                }
            } else {
                loading.dismiss()
                txtLayoutEmail.error = getString(R.string.error_email_format)
            }
        }
    }

    private suspend fun getEmailAddress(email: String) {
        val query = userRef.whereEqualTo("email", email).get().await()
        emailAddress = if (query.isEmpty) "" else email
    }

    private fun attemptLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).apply {
            addOnSuccessListener {
                userRef.document(it.user!!.uid).get().addOnSuccessListener { data ->
                    loading.dismiss()
                    UserManager.setUserInformation(data)
                    clearTextFields(bind.txtEmail,bind.txtPassword)
                    startActivity(
                        when (UserManager.role) {
                            "standard" -> Intent(applicationContext, ActMain::class.java)
//                          "admin" -> Intent(this@ActLogin, ActUserAdmin::class.java)
                            else -> null
                        }
                    )
                }
            }
            addOnFailureListener {
                loading.dismiss()
                bind.txtLayoutPassword.error = getString(R.string.error_password_incorrect)
            }
        }
    }
}