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
import com.hygeia.Utilities.dlgMessage
import com.hygeia.Utilities.dlgNoInternet
import com.hygeia.Utilities.isInternetConnected

import com.hygeia.databinding.FrgCreateAccountPart3Binding

class FrgCreateAccountPart3 : Fragment() {
    private lateinit var bind: FrgCreateAccountPart3Binding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
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
            txtFirstName.setText(userInfo["firstname"])
            txtLastName.setText(userInfo["lastname"])
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
                    dlgNoInternet(requireContext()).show()
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
            "role" to "standard",
            "balance" to 0
        )

        auth.createUserWithEmailAndPassword(data["email"].toString(), data["password"].toString())
            .addOnSuccessListener { getUser ->
                db.collection("User").document(getUser.user!!.uid).set(userData)
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
    }
}