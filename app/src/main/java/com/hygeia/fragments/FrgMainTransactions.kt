package com.hygeia.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.adapters.ArrAdpTransactions
import com.hygeia.classes.DataTransactions
import com.hygeia.databinding.FrgMainTransactionsBinding
import com.hygeia.objects.Utilities

class FrgMainTransactions : Fragment(), ArrAdpTransactions.OnTransactionItemClickListener {

    private lateinit var bind: FrgMainTransactionsBinding
    private lateinit var listOfTransactions: ArrayList<DataTransactions>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var transactionRef = db.collection("Transactions")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgMainTransactionsBinding.inflate(inflater, container, false)
        loading = Utilities.dlgLoading(requireContext())
        with(bind) {

            //POPULATE
            getListOfTransactions()

            return root
        }
    }

    private fun getListOfTransactions() {
        loading.show()
        bind.listViewTransactionHistory.layoutManager = LinearLayoutManager(requireContext())
        listOfTransactions = arrayListOf()

        transactionRef.get().apply {
            addOnSuccessListener { data ->
                for (item in data.documents) {
                    val transactionId = item.id
                    val transactionAmount = item.get("Amount")
                    val transactionDate = item.get("Date Created")
                    val transactionNumber = item.get("Number")
                    val transactionReference = item.get("Reference Number")
                    val transactionType = item.get("Type")

                    val transaction = DataTransactions(
                        transactionId,
                        transactionAmount,
                        transactionDate.toString(),
                        transactionNumber.toString(),
                        transactionReference.toString(),
                        transactionType.toString()
                    )

                    listOfTransactions.add(transaction)
                }
                bind.listViewTransactionHistory.adapter = ArrAdpTransactions(listOfTransactions, this@FrgMainTransactions)
                loading.dismiss()
            }
        }
    }

    override fun onTransactionItemClick(ID: String) {
        TODO("Not yet implemented")
    }

}