package com.hygeia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.dlgNoInternet
import com.hygeia.Utilities.dlgRequiredFields
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.databinding.FrgForgotPasswordBinding

class FrgForgotPassword : Fragment() {
    private lateinit var bind: FrgForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgForgotPasswordBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        with(bind) {
            //MAIN FUNCTIONS
            btnContinue.setOnClickListener {
                if (txtEmail.text!!.isEmpty()) {
                    dlgRequiredFields(requireContext()).show()
                    clearTextError()
                } else {
                    if (isInternetConnected(requireContext())) {
                        validateInput(txtEmail.text.toString())
                    } else {
                        dlgNoInternet(requireContext()).show()
                    }
                }
            }

            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                requireContext().getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
                }
                mainLayout.requestFocus()
                requireView().findFocus()?.clearFocus()
            }
            //NAVIGATION
            btnBack.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            return root
        }
    }

    private fun clearTextError() {
        bind.txtLayoutEmail.isErrorEnabled = false
    }

    private fun validateInput(email: String) {
        val loading = dlgLoading(requireContext())
        with(bind) {
            clearTextError()
            if (emailPattern.matches(email)) {
                loading.show()
                auth.fetchSignInMethodsForEmail(email).addOnSuccessListener { getEmailList ->
                    if (getEmailList.signInMethods?.isNotEmpty() == true) {
                        sendOTP()
                        sendArguments(getPhoneNumber())
                    } else {
                        txtLayoutEmail.error = getString(R.string.error_email_registered)
                    }
                    loading.dismiss()
                }
            } else {
                txtLayoutEmail.error = getString(R.string.error_email_format)
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

    private fun sendArguments(phoneNumber: String) {
        val bundle = Bundle().apply {
            putString("phoneNumber", phoneNumber)
        }
        val fragment = FrgOTP().apply { arguments = bundle }
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                androidx.appcompat.R.anim.abc_fade_in,
                androidx.appcompat.R.anim.abc_fade_out
            )
            .replace(R.id.containerForgotPassword, fragment)
            .addToBackStack(null)
            .commit()
    }
}