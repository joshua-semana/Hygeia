package com.hygeia.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.getSystemService
import com.hygeia.databinding.FrgResetPasswordBinding

class FrgResetPassword : Fragment() {
    private lateinit var bind : FrgResetPasswordBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgResetPasswordBinding.inflate(inflater, container, false)

        with(bind) {
            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                requireContext().getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
                }
                mainLayout.requestFocus()
                requireView().findFocus()?.clearFocus()
            }

            tglShowPassword.setOnCheckedChangeListener { _, isChecked ->
                val passwordDisplay = if (isChecked) null else PasswordTransformationMethod()
                with(txtResetPassword) {
                    this.transformationMethod = passwordDisplay
                    setSelection(text.length)
                }
            }

            tglShowConfirmPassword.setOnCheckedChangeListener { _, isChecked ->
                val passwordDisplay = if (isChecked) null else PasswordTransformationMethod()
                with(txtResetConfirmPassword) {
                    this.transformationMethod = passwordDisplay
                    setSelection(text.length)
                }
            }

            //INPUT VALIDATION
            textWatcher(txtResetPassword)
            textWatcher(txtResetConfirmPassword)

            //NAVIGATION
            btnBack.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            return root
        }
    }

    //INPUT VALIDATOR
    private fun textWatcher(textField : EditText) {
        textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                with(bind) {
                    btnUpdatePassword.isEnabled = txtResetPassword.text.isNotEmpty() and
                            txtResetConfirmPassword.text.isNotEmpty()
                }
            }
        })
    }
}

