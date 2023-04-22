package com.hygeia.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.dlgNoInternet
import com.hygeia.Utilities.dlgRequiredFields
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.Utilities.msg
import com.hygeia.databinding.FrgForgotPasswordBinding
import java.util.concurrent.TimeUnit

class FrgForgotPassword : Fragment() {
    private lateinit var bind: FrgForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loading: Dialog
    private val db = FirebaseFirestore.getInstance()
    private var phoneNumber = ""

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
        with(bind) {
            clearTextError()
            if (emailPattern.matches(email)) {
                loading.show()
                auth.fetchSignInMethodsForEmail(email).addOnSuccessListener { getEmailList ->
                    if (getEmailList.signInMethods?.isNotEmpty() == true) {
                        getPhoneNumber(email).addOnSuccessListener { result ->
                            phoneNumber = result.toString()
                            getPassword(email).addOnSuccessListener { password ->
                                sendOTP(phoneNumber, email, password.toString())
                            }
                        }
                    } else {
                        txtLayoutEmail.error = getString(R.string.error_email_registered)
                    }
                }
            } else {
                txtLayoutEmail.error = getString(R.string.error_email_format)
            }
        }
    }

    private fun getPhoneNumber(email: String): Task<String> {
        val query = db.collection("User").whereEqualTo("email", email)
        return query.get().continueWith { task ->
            val phoneNumber = task.result?.documents?.get(0)?.getString("phoneNumber").toString()
            phoneNumber
        }
    }

    private fun getPassword(email: String): Task<String> {
        val query = db.collection("User").whereEqualTo("email", email)
        return query.get().continueWith { task ->
            val password = task.result?.documents?.get(0)?.getString("password").toString()
            password
        }
    }

    private fun sendOTP(phoneNumber: String, email: String, password: String) {
        val options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS).setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                override fun onVerificationFailed(ex: FirebaseException) {
                    if (ex is FirebaseAuthInvalidCredentialsException) {
                        requireActivity().msg("Invalid request")
                    } else if (ex is FirebaseTooManyRequestsException) {
                        requireActivity().msg("Too many request")
                    }
                    loading.dismiss()
                }
                override fun onCodeSent(
                    otp: String,
                    resendToken: PhoneAuthProvider.ForceResendingToken,
                ) {
                    loading.dismiss()
                    sendArguments(phoneNumber, email, password, otp, resendToken)
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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