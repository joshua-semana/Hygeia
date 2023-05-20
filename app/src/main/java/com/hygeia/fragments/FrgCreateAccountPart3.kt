package com.hygeia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.hygeia.Utilities.dlgStatus
import com.hygeia.Utilities.dlgLoading
import com.hygeia.Utilities.isInternetConnected

import com.hygeia.databinding.FrgCreateAccountPart3Binding
import java.util.Date

class FrgCreateAccountPart3 : Fragment() {
    private lateinit var bind: FrgCreateAccountPart3Binding
    private lateinit var auth: FirebaseAuth

    private val db = FirebaseFirestore.getInstance()
    private val userRef = db.collection("User")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgCreateAccountPart3Binding.inflate(inflater, container, false)
        auth = Firebase.auth

        val userInfo = mapOf(
            "firstname" to arguments?.getString("firstname"),
            "lastname" to arguments?.getString("lastname"),
            "gender" to arguments?.getString("gender"),
            "birthdate" to arguments?.getString("birthdate"),
            "email" to arguments?.getString("email"),
            "phoneNumber" to arguments?.getString("phoneNumber"),
            "password" to arguments?.getString("password")
        )

        with(bind) {
            //POPULATE
            val fullname = "${userInfo["firstname"]} ${userInfo["lastname"]}"
            txtFullName.setText(fullname)
            txtGender.setText(userInfo["gender"])
            txtBirthDate.setText(userInfo["birthdate"])
            txtEmail.setText(userInfo["email"])
            txtPhoneNumber.setText(userInfo["phoneNumber"])
            txtPassword.setText(userInfo["password"])

            //MAIN FUNCTION
            btnCreateAccount.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    createAccount(userInfo)
                } else {
                    dlgStatus(requireContext(), "no internet").show()
                }
            }
            //NAVIGATION
            btnBack.setOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }

            return root
        }
    }

    private fun createAccount(data: Map<String, String?>) {
        val loading = dlgLoading(requireContext())
        loading.show()

        val userData = hashMapOf(
            "gender" to data["gender"],
            "firstname" to data["firstname"],
            "lastname" to data["lastname"],
            "birthdate" to data["birthdate"],
            "email" to data["email"],
            "phoneNumber" to data["phoneNumber"],
            "password" to data["password"],
            "status" to "active",
            "role" to "standard",
            "balance" to 0,
            "dateCreated" to Timestamp(Date())
        )

        auth.createUserWithEmailAndPassword(data["email"].toString(), data["password"].toString())
            .addOnSuccessListener {
                loading.dismiss()
                userRef.document(it.user!!.uid).set(userData)
                dlgStatus(requireContext(), "success create account").apply {
                    setOnDismissListener {
                        requireActivity().finish()
                    }
                    show()
                }
            }
    }
}