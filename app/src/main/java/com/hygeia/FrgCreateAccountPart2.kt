package com.hygeia

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.navigation.Navigation
import com.hygeia.databinding.FrgCreateAccountPart2Binding

class FrgCreateAccountPart2 : Fragment() {
    private lateinit var bind : FrgCreateAccountPart2Binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgCreateAccountPart2Binding.inflate(inflater, container, false)

        with(bind) {
            //ELEMENT BEHAVIOR
            mainLayout.setOnClickListener {
                requireActivity().getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(requireView().findFocus()?.windowToken, 0)
                mainLayout.requestFocus()
                requireView().findFocus()?.clearFocus()
            }

            tglShowPassword.setOnCheckedChangeListener { _, isChecked ->
                val passwordDisplay = if (isChecked) null else PasswordTransformationMethod()
                with(txtNewPassword) {
                    this.transformationMethod = passwordDisplay
                    setSelection(text.length)
                }
            }

            tglShowConfirmPassword.setOnCheckedChangeListener { _, isChecked ->
                val passwordDisplay = if (isChecked) null else PasswordTransformationMethod()
                with(txtNewConfirmPassword) {
                    this.transformationMethod = passwordDisplay
                    setSelection(text.length)
                }
            }

            //VALIDATION
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    btnContinueCreateAccountToPart3.isEnabled = txtNewEmail.text.isNotEmpty() and
                            txtNewPhoneNumber.text.isNotEmpty() and
                            txtNewPassword.text.isNotEmpty() and
                            txtNewConfirmPassword.text.isNotEmpty()

                }
            }

            txtNewEmail.addTextChangedListener(textWatcher)
            txtNewPhoneNumber.addTextChangedListener(textWatcher)
            txtNewPassword.addTextChangedListener(textWatcher)
            txtNewConfirmPassword.addTextChangedListener(textWatcher)
        }

        //NAVIGATION
        bind.btnContinueCreateAccountToPart3.setOnClickListener {
            Navigation.findNavController(bind.root).navigate(R.id.CreateAccountPart2ToPart3)
        }

        bind.btnBackToCreateAccountPart1.setOnClickListener {
            activity?.onBackPressed()
        }

        return bind.root
    }
}