package com.hygeia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import com.hygeia.databinding.ActivityCreateAccountPart2Binding

class CreateAccountPart2 : AppCompatActivity() {
    private lateinit var bind: ActivityCreateAccountPart2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCreateAccountPart2Binding.inflate(layoutInflater)
        setContentView(bind.root)

        with(bind) {
            //ELEMENT BEHAVIOR
            layoutCreateAccountPart2.setOnClickListener {
                getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                layoutCreateAccountPart2.requestFocus()
                currentFocus?.clearFocus()
            }

            tglShowPassword.setOnCheckedChangeListener{ _, isChecked ->
                if (isChecked) {
                    txtNewPassword.transformationMethod = null
                    txtNewPassword.setSelection(txtNewPassword.text.length)
                } else {
                    txtNewPassword.transformationMethod = PasswordTransformationMethod()
                    txtNewPassword.setSelection(txtNewPassword.text.length)
                }
            }

            tglShowConfirmPassword.setOnCheckedChangeListener{ _, isChecked ->
                if (isChecked) {
                    txtNewConfirmPassword.transformationMethod = null
                    txtNewConfirmPassword.setSelection(txtNewConfirmPassword.text.length)
                } else {
                    txtNewConfirmPassword.transformationMethod = PasswordTransformationMethod()
                    txtNewConfirmPassword.setSelection(txtNewConfirmPassword.text.length)
                }
            }

            //VALIDATION
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    btnContinueCreateAccountToPart3.isEnabled = txtNewEmail.text.isNotEmpty() and
                            txtNewPhoneNumber.text.isNotEmpty() and
                            txtNewPassword.text.isNotEmpty() and
                            txtNewConfirmPassword.text.isNotEmpty()
                }
            }

            txtNewEmail.addTextChangedListener(textWatcher)
            txtNewPhoneNumber.addTextChangedListener(textWatcher)
            txtNewPassword.addTextChangedListener(textWatcher)
            txtNewConfirmPassword.addTextChangedListener(textWatcher)
        }

        //NAVIGATION
        bind.btnBackToCreateAccountPart1.setOnClickListener {
            startActivity(Intent(this, CreateAccountPart1::class.java))
        }

        bind.btnContinueCreateAccountToPart3.setOnClickListener {
            startActivity(Intent(this, CreateAccountPart3::class.java))
        }
    }
}