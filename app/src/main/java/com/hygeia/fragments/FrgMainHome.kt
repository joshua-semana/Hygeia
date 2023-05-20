package com.hygeia.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.hygeia.*
import com.hygeia.databinding.FrgMainHomeBinding

class FrgMainHome : Fragment() {
    private lateinit var bind : FrgMainHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        bind = FrgMainHomeBinding.inflate(inflater, container, false)

        with(bind) {

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
}