package com.hygeia

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.classes.ButtonType
import com.hygeia.databinding.ActChangePasswordBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.dlgLoading
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActChangePassword : AppCompatActivity() {
    private lateinit var bind : ActChangePasswordBinding
    private lateinit var auth: FirebaseAuth

    private val db = FirebaseFirestore.getInstance()
    private val userRef = db.collection("User")
    private lateinit var loading : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActChangePasswordBinding.inflate(layoutInflater)
        auth = Firebase.auth
        loading = dlgLoading(this@ActChangePassword)
        setContentView(bind.root)

        with(bind) {
            btnUpdatePassword.setOnClickListener {
                if (isInternetConnected(this@ActChangePassword)){
                    if (inputsAreNotEmpty()){
                        loading.show()
                        clearTextError(txtLayoutOldPassword,txtLayoutPassword,txtLayoutConfirmPassword)
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (inputsAreCorrect()){
                                updatePassword()
                            }
                            loading.dismiss()
                        }
                    } else {
                        dlgStatus(this@ActChangePassword,"empty field").show()
                        Utilities.showRequiredTextField(
                            txtOldPassword to txtLayoutOldPassword,
                            txtPassword to txtLayoutPassword,
                            txtConfirmPassword to txtLayoutConfirmPassword,
                        )
                    }
                } else {
                    dlgStatus(this@ActChangePassword,"no internet")
                }
            }
            mainLayout.setOnClickListener {
                getSystemService(InputMethodManager::class.java).apply {
                    hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                }
                mainLayout.requestFocus()
                currentFocus?.clearFocus()
            }
            //NAVIGATION
            btnBack.setOnClickListener {
                onBackBtnPressed()
            }
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackBtnPressed()
                }
            })
        }
    }

    private fun onBackBtnPressed(){
        Utilities.dlgConfirmation(this, "going back") {
            if (it == ButtonType.PRIMARY) {
                this.finish()
            }
        }.show()
    }
    private fun inputsAreNotEmpty(): Boolean {
        return when{
            bind.txtOldPassword.text!!.isEmpty() -> false
            bind.txtPassword.text!!.isEmpty() -> false
            bind.txtConfirmPassword.text!!.isEmpty() -> false
            else -> true
        }
    }
    private suspend fun inputsAreCorrect(): Boolean {
        var inputErrorCount = 0
        with(bind) {
            if (txtOldPassword.text.toString() != UserManager.password) {
                inputErrorCount++
                txtLayoutOldPassword.error = getString(R.string.error_old_password_matched)
            }

            if (!txtPassword.text.toString().matches(Utilities.passwordPattern)) {
                inputErrorCount++
                txtLayoutPassword.error = getString(R.string.error_password_format)
            }

            if (txtConfirmPassword.text.toString() != txtPassword.text.toString()) {
                inputErrorCount++
                txtLayoutConfirmPassword.error = getString(R.string.error_password_matched)
            }
        }
        return inputErrorCount == 0
    }
    private fun updatePassword() {
        loading.show()
        auth.signInWithEmailAndPassword(UserManager.email.toString(), bind.txtOldPassword.text.toString()).addOnSuccessListener {
            it.user!!.updatePassword(bind.txtPassword.text.toString())
            userRef.document(it.user!!.uid)
                .update("password", bind.txtPassword.text.toString())
                .addOnSuccessListener {
                    loading.dismiss()
                    dlgStatus(this, "success update password").apply {
                        setOnDismissListener {
                            this@ActChangePassword.finish()
                        }
                        show()
                    }
                }
        }
    }

}