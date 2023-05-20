package com.hygeia.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities.dlgStatus
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.Utilities.msg
import com.hygeia.databinding.FrgForgotPasswordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class FrgForgotPassword : Fragment() {
    private lateinit var bind: FrgForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loading: Dialog

    private val db = FirebaseFirestore.getInstance()
    private val userRef = db.collection("User")

    private var emailAddress = ""
    private var phoneNumber = ""
    private var password = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgForgotPasswordBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        loading = dlgLoading(requireContext())
        with(bind) {
            //MAIN FUNCTIONS
            btnContinue.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    if (txtEmail.text!!.isNotEmpty()) {
                        validateInput(txtEmail.text.toString())
                    } else {
                        clearTextError()
                        dlgStatus(requireContext(), "empty field").show()
                    }
                } else {
                    dlgStatus(requireContext(), "no internet").show()
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
        clearTextError()
        with(bind) {
            if (email.matches(emailPattern)) {
                loading.show()
                lifecycleScope.launch(Dispatchers.Main) {
                    getEmailAddress(email)
                    if (emailAddress.isNotEmpty()) {
                        getPhoneNumber()
                        getPassword()
                        makeFirebaseRequest()
                    } else {
                        loading.dismiss()
                        txtLayoutEmail.error = getString(R.string.error_email_registered)
                    }
                }
            } else {
                txtLayoutEmail.error = getString(R.string.error_email_format)
            }
        }
    }

    private suspend fun getEmailAddress(email: String) {
        val query = userRef.whereEqualTo("email", email).get().await()
        emailAddress = if (query.isEmpty) "" else email
    }

    private suspend fun getPhoneNumber() {
        val query = userRef.whereEqualTo("email", emailAddress).get().await()
        phoneNumber = query.documents[0].get("phoneNumber").toString()
    }

    private suspend fun getPassword() {
        val query = userRef.whereEqualTo("email", emailAddress).get().await()
        password = query.documents[0].get("password").toString()
    }

    private var requestCount = 0
    private val requestLimit = 10
    private val requestInterval = 1_000L // 1 second

    private suspend fun makeFirebaseRequest() {
        try {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                    override fun onVerificationFailed(ex: FirebaseException) {
                        if (ex is FirebaseAuthInvalidCredentialsException) {
                            requireActivity().msg("Invalid request")
                        } else if (ex is FirebaseTooManyRequestsException) {
                            requireActivity().msg("Too many request. Please try again later.")
                        }
                        loading.dismiss()
                    }
                    override fun onCodeSent(
                        otp: String,
                        resendToken: PhoneAuthProvider.ForceResendingToken,
                    ) {
                        loading.dismiss()
                        sendArguments(phoneNumber, emailAddress, password, otp, resendToken)
                    }
                }).build()
            PhoneAuthProvider.verifyPhoneNumber(options)
            requestCount++
        } catch (e: FirebaseTooManyRequestsException) {
            delay(requestInterval * (2.0.pow(requestCount.toDouble())).toLong())
            makeFirebaseRequest()
        }

        if (requestCount >= requestLimit) {
            delay(requestInterval)
            requestCount = 0
        }
    }

    private fun sendArguments(
        phoneNumber: String,
        email: String,
        password: String,
        otp: String,
        token: Parcelable,
    ) {
        val bundle = Bundle().apply {
            putString("phoneNumber", phoneNumber)
            putString("email", email)
            putString("password", password)
            putString("otp", otp)
            putParcelable("token", token)
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