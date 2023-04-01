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
import com.hygeia.Utilities.msg

import com.hygeia.databinding.FrgCreateAccountPart3Binding

class FrgCreateAccountPart3 : Fragment() {
    private lateinit var bind : FrgCreateAccountPart3Binding
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgCreateAccountPart3Binding.inflate(inflater, container, false)
        auth = Firebase.auth

        val cloudFirestore = FirebaseFirestore.getInstance()

        val args = requireArguments()
        val gender = args.getString("gender")
        val firstname = args.getString("firstname")
        val lastname = args.getString("lastname")
        val fullname = "$gender $firstname $lastname"
        val birthdate = args.getString("birthdate")
        val email = args.getString("email")
        val phoneNumber = args.getString("phoneNumber")
        val password = args.getString("password")

        with(bind) {
            txtReviewFullNameAndGender.setText(fullname)
            txtReviewBirthdate.setText(birthdate)
            txtReviewEmail.setText(email)
            txtReviewPhoneNumber.setText(phoneNumber)
            txtReviewPassword.setText(password)

            //MAIN FUNCTION
            btnCreateAccount.setOnClickListener {
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
                try {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.toString(), password.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userFirebase = task.result?.user
                                if (userFirebase != null) {
                                    cloudFirestore.collection("User").document(userFirebase.uid).set(userData)
                                        .addOnSuccessListener {
                                            requireActivity().msg("Successfully registered")
                                        }.addOnFailureListener{
                                            requireActivity().msg("failed")
                                        }
                                }
                            }
                        }
                } catch (_: java.lang.Exception){

                }
            }
            //ELEMENT BEHAVIOR
            tglShowPassword.setOnCheckedChangeListener{ _, isChecked ->
                if (isChecked) {
                    txtReviewPassword.transformationMethod = null
                } else {
                    txtReviewPassword.transformationMethod = PasswordTransformationMethod()
                }
            }
        }

        //NAVIGATION
        bind.btnBackToCreateAccountPart2.setOnClickListener {
            activity?.onBackPressed()
        }

        return bind.root
    }
}