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
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.objects.OTPManager
import com.hygeia.R
import com.hygeia.classes.ButtonType
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.dlgLoading
import com.hygeia.objects.Utilities.emailPattern
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.databinding.FrgForgotPasswordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                        clearTextError(txtLayoutEmail)
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

    private fun validateInput(email: String) {
        auth = Firebase.auth
        clearTextError()
        with(bind) {
            if (email.matches(emailPattern)) {
                loading.show()
                lifecycleScope.launch(Dispatchers.Main) {
                    getEmailAddress(email)
                    if (emailAddress.isNotEmpty()) {
                        getPhoneNumber()
                        getPassword()
                        OTPManager.requestOTP(requireActivity(), phoneNumber, auth)
                        if (OTPManager.getOTP() != null) {
                            OTPManager.verifyOtp(
                                requireActivity(),
                                requireContext(),
                                phoneNumber,
                                auth,
                                OTPManager.getOTP()
                            ) {
                                if (it == ButtonType.VERIFIED) {
                                    loading.dismiss()
                                    sendArguments()
                                }
                            }.show()
                        }
                        loading.dismiss()
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

    private fun sendArguments() {
        val bundle = Bundle().apply {
            putString("email", emailAddress)
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