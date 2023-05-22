package com.hygeia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities.clearTextError
import com.hygeia.Utilities.dlgStatus
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.Utilities.passwordPattern
import com.hygeia.Utilities.phoneNumberPattern
import com.hygeia.databinding.FrgCreateAccountPart2Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FrgCreateAccountPart2 : Fragment() {
    private lateinit var bind: FrgCreateAccountPart2Binding
    private lateinit var auth: FirebaseAuth

    private val db = FirebaseFirestore.getInstance()
    private val userRef = db.collection("User")

    private var emailAddress = ""
    private var phoneNumber = ""
    private var password = ""
    private var confirmPassword = ""
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
                        clearTextError(txtLayoutEmail, txtLayoutPhoneNumber, txtLayoutPassword, txtLayoutConfirmPassword)
                        emailAddress = txtEmail.text.toString()
                        phoneNumber = (txtLayoutPhoneNumber.prefixText.toString() + txtPhoneNumber.text.toString()).trim()
                        password = txtPassword.text.toString()
                        confirmPassword = txtConfirmPassword.text.toString()

                        lifecycleScope.launch(Dispatchers.Main) {
                            if (inputsAreCorrect()) sendArguments()
                            loading.dismiss()
                        }
                    } else {
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
    private fun inputsAreNotEmpty(): Boolean {
        return when {
            bind.txtEmail.text!!.isEmpty() -> false
            bind.txtPhoneNumber.text!!.isEmpty() -> false
            bind.txtPassword.text!!.isEmpty() -> false
            bind.txtConfirmPassword.text!!.isEmpty() -> false
            else -> true
        }
    }
    private suspend fun inputsAreCorrect(): Boolean {
        var inputErrorCount = 0
        with(bind) {
            if (emailAddress.matches(emailPattern)) {
                if (getEmailExistenceOf(emailAddress)) {
                    inputErrorCount++
                    txtLayoutEmail.error = getString(R.string.error_email_taken)
                }
            } else {
                inputErrorCount++
                txtLayoutEmail.error = getString(R.string.error_email_format)
            }

            if (phoneNumber.matches(phoneNumberPattern)) {
                if (getPhoneNumberExistenceOf(phoneNumber)) {
                    inputErrorCount++
                    txtLayoutPhoneNumber.error = getString(R.string.error_phone_taken)
                }
            } else {
                inputErrorCount++
                txtLayoutPhoneNumber.error = getString(R.string.error_phone_format)
            }

            if (!password.matches(passwordPattern)) {
                inputErrorCount++
                txtLayoutPassword.error = getString(R.string.error_password_format)
            }

            if (confirmPassword != password) {
                inputErrorCount++
                txtLayoutConfirmPassword.error = getString(R.string.error_password_matched)
            }
        }
        return inputErrorCount == 0
    }
    private suspend fun getEmailExistenceOf(email: String): Boolean {
        val query = userRef.whereEqualTo("email", email).get().await()
        return !query.isEmpty
    }
    private suspend fun getPhoneNumberExistenceOf(phoneNumber: String): Boolean {
        val query = userRef.whereEqualTo("phoneNumber", phoneNumber).get().await()
        return !query.isEmpty
    }
    private fun sendArguments() {
        val bundle = Bundle().apply {
            putString("gender", arguments?.getString("gender"))
            putString("firstname", arguments?.getString("firstname"))
            putString("lastname", arguments?.getString("lastname"))
            putString("birthdate", arguments?.getString("birthdate"))
            putString("email", emailAddress)
            putString("phoneNumber", phoneNumber)
            putString("password", password)
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
