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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities
import com.hygeia.databinding.FrgCreateAccountPart2Binding

class FrgCreateAccountPart2 : Fragment() {
    private lateinit var bind : FrgCreateAccountPart2Binding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgCreateAccountPart2Binding.inflate(inflater, container, false)
        auth = Firebase.auth
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

            //INPUT VALIDATION
            textWatcher(txtNewEmail)
            textWatcher(txtNewPhoneNumber)
            textWatcher(txtNewPassword)
            textWatcher(txtNewConfirmPassword)

            //NAVIGATION
            btnContinueCreateAccountToPart3.setOnClickListener {
                validateInputs(
                    txtNewEmail.text.toString(),
                    txtNewPhoneNumber.text.toString(),
                    txtNewPassword.text.toString(),
                    txtNewConfirmPassword.text.toString()
                )
            }

            bind.btnBackToCreateAccountPart1.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
        return bind.root
    }

    private fun validateInputs(email : String, phoneNumber : String, password : String, confirmPassword : String) {
        var errors = 0
        with(bind) {
            val txtFields = arrayOf(txtNewEmail, txtNewPhoneNumber, txtNewPassword, txtNewConfirmPassword)
            val lblErrors = arrayOf(lblCreateAccountErrorMsg1, lblCreateAccountErrorMsg2, lblCreateAccountErrorMsg3, lblCreateAccountErrorMsg4)
            txtFields.forEach { it.setBackgroundResource(R.drawable.bg_textfield_default) }
            lblErrors.forEach { it.visibility = View.GONE }

            if (!Utilities.emailPattern.matches(email)){
                txtNewEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                lblCreateAccountErrorMsg1.visibility = View.VISIBLE
                lblCreateAccountErrorMsg1.text = getString(R.string.validate_email_format)
                errors += 1
            } else {
                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                    val signInMethod = task.result?.signInMethods ?: emptyList<String>()
                    if (signInMethod.isNotEmpty()) {
                        txtNewEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                        lblCreateAccountErrorMsg1.visibility = View.VISIBLE
                        lblCreateAccountErrorMsg1.text = getString(R.string.validate_email_taken)
                        errors += 1
                    }
                }
            }

            if (!Utilities.phoneNumberPattern.matches(phoneNumber)){
                txtNewPhoneNumber.setBackgroundResource(R.drawable.bg_textfield_error)
                lblCreateAccountErrorMsg2.visibility = View.VISIBLE
                lblCreateAccountErrorMsg2.text = getString(R.string.validate_phone_format)
                errors += 1
            } else {
                //TODO: CHECK IF NUMBER IS TAKEN
            }

            if (!Utilities.passwordPattern.matches(password)){
                txtNewPassword.setBackgroundResource(R.drawable.bg_textfield_error)
                lblCreateAccountErrorMsg3.visibility = View.VISIBLE
                lblCreateAccountErrorMsg3.text = getString(R.string.validate_password_format)
                errors += 1
            }

            if (confirmPassword != password){
                txtNewConfirmPassword.setBackgroundResource(R.drawable.bg_textfield_error)
                lblCreateAccountErrorMsg4.visibility = View.VISIBLE
                lblCreateAccountErrorMsg4.text = getString(R.string.validate_password_matched)
                errors += 1
            }

            if (errors <= 0) {
                sendArguments()
            }
        }
    }

    private fun sendArguments() {
        with(bind) {
            val bundle = Bundle().apply {
                putString("gender", arguments?.getString("gender"))
                putString("firstname", arguments?.getString("firstname"))
                putString("lastname", arguments?.getString("lastname"))
                putString("birthdate", arguments?.getString("birthdate"))
                putString("email", txtNewEmail.text.toString())
                putString("phoneNumber", txtNewPhoneNumber.text.toString())
                putString("password", txtNewPassword.text.toString())
            }
            val fragment = FrgCreateAccountPart3().apply { arguments = bundle }
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in,androidx.appcompat.R.anim.abc_fade_out)
                .replace(R.id.containerCreateAccount, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    //INPUT VALIDATOR
    private fun textWatcher(textField : EditText) {
        with(bind) {
            textField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
                override fun afterTextChanged(s: Editable?) {
                    btnContinueCreateAccountToPart3.isEnabled =
                        txtNewEmail.text.isNotEmpty() and
                        txtNewPhoneNumber.text.isNotEmpty() and
                        txtNewPassword.text.isNotEmpty() and
                        txtNewConfirmPassword.text.isNotEmpty()
                }
            })
        }
    }
}
