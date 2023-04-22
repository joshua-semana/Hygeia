package com.hygeia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.hygeia.Utilities.dlgRequiredFields
import com.hygeia.databinding.FrgOtpBinding

class FrgOTP : Fragment() {
    private lateinit var bind: FrgOtpBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgOtpBinding.inflate(inflater, container, false)

        with(bind) {
            val title = "${lblDescription.text} ${arguments?.getString("phoneNumber")}"

            //POPULATE
            lblDescription.text = title

            //MAIN FUNCTIONS
            btnVerify.setOnClickListener {
                if (txtOtp.text!!.isNotEmpty()) {
                    validateInput(txtOtp.text.toString())
                } else {
                    dlgRequiredFields(requireContext()).show()
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

    private fun validateInput(otp: String) {
        //TODO : VALIDATE IF OTP IS CORRECT
    }

    private fun sendArguments() {
        //TODO : GET EMAIL AND PHONE NUMBER FROM FrgForgotPassword.kt
    }
}