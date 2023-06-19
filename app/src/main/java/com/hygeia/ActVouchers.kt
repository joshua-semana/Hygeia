package com.hygeia

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hygeia.adapters.ArrAdpVoucher
import com.hygeia.classes.ButtonType
import com.hygeia.classes.DataVoucher
import com.hygeia.databinding.ActVouchersBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected
import com.hygeia.objects.Utilities.msg
import com.hygeia.objects.Utilities.showRequiredTextField
import java.util.Date
import kotlin.random.Random

class ActVouchers : AppCompatActivity(), ArrAdpVoucher.OnItemClickListener {
    private lateinit var bind: ActVouchersBinding
    private lateinit var listOfVouchers: ArrayList<DataVoucher>

    private var db = FirebaseFirestore.getInstance()
    private var vouchersRef = db.collection("Vouchers")
    private var userRef = db.collection("User")

    private var balance = UserManager.balance.toString().toDouble()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActVouchersBinding.inflate(layoutInflater)
        setContentView(bind.root)

        constraintViews()

        with(bind) {
            @Suppress("DEPRECATION") chipGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.chipAllVouchers -> {
                        getListOfVouchersForAdmin(vouchersRef)
                    }

                    R.id.chipCreated -> {
                        getListOfVouchersForAdmin(vouchersRef.whereEqualTo("Type", "Create"))
                    }

                    R.id.chipRedeemed -> {
                        getListOfVouchersForAdmin(vouchersRef.whereEqualTo("Type", "Redeem"))
                    }

                    R.id.chipCancelled -> {
                        getListOfVouchersForAdmin(vouchersRef.whereEqualTo("Type", "Cancel"))
                    }
                }
            }
        }

        bind.txtLayoutAmount.helperText = "You have $balance in your wallet."

        textWatcher(bind.txtAmount)

        bind.btnCreateVoucher.setOnClickListener {
            clearTextError(bind.txtLayoutAmount)
            if (isInternetConnected(this@ActVouchers)) {
                if (bind.txtAmount.text!!.isNotEmpty()) {
                    val amount = bind.txtAmount.text.toString().toDoubleOrNull()
                    if (amount != null && amount != 0.0 && amount > 0.99) {
                        if (UserManager.balance.toString().toDouble() > amount) {
                            dlgConfirmation(this@ActVouchers, "create voucher") {
                                if (it == ButtonType.PRIMARY) {
                                    createVoucher()
                                }
                            }.show()
                        } else {
                            bind.txtLayoutAmount.error = "You only have $balance in your wallet."
                        }
                    } else {
                        bind.txtLayoutAmount.error = "Amount must be greater than or equal to 1."
                    }
                } else {
                    showRequiredTextField(
                        bind.txtAmount to bind.txtLayoutAmount
                    )
                }
            } else {
                dlgStatus(this@ActVouchers, "no internet").show()
            }
        }

        bind.btnClaimVoucher.setOnClickListener {
            clearTextError(bind.txtLayoutCode)
            if (isInternetConnected(this@ActVouchers)) {
                if (bind.txtCode.text!!.isNotEmpty()) {
                    claimVoucher(bind.txtCode.text.toString())
                } else {
                    showRequiredTextField(
                        bind.txtCode to bind.txtLayoutCode
                    )
                }
            } else {
                dlgStatus(this@ActVouchers, "no internet").show()
            }
        }

        bind.btnBack.setOnClickListener {
            this.finish()
        }
    }

    private fun createVoucher() {
        val data = hashMapOf(
            "Amount" to bind.txtAmount.text.toString().toDouble(),
            "Date Created" to Timestamp(Date()),
            "Code" to Random.nextInt(100000000, 999999999).toString(),
            "Type" to "Create",
            "Created By" to UserManager.uid,
            "Redeemed By" to "",
            "Redeemer Name" to ""
        )

        vouchersRef.document().set(data).addOnSuccessListener {
            updateMinusUserBalance()
            getListOfVouchersForAdmin(vouchersRef)
            bind.txtAmount.setText("")
        }
    }

    private fun claimVoucher(code: String) {
        vouchersRef.whereEqualTo("Code", code).whereEqualTo("Type", "Create").get().apply {
            addOnSuccessListener { data ->
                if (!data.isEmpty) {
                    val data1 = mapOf(
                        "Type" to "Redeem",
                        "Redeemed By" to UserManager.uid.toString(),
                        "Redeemer Name" to "${UserManager.firstname} ${UserManager.lastname}"
                    )

                    vouchersRef.document(data.documents[0].id).update(data1).addOnSuccessListener {
                        bind.txtCode.setText("")
                        dlgStatus(this@ActVouchers, "claim voucher").show()
                        updateAddUserBalance(data.documents[0].get("Amount").toString().toDouble())
                        getListOfVouchersForStandard(vouchersRef)
                    }
                } else {
                    bind.txtLayoutCode.error = "This voucher code does not exist."
                }
            }
        }
    }

    private fun updateAddUserBalance(amount: Double) {
        balance = UserManager.balance.toString().toDouble() + amount
        bind.txtLayoutAmount.helperText = "You have $balance in your wallet."
        userRef.document(UserManager.uid!!.trim()).update("balance", balance)
        userRef.document(UserManager.uid!!).get().addOnSuccessListener { data ->
            UserManager.updateUserBalance(data)
        }
    }

    private fun updateMinusUserBalance() {
        balance =
            UserManager.balance.toString().toDouble() - bind.txtAmount.text.toString().toDouble()
        userRef.document(UserManager.uid!!.trim()).update("balance", balance)
        userRef.document(UserManager.uid!!).get().addOnSuccessListener { data ->
            UserManager.updateUserBalance(data)
        }
    }

    private fun cancelVoucher(id: String, amount: Double) {
        vouchersRef.document(id).update("Type", "Cancel").addOnSuccessListener {
            updateAddUserBalance(amount)
            getListOfVouchersForAdmin(vouchersRef)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfVouchersForAdmin(query: Query) {
        bind.cover.visibility = View.VISIBLE

        bind.lblMessage.text = "Fetching list of vouchers, please wait..."
        bind.loading.visibility = View.VISIBLE
        bind.listViewVouchers.layoutManager = LinearLayoutManager(this@ActVouchers)
        listOfVouchers = arrayListOf()

        query.orderBy("Date Created", Query.Direction.DESCENDING).get().apply {
            addOnSuccessListener { data ->
                listOfVouchers.clear()
                if (!data.isEmpty) {
                    bind.cover.visibility = View.GONE
                    for (item in data.documents) {
                        val items = DataVoucher(
                            item.id,
                            item.get("Amount").toString(),
                            item.get("Code").toString(),
                            item.get("Date Created") as Timestamp,
                            item.get("Redeemed By").toString(),
                            item.get("Redeemer Name").toString(),
                            item.get("Type").toString(),
                        )
                        listOfVouchers.add(items)
                    }
                    bind.listViewVouchers.adapter = ArrAdpVoucher(listOfVouchers, this@ActVouchers)
                } else {
                    bind.lblMessage.text = "There are currently no vouchers."
                    bind.loading.visibility = View.GONE
                }
            }
            addOnFailureListener {
                this@ActVouchers.msg("Please try again.")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfVouchersForStandard(query: Query) {
        bind.cover.visibility = View.VISIBLE

        bind.lblMessage.text = "Fetching list of vouchers, please wait..."
        bind.loading.visibility = View.VISIBLE
        bind.listViewVouchers.layoutManager = LinearLayoutManager(this@ActVouchers)
        listOfVouchers = arrayListOf()

        query.whereEqualTo("Redeemed By", UserManager.uid!!)
            .orderBy("Date Created", Query.Direction.DESCENDING).get().apply {
                addOnSuccessListener { data ->
                    listOfVouchers.clear()
                    if (!data.isEmpty) {
                        bind.cover.visibility = View.GONE
                        for (item in data.documents) {
                            val items = DataVoucher(
                                item.id,
                                item.get("Amount").toString(),
                                item.get("Code").toString(),
                                item.get("Date Created") as Timestamp,
                                item.get("Redeemed By").toString(),
                                item.get("Redeemer Name").toString(),
                                item.get("Type").toString(),
                            )
                            listOfVouchers.add(items)
                        }
                        bind.listViewVouchers.adapter =
                            ArrAdpVoucher(listOfVouchers, this@ActVouchers)
                    } else {
                        bind.lblMessage.text = "There are currently no vouchers."
                        bind.loading.visibility = View.GONE
                    }
                }
                addOnFailureListener {
                    this@ActVouchers.msg("Please try again.")
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun constraintViews() {
        when (UserManager.role) {
            "admin" -> {
                bind.lblDescription.text = "You can long press your created voucher to delete it."
                bind.adminControls.visibility = View.VISIBLE
                getListOfVouchersForAdmin(vouchersRef)
                bind.chipGroupScroll.visibility = View.VISIBLE
            }

            "standard" -> {
                bind.lblDescription.text = getString(R.string.desc_voucher)
                bind.adminControls.visibility = View.GONE
                getListOfVouchersForStandard(vouchersRef)
                bind.chipGroupScroll.visibility = View.GONE
            }
        }
    }

    private fun textWatcher(textField: EditText) {
        textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                bind.txtLayoutAmount.helperText = "You have $balance in your wallet."
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                val validInput = UserManager.balance.toString().toDouble()
                val userInput = input.toDoubleOrNull() ?: 0.0
                if (userInput > validInput) {
                    bind.txtLayoutAmount.error = "You only have $balance in your wallet."
                } else {
                    bind.txtLayoutAmount.helperText = "You have $balance in your wallet."
                    clearTextError(bind.txtLayoutAmount)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onItemLongClick(ID: String, amount: Double) {
        dlgConfirmation(this@ActVouchers, "cancel voucher") {
            if (it == ButtonType.PRIMARY) cancelVoucher(ID, amount)
        }.show()
    }
}