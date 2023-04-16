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
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.hygeia.Utilities.msg
import com.hygeia.databinding.FrgOtpBinding
import java.util.concurrent.TimeUnit

class FrgOTP : Fragment() {
    private lateinit var bind : FrgOtpBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var otpArgument : String
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
            btnResend.setOnClickListener {
                resendOTP()
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
    private fun resendOTP() {
        val phoneNumber = arguments?.getString("phoneNumber").toString()
        val resendToken : PhoneAuthProvider.ForceResendingToken?  = arguments?.getParcelable("resendToken")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .apply {
                if (resendToken != null) {
                    setForceResendingToken(resendToken)
                }
            }
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                requireActivity().msg("Invalid request")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                requireActivity().msg("Too many request")
            }
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            otpArgument = verificationId
            resendToken = token
        }
    }
    private fun validateInput(otp : String) {
        //TODO : VALIDATE IF OTP IS CORRECT
        otpArgument = arguments?.getString("OTP").toString()
        val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
            otpArgument, otp
        )
        signInWithPhoneAuthCredential(credential)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    requireActivity().msg("Success")
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        requireActivity().msg("Invalid")
                    }
                    // Update UI
                }
            }
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