package com.hygeia.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.navigation.Navigation
import com.hygeia.R
import com.hygeia.databinding.FrgForgotPasswordBinding

class FrgForgotPassword : Fragment() {
    private lateinit var bind : FrgForgotPasswordBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgForgotPasswordBinding.inflate(inflater, container, false)

        with(bind) {
            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                requireActivity().getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
                mainLayout.requestFocus()
                requireView().findFocus()?.clearFocus()
            }

            //VALIDATION
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    btnContinue.isEnabled = txtForgotEmail.text.isNotEmpty()
                }
            }

            txtForgotEmail.addTextChangedListener(textWatcher)
        }

        //NAVIGATION
        bind.btnContinue.setOnClickListener {
            Navigation.findNavController(bind.root).navigate(R.id.ForgotPasswordToResetPassword)
        }

        bind.btnBackToLogin.setOnClickListener {
            activity?.onBackPressed()
        }

        return bind.root
    }
}