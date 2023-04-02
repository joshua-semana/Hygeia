package com.hygeia

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.Utilities.msg
import com.hygeia.Utilities.msgDlg
import com.hygeia.databinding.ActivityLoginBinding

class ActLogin : AppCompatActivity() {
    private lateinit var bind : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    private var loginAttemptCount = 0
    private var cloudFirestore = Firebase.firestore
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(bind.root)

        with(bind) {
            //MAIN FUNCTIONS
            btnLogin.setOnClickListener {
                if (isInternetConnected(applicationContext)) {
                    login(txtEmail.text.toString(), txtPassword.text.toString())
                } else {
                    msgDlg(this@ActLogin, "no-wifi", "No internet connection", getString(R.string.dlg_no_wifi))
                }
            }

            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
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

            //VALIDATION
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    btnLogin.isEnabled = txtEmail.text.isNotEmpty() and
                            txtPassword.text.isNotEmpty()
                }
            }

            txtEmail.addTextChangedListener(textWatcher)
            txtPassword.addTextChangedListener(textWatcher)
        }
        //NAVIGATION
        bind.btnGoToForgotPassword.setOnClickListener {
            startActivity(Intent(this, ActForgotPassword::class.java))
        }

        bind.btnGoToCreateAccountPart1.setOnClickListener {
            startActivity(Intent(this, ActCreateAccount::class.java))
        }
    }

    //TODO: CREATE VALIDATE INPUT FUNCTION BEFORE LOGIN
    private fun login(email : String, password : String) {
        if (Utilities.emailPattern.matches(email)) {
            with(bind) {
                txtEmail.setBackgroundResource(R.drawable.bg_textfield_default)
                txtPassword.setBackgroundResource(R.drawable.bg_textfield_default)
                lblLoginErrorMsg1.visibility = View.GONE
                lblLoginErrorMsg2.visibility = View.GONE
            }
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                try {
                    val signInMethod = task.result?.signInMethods?:emptyList<String>()
                    if (signInMethod.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { signInTask ->
                            if (signInTask.isSuccessful) {
                                loginAttemptCount = 0
                                cloudFirestore.collection("User").document(auth.currentUser!!.uid).get()
                                    .addOnCompleteListener{ userTask ->
                                        if (userTask.isSuccessful){
                                            val userField = userTask.result
                                            if (userField != null){
                                                val userRole = userField.get("role").toString()
                                                if (userRole == "standard"){
                                                    msg("Login successfully!")
                                                    startActivity(Intent(this, ActUserStandard::class.java))
                                                }else if (userRole == "admin"){
                                                    msg("Login successfully!")
                                                    startActivity(Intent(this, ActUserAdmin::class.java))
                                                }
                                            }
                                        } else {
                                            msg("Error getting user data")
                                        }
                                    }
                            } else {
                                if (loginAttemptCount >= 3) {
                                    msg("You've attempted to login 3 times.")
                                } else {
                                    loginAttemptCount++
                                    bind.txtPassword.setBackgroundResource(R.drawable.bg_textfield_error)
                                    bind.lblLoginErrorMsg2.visibility = View.VISIBLE
                                }
                            }
                        }
                    } else {
                        bind.txtEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                        bind.lblLoginErrorMsg1.visibility = View.VISIBLE
                        bind.lblLoginErrorMsg1.text = getString(R.string.validate_email_exists)
                    }
                } catch (e : java.lang.Exception){
                    Log.e(TAG, "Error checking email registration", e)
                }
            }
        } else {
            bind.txtEmail.setBackgroundResource(R.drawable.bg_textfield_error)
            bind.lblLoginErrorMsg1.visibility = View.VISIBLE
            bind.lblLoginErrorMsg1.text = getString(R.string.validate_email_format)
        }
    }
}