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
import com.hygeia.Utilities.isInternetConnected
import com.hygeia.Utilities.msg
import com.hygeia.Utilities.passwordPattern
import com.hygeia.databinding.FrgResetPasswordBinding
import java.util.concurrent.CompletableFuture

class FrgResetPassword : Fragment() {
    private lateinit var bind : FrgResetPasswordBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var loading : Dialog
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgResetPasswordBinding.inflate(inflater, container, false)
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

            btnUpdatePassword.setOnClickListener {
                if (isInternetConnected(requireContext())){
                    loading.show()
                    val isNewPasswordValid = validateNewPassword(txtResetPassword.text.toString())
                    val isConfirmPasswordValid = validateConfirmPassword(
                        txtResetConfirmPassword.text.toString(),
                        txtResetPassword.text.toString())

                    val future = arrayOf(
                        isNewPasswordValid,
                        isConfirmPasswordValid
                    )
                    CompletableFuture.allOf(*future).thenRunAsync{
                        if (future.all { it.get() }){
                            updatingNewPassword(txtResetPassword.text.toString())
                            loading.dismiss()
                        }else {
                            loading.dismiss()
                        }
                    }
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
    private fun updatingNewPassword(newPassword : String){
        val email = arguments?.getString("email").toString()
        val password = arguments?.getString("password").toString()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = FirebaseAuth.getInstance().currentUser
                val uid = user?.uid
                it.user?.updatePassword(newPassword)
                    ?.addOnSuccessListener {
                        if (uid != null) {
                            FirebaseFirestore.getInstance()
                                .collection("User")
                                .document(uid)
                                .update("password", newPassword)
                                .addOnSuccessListener {
                                    requireActivity().msg("Success")
                                }
                        }
                    }
                    ?.addOnFailureListener { e ->
                        requireActivity().msg("Error updating password: ${e.message}")
                    }
            }
    }
    private fun validateNewPassword(password : String) : CompletableFuture<Boolean>{
        val future = CompletableFuture<Boolean>()
        with(bind){
            txtResetPassword.setBackgroundResource(R.drawable.bg_textfield_default)
            lblResetPasswordErrorMsg1.visibility = View.GONE
            if (password.matches(passwordPattern)){
                future.complete(true)
            } else {
                txtResetPassword.setBackgroundResource(R.drawable.bg_textfield_error)
                lblResetPasswordErrorMsg1.visibility = View.VISIBLE
                lblResetPasswordErrorMsg1.text = getString(R.string.validate_password_criteria)
                future.complete(false)
            }
        }
        return future
    }
    private fun validateConfirmPassword(confirmPassword : String,password : String) : CompletableFuture<Boolean>{
        val future = CompletableFuture<Boolean>()
        with(bind){
            txtResetConfirmPassword.setBackgroundResource(R.drawable.bg_textfield_default)
            lblCreateAccountErrorMsg2.visibility = View.GONE
            if (confirmPassword == password){
                future.complete(true)
            } else {
                txtResetConfirmPassword.setBackgroundResource(R.drawable.bg_textfield_error)
                lblCreateAccountErrorMsg2.visibility = View.VISIBLE
                lblCreateAccountErrorMsg2.text = getString(R.string.validate_password_matched)
                future.complete(false)
            }
        }
        return future
    }
    //INPUT VALIDATOR
    private fun textWatcher(textField : EditText) {
        textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                with(bind) {
                    btnUpdatePassword.isEnabled = listOf(
                        txtResetPassword,
                        txtResetConfirmPassword
                    ).all { it.text.isNotEmpty() }
                }
            }
        })
    }
}

