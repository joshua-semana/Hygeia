package com.hygeia.fragments

import android.annotation.SuppressLint
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
import com.hygeia.objects.Utilities.dlgTransactionDetails
import com.hygeia.objects.Utilities.msg

class FrgMainTransactions : Fragment(), ArrAdpTransactions.OnTransactionItemClickListener {

    private lateinit var bind: FrgMainTransactionsBinding
    private lateinit var listOfTransactions: ArrayList<DataTransactions>

    private var db = FirebaseFirestore.getInstance()
    private var transactionRef = db.collection("Transactions")
    private val query = transactionRef.whereEqualTo("User Reference", UserManager.uid)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgMainTransactionsBinding.inflate(inflater, container, false)

        constraintViews()

        with(bind) {

            chipAll.isChecked = true
            if (chipAll.isChecked) getListOfTransactions(query)

            chipAll.setOnClickListener {
                getListOfTransactions(query)
            }

            @Suppress("DEPRECATION")
            chipGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
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
        }
        return bind.root
    }

    private fun constraintViews() {
        when (UserManager.role) {
            "super admin" -> {
                bind.chipPurchase.visibility = View.GONE
                bind.chipStars.visibility = View.GONE
                bind.chipReceive.visibility = View.GONE
                bind.chipPurchase.visibility = View.GONE
                bind.chipRequest.visibility = View.VISIBLE
            }

            "admin" -> {
                bind.chipRequest.visibility = View.GONE
            }

            "standard" -> {
                bind.chipRequest.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfTransactions(query: Query) {
        bind.coverTransaction.visibility = View.VISIBLE

        bind.lblMessage.text = "Fetching your transaction information, please wait..."
        bind.loading.visibility = View.VISIBLE
        bind.listViewTransactionHistory.layoutManager = LinearLayoutManager(requireContext())
        listOfTransactions = arrayListOf()

        query.orderBy("Date Created", Query.Direction.DESCENDING).get().apply {
            addOnSuccessListener { data ->
                listOfTransactions.clear()
                if (!data.isEmpty) {
                    bind.coverTransaction.visibility = View.GONE
                    for (item in data.documents) {
                        val items = DataTransactions(
                            item.id,
                            item.get("Amount"),
                            item.get("Date Created") as Timestamp,
                            item.get("Number").toString(),
                            item.get("Reference Number").toString(),
                            item.get("Type").toString()
                        )
                        listOfTransactions.add(items)
                    }
                    bind.listViewTransactionHistory.adapter = ArrAdpTransactions(
                        listOfTransactions, this@FrgMainTransactions
                    )
                } else {
                    bind.lblMessage.text =
                        "You currently don't have any transactions.\nTry using our services and you can get rewarded."
                    bind.loading.visibility = View.GONE
                }
            }
            addOnFailureListener {
                requireContext().msg("Please try again.")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        bind.chipGroup.clearCheck()
    }

    override fun onTransactionItemClick(ID: String) {
        dlgTransactionDetails(requireContext(), ID).show()
    }
}