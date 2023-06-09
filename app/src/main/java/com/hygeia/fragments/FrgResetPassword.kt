package com.hygeia.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.classes.ButtonType
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.dlgLoading
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.objects.Utilities.passwordPattern
import com.hygeia.databinding.FrgResetPasswordBinding
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.showRequiredTextField

class FrgResetPassword : Fragment() {

    private lateinit var bind: FrgResetPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loading : Dialog

    private val db = FirebaseFirestore.getInstance()
    private val userRef = db.collection("User")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgResetPasswordBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        with(bind) {
            //MAIN FUNCTIONS
            btnUpdatePassword.setOnClickListener {
                clearTextError(txtLayoutPassword, txtLayoutConfirmPassword)
                if (isInternetConnected(requireContext())) {
                    if (inputsAreNotEmpty()) {
                        validateInputs()
                    } else {
                        showRequiredTextField(
                            txtPassword to txtLayoutPassword,
                            txtConfirmPassword to txtLayoutConfirmPassword
                        )
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
                onBackPressed()
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
            return root
        }
    }
    private fun onBackPressed(){
        dlgConfirmation(requireContext(), "going back") {
            if (it == ButtonType.PRIMARY) {
                parentFragmentManager.popBackStack()
            }
        }.show()
    }
    private fun inputsAreNotEmpty(): Boolean {
        return when {
            bind.txtPassword.text!!.isEmpty() -> false
            bind.txtConfirmPassword.text!!.isEmpty() -> false
            else -> true
        }
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
        loading = dlgLoading(requireContext())
        loading.show()
        val email = arguments?.getString("email").toString()
        val password = arguments?.getString("password").toString()
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            it.user!!.updatePassword(newPassword)
            userRef.document(it.user!!.uid)
                .update("password", newPassword)
                .addOnSuccessListener {
                    loading.dismiss()
                    dlgStatus(requireContext(), "success update password").apply {
                        setOnDismissListener {
                            requireActivity().finish()
                        }
                        show()
                    }
                }
        }
    }
}
