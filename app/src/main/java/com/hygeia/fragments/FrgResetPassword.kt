package com.hygeia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.hygeia.Utilities.dlgNoInternet
import com.hygeia.Utilities.dlgRequiredFields
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.databinding.FrgResetPasswordBinding

class FrgResetPassword : Fragment() {
    private lateinit var bind: FrgResetPasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgResetPasswordBinding.inflate(inflater, container, false)

        with(bind) {
            //MAIN FUNCTIONS
            btnUpdatePassword.setOnClickListener {
                if (txtPassword.text!!.isNotEmpty() or txtConfirmPassword.text!!.isNotEmpty()) {
                    dlgRequiredFields(requireContext()).show()
                } else {
                    if (isInternetConnected(requireContext())) {
                        // TODO: VALIDATE RESET PASSWORD INPUTS
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
}

