package com.hygeia.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.hygeia.ActQrCodeScanner
import com.hygeia.ActRequestMoney
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
import com.hygeia.objects.Utilities.dlgMyQrCode
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.formatCredits
import com.hygeia.objects.Utilities.formatPoints
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.objects.Utilities.msg

class FrgMainHome : Fragment(), ArrAdpTransactions.OnTransactionItemClickListener {
    private lateinit var bind: FrgMainHomeBinding
    private lateinit var listOfTransactions: ArrayList<DataTransactions>

    private var db = FirebaseFirestore.getInstance()
    private var userRef = db.collection("User")
    private var transactionRef = db.collection("Transactions")

    private var isVisible = false

    @SuppressLint("DiscouragedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = FrgMainHomeBinding.inflate(inflater, container, false)

        val (language, greeting) = greetings.entries.random()

        val resourceId = resources.getIdentifier(
            UserManager.walletBackground, "drawable", requireContext().packageName
        )

        var prevBalance: Any? = null
        userRef.document(UserManager.uid!!).addSnapshotListener { snapshot, exception ->
            if (exception != null) return@addSnapshotListener
            if (prevBalance != null && snapshot?.get("balance") != prevBalance && isVisible) {
                populateMainHome()
            }
            prevBalance = snapshot?.get("balance")
        }

        constraintViews()
        populateMainHome()
        getListOfTransactions()

        with(bind) {
            lblGreetings.text = greeting
            cardWalletBackground.setBackgroundResource(resourceId)

            cardWallet.setOnClickListener {
                getListOfTransactions()
            }

            lblGreetings.setOnClickListener {
                dlgInformation(requireContext(), language).show()
            }

            cardPoints.setOnClickListener {
                dlgInformation(requireContext(), "introduce stars").show()
            }

            btnMyQr.setOnClickListener {
                dlgMyQrCode(requireContext(), UserManager.phoneNumber.toString()).show()
            }

            btnSendMoney.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    UserManager.isOnAnotherActivity = true
                    startActivity(Intent(requireContext(), ActSendMoney::class.java))
                } else {
                    dlgStatus(requireContext(), "no internet").show()
                }
            }

            btnRequestMoney.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    UserManager.isOnAnotherActivity = true
                    startActivity(Intent(requireContext(), ActRequestMoney::class.java))
                } else {
                    dlgStatus(requireContext(), "no internet").show()
                }
            }

            btnPurchase.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    if (UserManager.balance.toString().toDouble() == 0.00) {
                        dlgStatus(requireContext(), "0 funds").show()
                    } else {
                        UserManager.isOnAnotherActivity = true
                        val intent = Intent(requireActivity(), ActQrCodeScanner::class.java)
                        intent.putExtra("From ActQrCodeScanner", "ActPurchase")
                        requireActivity().startActivity(intent)
                    }
                } else {
                    dlgStatus(requireContext(), "no internet").show()
                }
            }

            btnPurchaseUsingPoints.setOnClickListener {
                if (isInternetConnected(requireContext())) {
                    if (UserManager.points.toString().toDouble() == 0.00) {
                        dlgStatus(requireContext(), "0 points").show()
                    } else {
                        UserManager.isOnAnotherActivity = true
                        val intent = Intent(requireActivity(), ActQrCodeScanner::class.java)
                        intent.putExtra("From ActQrCodeScanner", "ActPurchaseUsingStars")
                        requireActivity().startActivity(intent)
                    }
                } else {
                    dlgStatus(requireContext(), "no internet").show()
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        onBackPressed()
                    }
                })

            var previousSnapshot: QuerySnapshot? = null
            transactionRef.addSnapshotListener { snapshot, exception ->
                if (exception != null) return@addSnapshotListener
                if (previousSnapshot != null && snapshot!!.size() > previousSnapshot!!.size() && isVisible) {
                    bind.cardWallet.performClick()
                }
                previousSnapshot = snapshot
            }
        }
        return bind.root
    }

    private fun constraintViews() {
        when (UserManager.role) {
            "super admin" -> {
                bind.btnPurchaseUsingPoints.visibility = View.GONE
                bind.lblPurchaseUsingPoints.visibility = View.GONE
                bind.btnPurchase.visibility = View.GONE
                bind.lblPurchase.visibility = View.GONE
                bind.btnMyQr.visibility = View.GONE
                bind.lblMyQr.visibility = View.GONE
                bind.btnRequestMoney.visibility = View.VISIBLE
                bind.lblRequestMoney.visibility = View.VISIBLE
                bind.cardPoints.visibility = View.GONE
            }

            "admin" -> {
                bind.btnPurchaseUsingPoints.visibility = View.GONE
                bind.lblPurchaseUsingPoints.visibility = View.GONE
                bind.btnPurchase.visibility = View.GONE
                bind.lblPurchase.visibility = View.GONE
                bind.btnRequestMoney.visibility = View.GONE
                bind.lblRequestMoney.visibility = View.GONE
                bind.cardPoints.visibility = View.GONE
            }

            "standard" -> {
                bind.btnRequestMoney.visibility = View.GONE
                bind.lblRequestMoney.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateMainHome() {
        userRef.document(UserManager.uid!!).get().apply {
            addOnSuccessListener {
                UserManager.setUserInformation(it)
                val fullname = "${UserManager.firstname} ${UserManager.lastname}"
                val balance = formatCredits(UserManager.balance)
                val points = formatPoints(UserManager.points.toString().toDouble())

                bind.lblUserFullName.text = fullname
                bind.lblAmountBalance.text = balance
                bind.lblHygeiaPoints.text = points
            }
            addOnFailureListener {
                requireContext().msg("No internet connection.")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfTransactions() {
        bind.cover.visibility = View.VISIBLE

        bind.lblMessage.text = "Fetching your latest transaction information, please wait..."
        bind.loading.visibility = View.VISIBLE
        bind.listViewTransactionHistory.layoutManager = LinearLayoutManager(requireContext())
        listOfTransactions = arrayListOf()

        val query = transactionRef.whereEqualTo("User Reference", UserManager.uid).limit(3)
            .orderBy("Date Created", Query.Direction.DESCENDING)

        query.get().apply {
            addOnSuccessListener { data ->
                listOfTransactions.clear()
                if (!data.isEmpty) {
                    bind.cover.visibility = View.GONE
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
                        listOfTransactions, this@FrgMainHome
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

    private fun onBackPressed() {
        if (isInternetConnected(requireContext())) {
            dlgConfirmation(requireContext(), "log out") {
                if (it == ButtonType.PRIMARY) {
                    userRef.document(UserManager.uid!!).update("isOnline", false)
                        .addOnSuccessListener {
                            requireActivity().finish()
                        }
                }
            }.show()
        } else {
            dlgStatus(requireContext(), "no internet").show()
        }
    }

    override fun onResume() {
        super.onResume()
        isVisible = true
        constraintViews()
        populateMainHome()
        getListOfTransactions()
    }

    override fun onStop() {
        super.onStop()
        isVisible = false
    }

    override fun onPause() {
        super.onPause()
        isVisible = false
    }

    override fun onTransactionItemClick(ID: String) {
        Utilities.dlgTransactionDetails(requireContext(), ID).show()
    }
}