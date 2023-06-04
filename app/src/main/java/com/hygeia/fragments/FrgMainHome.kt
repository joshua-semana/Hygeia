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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hygeia.ActMain
import com.hygeia.ActPurchase
import com.hygeia.ActQrCodeScanner
import com.hygeia.ActSendMoney
import com.hygeia.adapters.ArrAdpTransactions
import com.hygeia.classes.ButtonType
import com.hygeia.classes.DataTransactions
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

class FrgMainHome : Fragment(), ArrAdpTransactions.OnTransactionItemClickListener {
    private lateinit var bind: FrgMainHomeBinding
    private lateinit var listOfTransactions: ArrayList<DataTransactions>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")
    private var transactionRef = db.collection("Transactions")


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
                getListOfTransactions()
            }

            //POPULATE
            getListOfTransactions()

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

    private fun getListOfTransactions() {
        loading.show()
        bind.listViewTransactionHistory.layoutManager = LinearLayoutManager(requireContext())
        listOfTransactions = arrayListOf()

        val query = transactionRef.whereEqualTo("User Reference", UserManager.uid).limit(3).orderBy("Date Created", Query.Direction.DESCENDING)

        query.get().apply {
            addOnSuccessListener { data ->
                if (!data.isEmpty) {
                    bind.coverTransaction.visibility = View.GONE
                    for (item in data.documents) {
                        val transactionId = item.id
                        val transactionAmount = item.get("Amount")
                        val transactionDate: Timestamp = item.get("Date Created") as Timestamp
                        val transactionNumber = item.get("Number")
                        val transactionReference = item.get("Reference Number")
                        val transactionType = item.get("Type")

                        val transaction = DataTransactions(
                            transactionId,
                            transactionAmount,
                            transactionDate,
                            transactionNumber.toString(),
                            transactionReference.toString(),
                            transactionType.toString()
                        )

                        listOfTransactions.add(transaction)
                    }
                    bind.listViewTransactionHistory.adapter =
                        ArrAdpTransactions(listOfTransactions, this@FrgMainHome)
                }
                loading.dismiss()
            }
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
                userRef.document(UserManager.uid!!).update("status", "inactive")
                    .addOnSuccessListener {
                        requireActivity().finish()
                    }
            }
        }.show()
    }

    override fun onTransactionItemClick(ID: String) {
        Utilities.dlgTransactionDetails(requireContext(), ID).show()
    }
}