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
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.msg
import com.hygeia.Utilities.dlgMessage
import com.hygeia.Utilities.isInternetConnected

import com.hygeia.databinding.FrgCreateAccountPart3Binding

class FrgCreateAccountPart3 : Fragment() {
    private lateinit var bind : FrgCreateAccountPart3Binding
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FrgCreateAccountPart3Binding.inflate(inflater, container, false)
        auth = Firebase.auth

        val userInfo = mapOf(
            "gender" to arguments?.getString("gender"),
            "firstname" to arguments?.getString("firstname"),
            "lastname" to arguments?.getString("lastname"),
            "birthdate" to arguments?.getString("birthdate"),
            "email" to arguments?.getString("email"),
            "phoneNumber" to arguments?.getString("phoneNumber"),
            "password" to arguments?.getString("password")
        )

        val fullname = "${userInfo["gender"]} ${userInfo["firstname"]} ${userInfo["lastname"]}"

        with(bind) {
            //POPULATE
            txtReviewFullNameAndGender.setText(fullname)
            txtReviewBirthdate.setText(userInfo["birthdate"])
            txtReviewEmail.setText(userInfo["email"])
            txtReviewPhoneNumber.setText(userInfo["phoneNumber"])
            txtReviewPassword.setText(userInfo["password"])

            //MAIN FUNCTION
            btnCreateAccount.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    createAccount(userInfo)
                } else {
                    dlgMessage(
                        requireContext(),
                        "no-wifi",
                        getString(R.string.dlg_title_wifi),
                        getString(R.string.dlg_body_wifi),
                        "Okay"
                    )
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
            //NAVIGATION
            btnBackToCreateAccountPart2.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            return root
        }
    }

    private fun createAccount(data : Map<String, String?>) {
        val loading = dlgLoading(requireContext())
        val cloudFirestore = FirebaseFirestore.getInstance()
        loading.show()

        val userData = hashMapOf(
            "gender" to data["gender"],
            "firstname" to data["firstname"],
            "lastname" to data["lastname"],
            "birthdate" to data["birthdate"],
            "email" to data["email"],
            "phoneNumber" to data["phoneNumber"],
            "password" to data["password"],
            "role" to "standard"
        )

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(data["email"].toString(), data["password"].toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userFirebase = task.result?.user
                    if (userFirebase != null) {
                        cloudFirestore.collection("User").document(userFirebase.uid).set(userData)
                            .addOnSuccessListener {
                                loading.dismiss()
                                dlgMessage(
                                    requireContext(),
                                    "success",
                                    getString(R.string.dlg_title_create_account),
                                    getString(R.string.dlg_body_create_account),
                                    "Go back to login"
                                ).apply {
                                    setOnDismissListener {
                                        requireActivity().finish()
                                    }
                                    show()
                                }
                            }
                    }
                } else {
                    loading.dismiss()
                    requireActivity().msg("Please try again")
                }
            }

    }
}