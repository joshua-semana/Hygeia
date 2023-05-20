package com.hygeia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities.dlgInformation
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.Utilities.passwordPattern
import com.hygeia.Utilities.phoneNumberPattern
import com.hygeia.databinding.FrgCreateAccountPart2Binding
import java.util.concurrent.CompletableFuture

class FrgCreateAccountPart2 : Fragment() {
    private lateinit var bind: FrgCreateAccountPart2Binding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgCreateAccountPart2Binding.inflate(inflater, container, false)
        auth = Firebase.auth
        val loading = dlgLoading(requireContext())

        with(bind) {
            //MAIN FUNCTIONS
            btnContinue.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    if (inputsAreNotEmpty()) {
                        loading.show()
                        clearTextErrors()

                        val validations = arrayOf(
                            validateEmail(),
                            validatePhoneNumber(),
                            validatePassword(),
                            validateConfirmPassword()
                        )

                        CompletableFuture.allOf(*validations).thenRunAsync {
                            loading.dismiss()
                            if (validations.all { it.get() }) {
                                sendArguments()
                            }
                        }
                    } else {
                        dlgInformation(requireContext(), "empty field").show()
                    }
                } else {
                    dlgInformation(requireContext(), "no internet").show()
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

    private fun inputsAreNotEmpty(): Boolean {
        return when {
            bind.txtEmail.text!!.isEmpty() -> false
            bind.txtPhoneNumber.text!!.isEmpty() -> false
            bind.txtPassword.text!!.isEmpty() -> false
            bind.txtConfirmPassword.text!!.isEmpty() -> false
            else -> true
        }
    }

    private fun clearTextErrors() {
        bind.txtLayoutEmail.isErrorEnabled = false
        bind.txtLayoutPhoneNumber.isErrorEnabled = false
        bind.txtLayoutPassword.isErrorEnabled = false
        bind.txtLayoutConfirmPassword.isErrorEnabled = false
    }

    private fun validateEmail(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        with(bind) {
            if (txtEmail.text!!.matches(emailPattern)) {
                auth.fetchSignInMethodsForEmail(txtEmail.text!!.toString())
                    .addOnSuccessListener { getEmailList ->
                        if (getEmailList.signInMethods?.isEmpty() == true) {
                            future.complete(true)
                        } else {
                            txtLayoutEmail.error = getString(R.string.error_email_taken)
                            future.complete(false)
                        }
                    }
            } else {
                txtLayoutEmail.error = getString(R.string.error_email_format)
                future.complete(false)
            }
        }
        return future
    }

    private fun validatePhoneNumber(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        with(bind) {
            val phoneNumber =
                (txtLayoutPhoneNumber.prefixText.toString() + txtPhoneNumber.text.toString()).trim()
            if (phoneNumber.matches(phoneNumberPattern)) {
                db.collection("User")
                    .whereEqualTo("phoneNumber", phoneNumber).get()
                    .addOnSuccessListener { getPhoneNumbers ->
                        if (getPhoneNumbers.isEmpty) {
                            future.complete(true)
                        } else {
                            txtLayoutPhoneNumber.error = getString(R.string.error_phone_taken)
                            future.complete(false)
                        }
                    }
            } else {
                txtLayoutPhoneNumber.error = getString(R.string.error_phone_format)
                future.complete(false)
            }
        }
        return future
    }

    private fun validatePassword(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        with(bind) {
            if (txtPassword.text!!.matches(passwordPattern)) {
                future.complete(true)
            } else {
                txtLayoutPassword.error = getString(R.string.error_password_format)
                future.complete(false)
            }
        }
        return future
    }

    private fun validateConfirmPassword(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        with(bind) {
            if (txtConfirmPassword.text!!.toString() == txtPassword.text!!.toString()) {
                future.complete(true)
            } else {
                txtLayoutConfirmPassword.error = getString(R.string.error_password_matched)
                future.complete(false)
            }
        }
        return future
    }

    private fun sendArguments() {
        with(bind) {
            val bundle = Bundle().apply {
                putString("gender", arguments?.getString("gender"))
                putString("firstname", arguments?.getString("firstname"))
                putString("lastname", arguments?.getString("lastname"))
                putString("birthdate", arguments?.getString("birthdate"))
                putString("email", txtEmail.text.toString())
                putString(
                    "phoneNumber",
                    (txtLayoutPhoneNumber.prefixText.toString() + txtPhoneNumber.text.toString()).trim()
                )
                putString("password", txtPassword.text.toString())
            }
            val fragment = FrgCreateAccountPart3().apply { arguments = bundle }
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    androidx.appcompat.R.anim.abc_fade_in,
                    androidx.appcompat.R.anim.abc_fade_out
                )
                .replace(R.id.containerCreateAccount, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
