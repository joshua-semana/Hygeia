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
import android.widget.Toast
import androidx.core.content.getSystemService
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hygeia.databinding.ActivityLoginBinding
class ActLogin : AppCompatActivity() {
    private lateinit var bind : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var ics : InternetConnectionStatus
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = Firebase.auth
        checkNetworkStatus()
        var attempt = 0
        bind.btnLogin.setOnClickListener {
            //TODO: "Validate email and password, if email doesn't exist in our system, or format
            // is incorrect, set lblLoginErrorMsg1.visibility to View.visible or View.gone"
            val emailPattern = "[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+".toRegex()
            if (emailPattern.matches(bind.txtEmail.text.toString())){
                bind.txtEmail.setBackgroundResource(R.drawable.bg_textfield_default)
                bind.txtPassword.setBackgroundResource(R.drawable.bg_textfield_default)
                bind.lblLoginErrorMsg1.visibility = View.GONE
                bind.lblLoginErrorMsg2.visibility = View.GONE
                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(bind.txtEmail.text.toString())
                    .addOnCompleteListener{ task ->
                        try {
                            val signInMethodFirebase = task.result?.signInMethods ?: emptyList<String>()
                            if (task.isSuccessful && signInMethodFirebase.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)){
                                auth.signInWithEmailAndPassword(bind.txtEmail.text.toString(), bind.txtPassword.text.toString())
                                    .addOnCompleteListener(this){ signInTask ->
                                        if (signInTask.isSuccessful) {
                                            attempt = 0
                                            Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, ActUserStandard::class.java))
                                        } else {
                                            if(attempt >= 3){
                                                Toast.makeText(this, "3 times attempt", Toast.LENGTH_SHORT).show()
                                                //Take action after 3 times attempt
                                            } else{
                                                attempt++
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
            }else {
                bind.txtEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                bind.lblLoginErrorMsg1.visibility = View.VISIBLE
                bind.lblLoginErrorMsg1.text = getString(R.string.validate_email_format)
            }
        }
        with(bind) {
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
    private fun checkNetworkStatus() {
        ics = InternetConnectionStatus(this)
        ics.observe(this){isConnected ->
            if (isConnected) Toast.makeText(this, "Internet is connected", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Internet is not connected", Toast.LENGTH_SHORT).show()
        }
    }
}