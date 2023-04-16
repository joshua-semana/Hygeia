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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.dlgMessage
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.databinding.FrgForgotPasswordBinding

class FrgForgotPassword : Fragment() {
    private lateinit var bind : FrgForgotPasswordBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgForgotPasswordBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        with(bind) {
            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                requireContext().getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
                }
                mainLayout.requestFocus()
                requireView().findFocus()?.clearFocus()
            }

            //VALIDATION
            textWatcher(txtForgotEmail)

            //NAVIGATION
            btnContinue.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    validateInput(txtForgotEmail.text.toString())
                } else {
                    dlgMessage(
                        requireContext(),
                        "no-wifi",
                        getString(R.string.dlg_title_wifi),
                        getString(R.string.dlg_body_wifi),
                        "Okay"
                    )
                }
            }

            btnBack.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            return root
        }
    }

    private fun validateInput(email : String) {
        val loading = dlgLoading(requireContext())
        with(bind) {
            txtForgotEmail.setBackgroundResource(R.drawable.bg_textfield_default)
            lblForgotPasswordErrorMsg1.visibility = View.GONE

            if (!emailPattern.matches(email)) {
                txtForgotEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                lblForgotPasswordErrorMsg1.visibility = View.VISIBLE
                lblForgotPasswordErrorMsg1.text = getString(R.string.validate_email_format)
            } else {
                loading.show()
                auth.fetchSignInMethodsForEmail(email).addOnSuccessListener{ getEmailList ->
                    if (getEmailList.signInMethods?.isEmpty() == true) {
                        txtForgotEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                        lblForgotPasswordErrorMsg1.visibility = View.VISIBLE
                        lblForgotPasswordErrorMsg1.text = getString(R.string.validate_email_exists)
                    } else {
                        sendOTP()
                        sendArguments(getPhoneNumber())
                    }
                    loading.dismiss()
                }
            }
        }
    }

    private fun sendOTP() {
        //TODO : SEND OTP FUNCTION, ADD 30 SECONDS GLOBAL OTP TIME COUNTER
    }

    private fun getPhoneNumber(): String {
        //TODO : GET THE LAST 4 DIGITS OF PHONE NUMBER
        val phoneNumber = "09087788795"
        return phoneNumber.substring(7, 11)
    }

    private fun sendArguments(phoneNumber : String) {
        val bundle = Bundle().apply {
            putString("phoneNumber", phoneNumber)
        }
        val fragment = FrgOTP().apply { arguments = bundle }
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
            .replace(R.id.containerForgotPassword, fragment)
            .addToBackStack(null)
            .commit()
    }

    //INPUT VALIDATOR
    private fun textWatcher(textField : EditText) {
        with(bind) {
            textField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    btnContinue.isEnabled = txtForgotEmail.text.isNotEmpty()
                }
            })
        }
    }
}