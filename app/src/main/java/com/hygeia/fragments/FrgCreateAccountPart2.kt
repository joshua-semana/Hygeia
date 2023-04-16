package com.hygeia.fragments

import android.app.Dialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.dlgMessage
import com.hygeia.Utilities.emailPattern
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.Utilities.passwordPattern
import com.hygeia.Utilities.phoneNumberPattern
import com.hygeia.databinding.FrgCreateAccountPart2Binding
import java.util.concurrent.CompletableFuture

class FrgCreateAccountPart2 : Fragment() {
    private lateinit var bind : FrgCreateAccountPart2Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var loading : Dialog
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgCreateAccountPart2Binding.inflate(inflater, container, false)
        auth = Firebase.auth
        loading = dlgLoading(requireContext())

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
                if (isInternetConnected(requireContext())) {
                    loading.show()
                    val isEmailValid = validateEmail(txtNewEmail.text.toString())
                    val isPhoneNumberValid = validatePhoneNumber(txtNewPhoneNumber.text.toString())
                    val isPasswordValid = validatePassword(txtNewPassword.text.toString())
                    val isConfirmPasswordValid = validateConfirmPassword(
                        txtNewConfirmPassword.text.toString(),
                        txtNewPassword.text.toString()
                    )
                    val futures = arrayOf(
                        isEmailValid,
                        isPhoneNumberValid,
                        isPasswordValid,
                        isConfirmPasswordValid
                    )
                    CompletableFuture.allOf(*futures).thenRunAsync {
                        if (futures.all { it.get() }) {
                            loading.dismiss()
                            sendArguments()
                        } else {
                            loading.dismiss()
                        }
                    }
                } else {
                    dlgMessage(
                        requireContext(),
                        "no-wifi",
                        getString(R.string.dlg_title_wifi),
                        getString(R.string.dlg_body_wifi),
                        "Okay"
                    ).show()
                }
            }

            btnBack.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            return root
        }
    }

    private fun validateEmail(email : String) : CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        with(bind) {
            txtNewEmail.setBackgroundResource(R.drawable.bg_textfield_default)
            lblCreateAccountErrorMsg1.visibility = View.GONE
            if (email.matches(emailPattern)) {
                auth.fetchSignInMethodsForEmail(email).addOnSuccessListener { getEmailList ->
                    if (getEmailList.signInMethods?.isEmpty() == true) {
                        future.complete(true)
                    } else {
                        txtNewEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                        lblCreateAccountErrorMsg1.visibility = View.VISIBLE
                        lblCreateAccountErrorMsg1.text =getString(R.string.validate_email_taken)
                        future.complete(false)
                    }
                }
            } else {
                txtNewEmail.setBackgroundResource(R.drawable.bg_textfield_error)
                lblCreateAccountErrorMsg1.visibility = View.VISIBLE
                lblCreateAccountErrorMsg1.text = getString(R.string.validate_email_format)
                future.complete(false)
            }
        }
        return future
    }

    private fun validatePhoneNumber(phoneNumber: String) : CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        with(bind) {
            txtNewPhoneNumber.setBackgroundResource(R.drawable.bg_textfield_default)
            lblCreateAccountErrorMsg2.visibility = View.GONE
            if (phoneNumber.matches(phoneNumberPattern)) {
                FirebaseFirestore.getInstance().collection("User")
                    .whereEqualTo("phoneNumber", phoneNumber).get()
                        .addOnSuccessListener { getPhoneNumbers ->
                            if (getPhoneNumbers.isEmpty){
                                future.complete(true)
                            } else {
                                txtNewPhoneNumber.setBackgroundResource(R.drawable.bg_textfield_error)
                                lblCreateAccountErrorMsg2.visibility = View.VISIBLE
                                lblCreateAccountErrorMsg2.text = getString(R.string.validate_phone_taken)
                                future.complete(false)
                            }
                        }
            } else {
                txtNewPhoneNumber.setBackgroundResource(R.drawable.bg_textfield_error)
                lblCreateAccountErrorMsg2.visibility = View.VISIBLE
                lblCreateAccountErrorMsg2.text = getString(R.string.validate_phone_format)
                future.complete(false)
            }
        }
        return future
    }

    private fun validatePassword(password : String) : CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        with(bind) {
            txtNewPassword.setBackgroundResource(R.drawable.bg_textfield_default)
            lblCreateAccountErrorMsg3.visibility = View.GONE
            if (password.matches(passwordPattern)) {
                future.complete(true)
            } else {
                txtNewPassword.setBackgroundResource(R.drawable.bg_textfield_error)
                lblCreateAccountErrorMsg3.visibility = View.VISIBLE
                lblCreateAccountErrorMsg3.text = getString(R.string.validate_password_format)
                future.complete(false)
            }
        }
        return future
    }

    private fun validateConfirmPassword(confirmPassword: String, password: String) : CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        with(bind) {
            txtNewConfirmPassword.setBackgroundResource(R.drawable.bg_textfield_default)
            lblCreateAccountErrorMsg4.visibility = View.GONE
            if (confirmPassword == password) {
                future.complete(true)
            } else {
                txtNewConfirmPassword.setBackgroundResource(R.drawable.bg_textfield_error)
                lblCreateAccountErrorMsg4.visibility = View.VISIBLE
                lblCreateAccountErrorMsg4.text = getString(R.string.validate_password_matched)
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
                    btnContinueCreateAccountToPart3.isEnabled = listOf(
                        txtNewEmail,
                        txtNewPhoneNumber,
                        txtNewPassword,
                        txtNewConfirmPassword
                    ).all { it.text.isNotEmpty() }
                }
            })
        }
    }
}
