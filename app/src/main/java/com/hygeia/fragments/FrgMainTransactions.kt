package com.hygeia.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hygeia.R
import com.hygeia.adapters.ArrAdpTransactions
import com.hygeia.classes.DataTransactions
import com.hygeia.databinding.FrgMainTransactionsBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgTransactionDetails

class FrgMainTransactions : Fragment(), ArrAdpTransactions.OnTransactionItemClickListener {

    private lateinit var bind: FrgMainTransactionsBinding
    private lateinit var listOfTransactions: ArrayList<DataTransactions>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var transactionRef = db.collection("Transactions")
    private val query = transactionRef.whereEqualTo("User Reference", UserManager.uid)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgMainTransactionsBinding.inflate(inflater, container, false)
        loading = Utilities.dlgLoading(requireContext())

        listOfTransactions = arrayListOf()

        constraintViews()

        with(bind) {

            chipAll.isChecked = true
            if (chipAll.isChecked) getListOfTransactions(query)

            chipAll.setOnClickListener {
                getListOfTransactions(query)
            }

            @Suppress("DEPRECATION")
            chipGroup.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId) {
                    R.id.chipSend -> {
                        getListOfTransactions(query.whereEqualTo("Type", "Send Money"))
                    }
                    R.id.chipReceive -> {
                        getListOfTransactions(query.whereEqualTo("Type", "Receive Money"))
                    }
                    R.id.chipPurchase -> {
                        getListOfTransactions(query.whereEqualTo("Type", "Purchase"))
                    }
                    R.id.chipStars -> {
                        getListOfTransactions(query.whereEqualTo("Type", "Purchase Using Star"))
                    }
                    R.id.chipRequest -> {
                        getListOfTransactions(query.whereEqualTo("Type", "Request"))
                    }
                }
            }
            return root
        }
    }

    override fun onStop() {
        super.onStop()
        bind.chipGroup.clearCheck()
    }

    private fun constraintViews() {
        with(bind) {
            when (UserManager.role) {
                "super admin" -> {
                    chipPurchase.visibility = View.GONE
                    chipStars.visibility = View.GONE
                    chipReceive.visibility = View.GONE
                    chipPurchase.visibility = View.GONE
                    chipRequest.visibility = View.VISIBLE
                }
                "admin" -> {
                    chipRequest.visibility = View.GONE
                }
                "standard" -> {
                    chipRequest.visibility = View.GONE
                }
            }
        }
    }

    private fun getListOfTransactions(query: Query) {
        listOfTransactions.clear()
        loading.show()
        bind.listViewTransactionHistory.layoutManager = LinearLayoutManager(requireContext())

        query.orderBy("Date Created", Query.Direction.DESCENDING).get().apply {
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
                } else {
                    bind.coverTransaction.visibility = View.VISIBLE
                }
                loading.dismiss()
            }
        }
    }

    override fun onTransactionItemClick(ID: String) {
        dlgTransactionDetails(requireContext(), ID).show()
    }

}