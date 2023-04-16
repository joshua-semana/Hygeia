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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.dlgMessage
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.Utilities.msg
import com.hygeia.databinding.FrgForgotPasswordBinding
import java.util.concurrent.TimeUnit

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

            btnBackToLogin.setOnClickListener {
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
//                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
//                            .addOnCompleteListener{emailCheck ->
//                                if (emailCheck.isSuccessful){
//                                    requireActivity().msg("Check your email")
//                                } else {
//                                    requireActivity().msg("Message Failed")
//                                }
//                            }
                        getPhoneNumber()
                    }
                    loading.dismiss()
                }
            }
        }
    }
    private fun sendOTP() {
        //TODO : SEND OTP FUNCTION, ADD 30 SECONDS GLOBAL OTP TIME COUNTER
    }
    private data class UserData(val phoneNumber: String)
    private fun getPhoneNumber(): UserData {
        //TODO : GET THE LAST 4 DIGITS OF PHONE NUMBER
        var phoneNumber = UserData("")
        with(bind) {
            FirebaseFirestore.getInstance().collection("User")
                .whereEqualTo("email", txtForgotEmail.text.toString())
                .get()
                .addOnSuccessListener { getPhoneNumberList ->
                    phoneNumber = UserData(getPhoneNumberList.documents[0].getString("phoneNumber").toString())
                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber.phoneNumber)   // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(requireActivity())           // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
                .addOnFailureListener {
                    // handle Firestore query failure
                    requireActivity().msg("Error getting phone number")
                }
        }
        return phoneNumber
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
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        val userData = getPhoneNumber()
        val phoneNumber = userData.phoneNumber
        val censorPhoneNumber = "*****${phoneNumber.substring(7)}"
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
            val bundle = Bundle().apply {
                putString("phoneNumber", censorPhoneNumber)
                putString("OTP", verificationId)
                putParcelable("resendToken", token)
            }
            val fragment = FrgOTP().apply { arguments = bundle }
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                .replace(R.id.containerForgotPassword, fragment)
                .addToBackStack(null)
                .commit()
        }
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