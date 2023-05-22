package com.hygeia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hygeia.objects.Utilities.dlgInformation
import com.hygeia.objects.Utilities.greetings
import com.hygeia.databinding.FrgMainHomeBinding
import com.hygeia.objects.UserManager
import java.text.DecimalFormat

class FrgMainHome : Fragment() {
    private lateinit var bind : FrgMainHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        bind = FrgMainHomeBinding.inflate(inflater, container, false)

        val (language, greeting) = greetings.entries.random()
        val fullname = "${UserManager.firstname} ${UserManager.lastname}"
        val balance = formatNumber(UserManager.balance)

        with(bind) {
            //POPULATE
            lblGreetings.text = greeting
            lblUserFullName.text = fullname
            lblAmountBalance.text = balance

            //MAIN FUNCTIONS
            lblGreetings.setOnClickListener {
                dlgInformation(requireContext(), language).show()
            }

            //NAVIGATION
//            btnSendMoney.setOnClickListener {
//                startActivity(Intent(requireContext(), ActSendMoney::class.java))
//            }
//
//            btnRequestMoney.setOnClickListener {
//                startActivity(Intent(requireContext(), ActRequestMoney::class.java))
//            }
//
//            btnAddMoney.setOnClickListener {
//                startActivity(Intent(requireContext(), ActTopUp::class.java))
//            }
//
//            btnViewAllTransactions.setOnClickListener {
//                startActivity(Intent(requireContext(), ActTransactions::class.java))
//            }
//
//            lblViewAllTransactions.setOnClickListener {
//                btnViewAllTransactions.performClick()
//            }

            return root
        }
    }

    private fun formatNumber(number: Long?): String {
        return DecimalFormat("₱ #,###.##").format(number)
    }
}