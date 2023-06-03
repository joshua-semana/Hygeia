package com.hygeia.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.ActPurchase
import com.hygeia.ActQrCodeScanner
import com.hygeia.ActSendMoney
import com.hygeia.classes.ButtonType
import com.hygeia.objects.Utilities.dlgInformation
import com.hygeia.objects.Utilities.greetings
import com.hygeia.databinding.FrgMainHomeBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.formatNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FrgMainHome : Fragment() {
    private lateinit var bind: FrgMainHomeBinding
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        loading = Utilities.dlgLoading(requireContext())
        bind = FrgMainHomeBinding.inflate(inflater, container, false)
        val (language, greeting) = greetings.entries.random()
        with(bind) {
            populateMainHome()
            lblGreetings.text = greeting
            //MAIN FUNCTIONS
            lblGreetings.setOnClickListener {
                dlgInformation(requireContext(), language).show()
            }

            cardWallet.setOnClickListener {
                loading.show()
                lifecycleScope.launch(Dispatchers.Main) {
                    getWalletBalance()
                }
            }

            //NAVIGATION
            btnSendMoney.setOnClickListener {
                startActivity(Intent(requireContext(), ActSendMoney::class.java))
            }

            btnPurchase.setOnClickListener {
                startActivity(Intent(requireContext(), ActQrCodeScanner::class.java))
            }

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        onBackPressed()
                    }
                })

            return root
        }
    }

    private suspend fun getWalletBalance() {
        val query = userRef.document(UserManager.uid!!).get().await()
        val balance = formatNumber(query.get("balance"))
        bind.lblAmountBalance.text = balance
        loading.dismiss()
    }

    private fun populateMainHome() {
        val fullname = "${UserManager.firstname} ${UserManager.lastname}"
        val balance = formatNumber(UserManager.balance)

        bind.lblUserFullName.text = fullname
        bind.lblAmountBalance.text = balance
    }

    private fun onBackPressed() {
        dlgConfirmation(requireContext(), "log out") {
            if (it == ButtonType.PRIMARY) {
                requireActivity().finish()
            }
        }.show()
    }
}