package com.hygeia

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.dlgNoInternet
import com.hygeia.Utilities.dlgRequiredFields
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.databinding.ActLoginBinding

class ActLogin : AppCompatActivity() {
    private lateinit var bind: ActLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loading: Dialog
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        loading = dlgLoading(this@ActLogin)
        setContentView(bind.root)

        with(bind) {
            //MAIN FUNCTIONS
            btnLogin.setOnClickListener {
                if (txtEmail.text!!.isEmpty() or txtPassword.text!!.isEmpty()) {
                    dlgRequiredFields(this@ActLogin).show()
                    clearTextErrors()
                } else {
                    if (isInternetConnected(applicationContext)) {
                        validateInputs(txtEmail.text.toString(), txtPassword.text.toString())
                    } else {
                        dlgNoInternet(this@ActLogin).show()
                    }
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
            }

            btnCreateAccount.setOnClickListener {
                startActivity(Intent(this@ActLogin, ActCreateAccount::class.java))
            }
        }
    }

    private fun clearTextErrors() {
        with(bind) {
            txtLayoutEmail.isErrorEnabled = false
            txtLayoutPassword.isErrorEnabled = false
        }
    }

    private fun validateInputs(email: String, password: String) {
        clearTextErrors()
        loading.show()
        with(bind) {
            if (emailPattern.matches(email)) {
                auth.fetchSignInMethodsForEmail(email).addOnSuccessListener { getEmailList ->
                    if (getEmailList.signInMethods?.isNotEmpty() == true) {
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

    private fun attemptLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).apply {
            addOnSuccessListener {
                db.collection("User").document(auth.currentUser!!.uid).get()
                    .addOnSuccessListener { userInfo ->
                        loading.dismiss()
                        startActivity(
                            when (userInfo.get("role")) {
                                "standard" -> Intent(applicationContext, ActMain::class.java)
//                                "admin" -> Intent(this@ActLogin, ActUserAdmin::class.java)
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