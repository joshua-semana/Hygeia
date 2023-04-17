package com.hygeia.fragments

import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.gms.tasks.Task
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

    private var phoneNumber: String = "+639123456789"
    private var otp: String = "123456"

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
//                        getPhoneNumber(email).addOnSuccessListener { result ->
//                            phoneNumber = result.toString()
//                            loading.dismiss()
//                            sendOTP(phoneNumber)
//                        }
                        loading.dismiss()
                        sendOTP(phoneNumber)
                    }
                    loading.dismiss()
                }
            }
        }
    }

    private fun getPhoneNumber(email: String): Task<String> {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("User").whereEqualTo("email", email)

        return query.get().continueWith { task ->
            val phoneNumber = task.result?.documents?.get(0)?.getString("phoneNumber").toString()
            phoneNumber
        }
    }

    private fun sendOTP(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) { }
                override fun onVerificationFailed(ex: FirebaseException) {
                    if (ex is FirebaseAuthInvalidCredentialsException) {
                        requireActivity().msg("Invalid request")
                    } else if (ex is FirebaseTooManyRequestsException) {
                        requireActivity().msg("Too many request")
                    }
                }
                override fun onCodeSent(
                    verificationId: String,
                    resendToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    sendArguments(phoneNumber, otp, resendToken)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun sendArguments(phoneNumber : String, otp : String, token : Parcelable) {
        val bundle = Bundle().apply {
            putString("phoneNumber", phoneNumber)
            putString("otp", otp)
            putParcelable("token", token)
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