package com.hygeia.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.ActChangePassword
import com.hygeia.ActUserInfo
import com.hygeia.ActWalletBackgrounds
import com.hygeia.classes.ButtonType
import com.hygeia.databinding.FrgMainSettingsBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgInformation
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected

class FrgMainSettings : Fragment() {
    private lateinit var bind: FrgMainSettingsBinding
    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgMainSettingsBinding.inflate(inflater, container, false)

        val fullname = "${UserManager.firstname} ${UserManager.lastname}"
        val phonenumber = UserManager.phoneNumber

        with(bind) {
            lblUserFullName.text = fullname
            lblPhoneNumber.text = phonenumber

            btnViewMyInfo.setOnClickListener {
                startActivity(Intent(requireContext(), ActUserInfo::class.java))
            }
            btnChangePassword.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    startActivity(Intent(requireContext(), ActChangePassword::class.java))
                } else {
                    dlgStatus(requireContext(), "no internet").show()
                }
            }

            btnAboutUs.setOnClickListener {
                dlgInformation(requireContext(), "about us").show()
            }

            btnChangeWalletBackground.setOnClickListener {
                startActivity(Intent(requireContext(), ActWalletBackgrounds::class.java))
            }

            btnLogOut.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    Utilities.dlgConfirmation(requireContext(), "log out") {
                        if (it == ButtonType.PRIMARY) {
                            userRef.document(UserManager.uid!!).update("isOnline", false)
                                .addOnSuccessListener {
                                    requireActivity().finish()
                                }
                        }
                    }.show()
                } else {
                    dlgStatus(requireContext(), "no internet").show()
                }
            }

            return root
        }
    }

}