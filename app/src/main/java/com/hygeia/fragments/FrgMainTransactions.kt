package com.hygeia.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hygeia.adapters.ArrAdpTransactions
import com.hygeia.classes.DataTransactions
import com.hygeia.databinding.FrgMainTransactionsBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.dlgTransactionDetails
import com.hygeia.objects.Utilities.isInternetConnected

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
            if (isInternetConnected(requireContext())){
                //POPULATE
                getListOfTransactions()
            }else {
                dlgStatus(requireContext(),"no internet").show()
            }
            return root
        }
    }

    private fun getListOfTransactions() {
        loading.show()
        bind.listViewTransactionHistory.layoutManager = LinearLayoutManager(requireContext())
        listOfTransactions = arrayListOf()

        val query = transactionRef.whereEqualTo("User Reference", UserManager.uid).orderBy("Date Created", Query.Direction.DESCENDING)

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
                        ArrAdpTransactions(listOfTransactions, this@FrgMainTransactions)
                }
                loading.dismiss()
            }
        }
    }

    override fun onTransactionItemClick(ID: String) {
        dlgTransactionDetails(requireContext(), ID).show()
    }

}