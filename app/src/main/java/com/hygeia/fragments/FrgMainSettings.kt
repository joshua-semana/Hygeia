package com.hygeia.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hygeia.ActChangePassword
import com.hygeia.ActUserInfo
import com.hygeia.classes.ButtonType
import com.hygeia.databinding.ActUserInfoBinding
import com.hygeia.databinding.FrgMainSettingsBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities

class FrgMainSettings : Fragment() {
    private lateinit var bind: FrgMainSettingsBinding

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
                startActivity(Intent(requireContext(), ActChangePassword::class.java))
            }

            btnLogOut.setOnClickListener {
                Utilities.dlgConfirmation(requireContext(), "log out") {
                    if (it == ButtonType.PRIMARY) {
                        requireActivity().finish()
                    }
                }.show()
            }

            return root
        }
    }

}