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
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hygeia.Utilities.msg
import com.hygeia.databinding.FrgOtpBinding
import java.util.concurrent.TimeUnit

class FrgOTP : Fragment() {
    private lateinit var bind : FrgOtpBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var argToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var argOtp : String
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgOtpBinding.inflate(inflater, container, false)
        auth = Firebase.auth

        with(bind) {
            val title = "${titleOTP.text} ${arguments?.getString("phoneNumber")?.substring(7)}"
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
        val resendToken : PhoneAuthProvider.ForceResendingToken? = arguments?.getParcelable("resendToken")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) { }
                override fun onVerificationFailed(ex: FirebaseException) { }
                override fun onCodeSent(
                    verificationId: String,
                    resendToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    argOtp = verificationId
                    argToken = resendToken
                }
            })
            .apply {
                if (resendToken != null) {
                    setForceResendingToken(resendToken)
                }
            }
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun validateInput(otp : String) {
        argOtp = arguments?.getString("otp").toString()
        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(argOtp, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).apply {
            addOnSuccessListener {
                requireActivity().msg("Success")
            }
            addOnFailureListener {
                requireActivity().msg("Invalid")
            }
        }
    }
//    private fun sendArguments() {
//        //TODO : GET EMAIL AND PHONE NUMBER FROM FrgForgotPassword.kt
//    }
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