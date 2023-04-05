package com.hygeia.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.hygeia.databinding.FrgOtpBinding

class FrgOTP : Fragment() {
    private lateinit var bind : FrgOtpBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgOtpBinding.inflate(inflater, container, false)

        with(bind) {
            val title = "${titleOTP.text} ${arguments?.getString("phoneNumber")}"

            //POPULATE
            titleOTP.text = title

            //MAIN FUNCTIONS
            btnVerify.setOnClickListener {
                validateInput(txtOTP.text.toString())
            }

            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                requireContext().getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
                }
                mainLayout.requestFocus()
                requireView().findFocus()?.clearFocus()
            }

            //INPUT VALIDATIONS
            textWatcher(txtOTP)

            //NAVIGATION
            btnBack.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
            return root
        }
    }

    private fun validateInput(otp : String) {
        //TODO : VALIDATE IF OTP IS CORRECT
    }

    private fun sendArguments() {
        //TODO : GET EMAIL AND PHONE NUMBER FROM FrgForgotPassword.kt
    }
    //INPUT VALIDATOR
    private fun textWatcher(textField : EditText) {
        with(bind) {
            textField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    btnVerify.isEnabled = txtOTP.text.length == 6
                }
            })
        }
    }
}