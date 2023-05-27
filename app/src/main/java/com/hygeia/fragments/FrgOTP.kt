package com.hygeia.fragments

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.msg
import com.hygeia.databinding.FrgOtpBinding
import java.util.concurrent.TimeUnit

class FrgOTP : Fragment() {
    private lateinit var bind: FrgOtpBinding
    private lateinit var auth: FirebaseAuth
    private var argToken: PhoneAuthProvider.ForceResendingToken? = null
    //private var argResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var argOtp: String
    private lateinit var argPhoneNumber: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgOtpBinding.inflate(inflater, container, false)
        auth = Firebase.auth

        with(bind) {
            timerForResendOTP()
            val censoredPhoneNumber = "${lblDescription.text} +63 9** *** ${
                arguments?.getString("phoneNumber")?.substring(9)
            }"

            //POPULATE
            lblDescription.text = censoredPhoneNumber
            argOtp = arguments?.getString("otp").toString()
            argToken = arguments?.getParcelable("resendToken")
            argPhoneNumber = arguments?.getString("phoneNumber").toString()

            //MAIN FUNCTIONS
            btnVerify.setOnClickListener {
                if (txtOtp.text!!.isNotEmpty()) {
                    validateInput(txtOtp.text.toString())
                } else {
                    dlgStatus(requireContext(), "empty field").show()
                }
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

            //NAVIGATION
            btnBack.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            return root
        }
    }

    private fun timerForResendOTP() {
        with(bind) {
            val countDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = millisUntilFinished / 1000
                    val resendTimer = "Resend code in $seconds seconds"
                    btnResend.isEnabled = false
                    btnResend.text = resendTimer
                }
                override fun onFinish() {
                    btnResend.isEnabled = true
                    btnResend.text = getString(R.string.btn_resend)
                }
            }
            countDownTimer.start()
        }
    }

    private fun validateInput(otp: String) {
        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(argOtp, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendOTP() {
        timerForResendOTP()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(argPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                override fun onVerificationFailed(ex: FirebaseException) {}
                override fun onCodeSent(
                    verificationId: String,
                    resendToken: PhoneAuthProvider.ForceResendingToken,
                ) { }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).apply {
            addOnSuccessListener {
                requireActivity().msg("One-time PIN is correct.")
                sendArguments()
            }
            addOnFailureListener {
                bind.txtLayoutOtp.error = getString(R.string.error_otp_incorrect)
            }
        }
    }

    private fun sendArguments() {
        val email = arguments?.getString("email").toString()
        val password = arguments?.getString("password").toString()
        val bundle = Bundle().apply {
            putString("email", email)
            putString("password", password)
        }
        val fragment = FrgResetPassword().apply { arguments = bundle }
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