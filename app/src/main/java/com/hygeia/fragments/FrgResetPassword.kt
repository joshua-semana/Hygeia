package com.hygeia.fragments

import android.app.Dialog
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
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.dlgNoInternet
import com.hygeia.Utilities.dlgRequiredFields
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.Utilities.msg
import com.hygeia.Utilities.passwordPattern
import com.hygeia.databinding.FrgResetPasswordBinding

class FrgResetPassword : Fragment() {

    private lateinit var bind: FrgResetPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loading: Dialog
    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgResetPasswordBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        loading = dlgLoading(requireContext())

        with(bind) {
            //MAIN FUNCTIONS
            btnUpdatePassword.setOnClickListener {
                if (txtPassword.text!!.isEmpty() or txtConfirmPassword.text!!.isEmpty()) {
                    dlgRequiredFields(requireContext()).show()
                } else {
                    if (isInternetConnected(requireContext())) {
                        clearTextError()
                        validateInputs()
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
        bind.txtLayoutPassword.isErrorEnabled = false
        bind.txtLayoutConfirmPassword.isErrorEnabled = false
    }

    private fun validateInputs() {
        with(bind) {
            if (txtPassword.text!!.matches(passwordPattern)) {
                if (txtConfirmPassword.text!!.toString() == txtPassword.text!!.toString()) {
                    updatePassword(txtPassword.text.toString())
                } else {
                    txtLayoutConfirmPassword.error = getString(R.string.error_password_matched)
                }
            } else {
                txtLayoutPassword.error = getString(R.string.error_password_format)
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        val loading = dlgLoading(requireContext())
        val email = arguments?.getString("email").toString()
        val password = arguments?.getString("password").toString()
        loading.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = it.user?.uid
                it.user?.updatePassword(newPassword)
                    ?.addOnSuccessListener {
                        db.collection("User")
                            .document(uid!!)
                            .update("password", newPassword)
                            .addOnSuccessListener {
                                requireActivity().msg("Success")
                            }
                    }
                loading.dismiss()
            }
    }
}

