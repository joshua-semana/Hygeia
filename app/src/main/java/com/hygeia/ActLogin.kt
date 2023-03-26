package com.hygeia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hygeia.databinding.ActivityLoginBinding
class ActLogin : AppCompatActivity() {
    private lateinit var bind : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)

        auth = Firebase.auth

        bind.btnLogin.setOnClickListener {
            //TODO: "Validate email and password, if email doesn't exist in our system, or format
            // is incorrect, set lblLoginErrorMsg1.visibility to View.visible or View.gone"
            auth.signInWithEmailAndPassword(bind.txtEmail.text.toString(), bind.txtPassword.text.toString())
                .addOnCompleteListener(this){ task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, " Successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ActUserStandard::class.java))
                    } else {
                        Toast.makeText(this, "Please input correct email or password", Toast.LENGTH_SHORT).show()
                    }
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
}