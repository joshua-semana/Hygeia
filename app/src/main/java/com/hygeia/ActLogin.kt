package com.hygeia

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.dlgMessage
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.databinding.ActivityLoginBinding

class ActLogin : AppCompatActivity() {
    private lateinit var bind : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var loading : Dialog
    private var cloudFirestore = Firebase.firestore
    //private var loginAttemptCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        loading = dlgLoading(this@ActLogin)
        setContentView(bind.root)

        with(bind) {
            //MAIN FUNCTIONS
            btnLogin.setOnClickListener {
                if (isInternetConnected(applicationContext)) {
                    validateInputs(txtEmail.text.toString(), txtPassword.text.toString())
                } else {
                    dlgMessage(
                        this@ActLogin,
                        "no-wifi",
                        getString(R.string.dlg_title_wifi),
                        getString(R.string.dlg_body_wifi),
                        "Okay"
                    ).show()
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

            tglShowPassword.setOnCheckedChangeListener { _, isChecked ->
                val passwordDisplay = if (isChecked) null else PasswordTransformationMethod()
                with(txtPassword) {
                    this.transformationMethod = passwordDisplay
                    setSelection(text.length)
                }
            }

            //INPUT VALIDATION
            textWatcher(txtEmail)
            textWatcher(txtPassword)

            //NAVIGATION
            btnGoToForgotPassword.setOnClickListener {
                startActivity(Intent(this@ActLogin, ActForgotPassword::class.java))
            }

            btnGoToCreateAccountPart1.setOnClickListener {
                startActivity(Intent(this@ActLogin, ActCreateAccount::class.java))
            }
        }
    }
    private fun validateInputs(email : String, password : String) {
        loading.show()
        with(bind) {
            txtEmail.setBackgroundResource(R.drawable.bg_textfield_default)
            txtPassword.setBackgroundResource(R.drawable.bg_textfield_default)
            lblLoginErrorMsg1.visibility = View.GONE
            lblLoginErrorMsg2.visibility = View.GONE

            if (emailPattern.matches(email)) {
                auth.fetchSignInMethodsForEmail(email).addOnSuccessListener { getEmailList ->
                    if (getEmailList.signInMethods?.isNotEmpty() == true) {
                        attemptLogin(email, password)
                    } else {
                        loading.dismiss()
                        txtEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                        lblLoginErrorMsg1.visibility = View.VISIBLE
                        lblLoginErrorMsg1.text = getString(R.string.validate_email_exists)
                    }
                }
            } else {
                loading.dismiss()
                txtEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                lblLoginErrorMsg1.visibility = View.VISIBLE
                lblLoginErrorMsg1.text = getString(R.string.validate_email_format)
            }
        }
    }

    private fun attemptLogin(email : String, password : String) {
        auth.signInWithEmailAndPassword(email, password).apply {
            addOnSuccessListener {
                cloudFirestore.collection("User").document(auth.currentUser!!.uid).get()
                    .addOnSuccessListener { userInfo ->
                        loading.dismiss()
                        val intent = when (userInfo.get("role")) {
                            "standard" -> Intent(this@ActLogin, ActUserStandard::class.java)
                            "admin" -> Intent(this@ActLogin, ActUserAdmin::class.java)
                            else -> null
                        }
                        startActivity(intent)
                    }
            }
            addOnFailureListener {
                loading.dismiss()
                bind.txtPassword.setBackgroundResource(R.drawable.bg_textfield_error)
                bind.lblLoginErrorMsg2.visibility = View.VISIBLE
            }
        }
    }

    //INPUT VALIDATOR
    private fun textWatcher(textField : EditText) {
        textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                with(bind) {
                    btnLogin.isEnabled = txtEmail.text.isNotEmpty() and
                            txtPassword.text.isNotEmpty()
                }
            }
        })
    }
}