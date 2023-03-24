package com.hygeia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hygeia.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var bind: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)
        auth = Firebase.auth

        bind.btnGoToCreateAccount.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
//            setContentView(R.layout.activity_create_account)
        }
        bind.btnLogin.setOnClickListener {
            if (TextUtils.isEmpty(bind.txtUsername.text) && TextUtils.isEmpty(bind.txtPassword.text)){
                Toast.makeText(this, "Please input the required details", Toast.LENGTH_SHORT).show()
            }
            auth.signInWithEmailAndPassword(bind.txtUsername.text.toString(),bind.txtPassword.text.toString())
                .addOnCompleteListener(this){ task ->
                    if (task.isSuccessful){
                        Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, StandardMainActivity::class.java))
                    }else{
                        Toast.makeText(this, "Please input correct email or password", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        bind.tglShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                bind.txtPassword.transformationMethod = null
                bind.tglShowPassword.setBackgroundResource(R.drawable.ic_visibility_off)
                bind.txtPassword.setSelection(bind.txtPassword.text.length)
            } else {
                bind.txtPassword.transformationMethod = PasswordTransformationMethod()
                bind.tglShowPassword.setBackgroundResource(R.drawable.ic_visibility)
                bind.txtPassword.setSelection(bind.txtPassword.text.length)
            }
        }
    }
}