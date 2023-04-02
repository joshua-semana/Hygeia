package com.hygeia.fragments

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.R
import com.hygeia.Utilities.msg
import com.hygeia.Utilities.msgDlg

import com.hygeia.databinding.FrgCreateAccountPart3Binding

class FrgCreateAccountPart3 : Fragment() {
    private lateinit var bind : FrgCreateAccountPart3Binding
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgCreateAccountPart3Binding.inflate(inflater, container, false)
        auth = Firebase.auth

        val cloudFirestore = FirebaseFirestore.getInstance()

        val gender = arguments?.getString("gender")
        val firstname = arguments?.getString("firstname")
        val lastname = arguments?.getString("lastname")
        val fullname = "$gender $firstname $lastname"
        val birthdate = arguments?.getString("birthdate")
        val email = arguments?.getString("email")
        val phoneNumber = arguments?.getString("phoneNumber")
        val password = arguments?.getString("password")

        with(bind) {
            txtReviewFullNameAndGender.setText(fullname)
            txtReviewBirthdate.setText(birthdate)
            txtReviewEmail.setText(email)
            txtReviewPhoneNumber.setText(phoneNumber)
            txtReviewPassword.setText(password)

            //MAIN FUNCTION
            btnCreateAccount.setOnClickListener {
                // TODO: ADD LOADING DIALOG HERE
                val userData = hashMapOf(
                    "gender" to gender,
                    "firstname" to firstname,
                    "lastname" to lastname,
                    "birthdate" to birthdate,
                    "email" to email,
                    "phoneNumber" to phoneNumber,
                    "password" to password,
                    "role" to "standard"
                )
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.toString(), password.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userFirebase = task.result?.user
                            if (userFirebase != null) {
                                cloudFirestore.collection("User").document(userFirebase.uid).set(userData)
                                    .addOnSuccessListener {
                                        // IF SUCCESS, HIDE LOADING, SHOW MESSAGE, GO BACK TO LOGIN
                                        msgDlg(requireContext(), "success", "Successfully registered!", getString(R.string.dlg_success_create_account))
                                    }.addOnFailureListener{
                                        // IF NOT SUCCESS, CHECK IF THERE IS INTERNET CONNECTION
                                        // ELSE SHOW ERROR ICON, ERROR MESSAGE AND TRY AGAIN BUTTON
                                        requireActivity().msg("failed")
                                    }
                            }
                        } else {
                            // MAKE A LOCAL FUNCTION TO CHECK INTERNET CONNECTION
                            // AND SHOW THE ERROR MESSAGE TO TRY AGAIN
                            requireActivity().msg("FAILED")
                        }
                    }
            }
            //ELEMENT BEHAVIOR
            tglShowPassword.setOnCheckedChangeListener{ _, isChecked ->
                val passwordDisplay = if (isChecked) null else PasswordTransformationMethod()
                with(txtReviewPassword) {
                    this.transformationMethod = passwordDisplay
                    setSelection(text.length)
                }
            }
        }

        //NAVIGATION
        bind.btnBackToCreateAccountPart2.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        return bind.root
    }
}