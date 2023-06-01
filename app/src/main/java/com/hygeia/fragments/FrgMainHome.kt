package com.hygeia.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hygeia.ActSendMoney
import com.hygeia.objects.Utilities.dlgInformation
import com.hygeia.objects.Utilities.greetings
import com.hygeia.databinding.FrgMainHomeBinding
import com.hygeia.objects.UserManager
import java.text.DecimalFormat

class FrgMainHome : Fragment() {
    private lateinit var bind: FrgMainHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
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
            btnSendMoney.setOnClickListener {
                startActivity(Intent(requireContext(), ActSendMoney::class.java))
            }

            return root
        }
    }

    private fun formatNumber(balance: Any?): String {
        return if (balance.toString() == "0") {
            DecimalFormat("₱ 0.00").format(balance)
        } else if (balance.toString().contains(".")) {
            DecimalFormat("₱ #,###.##").format(balance)
        } else {
            DecimalFormat("₱ #,###.00").format(balance)
        }
    }
}