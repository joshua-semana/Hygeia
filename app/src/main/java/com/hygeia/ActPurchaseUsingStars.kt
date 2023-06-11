package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hygeia.adapters.ArrAdpProductsUsingPoints
import com.hygeia.classes.ButtonType
import com.hygeia.classes.DataProducts
import com.hygeia.databinding.ActPurchaseUsingStarsBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.msg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class ActPurchaseUsingStars : AppCompatActivity(),
    ArrAdpProductsUsingPoints.OnProductItemClickListener {
    private lateinit var bind: ActPurchaseUsingStarsBinding
    private lateinit var listOfProducts: ArrayList<DataProducts>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")
    private var transactionRef = db.collection("Transactions")
    private var userRef = db.collection("User")

    private var grandTotal = 0.00
    private var totalCount = 0

    private val machineID = MachineManager.machineId
    private var machineName = MachineManager.name

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val usedNumbers = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActPurchaseUsingStarsBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActPurchaseUsingStars)
        setContentView(bind.root)

        UserManager.isOnAnotherActivity = true
        UserManager.setUserOnline()

        bind.lblHygeiaStarsBalanceNumber.text = Utilities.formatPoints(UserManager.points)
        getListOfProducts()
        updatePurchaseBreakdown()
        with(bind) {
            btnPurchase.setOnClickListener {
                if (Utilities.isInternetConnected(applicationContext)) {
                    if (grandTotal == 0.00) {
                        Utilities.dlgStatus(this@ActPurchaseUsingStars, "empty cart").show()
                    } else if (grandTotal > UserManager.points.toString().toDouble()) {
                        Utilities.dlgStatus(this@ActPurchaseUsingStars, "insufficient points")
                            .show()
                    } else {
                        Utilities.dlgConfirmation(this@ActPurchaseUsingStars, "purchase") {
                            if (it == ButtonType.PRIMARY) {
                                saveTransaction()
                            }
                        }.show()
                    }
                } else {
                    Utilities.dlgStatus(this@ActPurchaseUsingStars, "no internet").show()
                }
            }
            btnBack.setOnClickListener {
                onBackBtnPressed()
            }
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackBtnPressed()
                }
            })
        }
    }

    private fun onBackBtnPressed() {
        if (grandTotal == 0.00) {
            machinesRef.document(machineID.toString()).update("User Connected", 0)
                .addOnSuccessListener {
                    finish()
                }
        } else {
            Utilities.dlgConfirmation(this@ActPurchaseUsingStars, "going back") {
                if (it == ButtonType.PRIMARY) {
                    machinesRef.document(machineID.toString()).update("User Connected", 0)
                        .addOnSuccessListener {
                            finish()
                        }
                }
            }.show()
        }
    }

    private fun saveTransaction() {
        val data = hashMapOf(
            "Amount" to grandTotal,
            "Date Created" to Timestamp(Date()),
            "Reference Number" to createReferenceNumber(),
            "Number" to 0,
            "Type" to "Purchase Using Star",
            "User Reference" to UserManager.uid,
            "Vendo" to machineName
        )

        val docRef = transactionRef.document()
        docRef.set(data).addOnSuccessListener {
            val adapter = ArrAdpProductsUsingPoints(listOfProducts, this@ActPurchaseUsingStars)
            for (i in 0 until adapter.itemCount) {
                val viewHolder: RecyclerView.ViewHolder =
                    adapter.createViewHolder(bind.listViewProducts, adapter.getItemViewType(i))
                adapter.bindViewHolder(viewHolder as ArrAdpProductsUsingPoints.ViewHolder, i)

                val product: DataProducts? = adapter.getNonZeroQuantityProduct(i)
                if (product != null) {
                    if (product.Count != 0) {
                        totalCount += product.Count

                        val subData = hashMapOf(
                            "Name" to product.Name,
                            "Price" to product.Points.toString(),
                            "Quantity" to product.Count
                        )

                        val database = FirebaseDatabase.getInstance()
                        val reference = database.getReference("Orders")
                        val order =
                            mapOf<String, Any>("Slot_" + product.Slot.toString() to product.Count)
                        reference.updateChildren(order)

                        docRef.collection("Items").document().set(subData)

                        //UPDATE Product Quantity
                        val productsRef =
                            machinesRef.document(machineID.toString()).collection("Products")
                        productsRef.document(product.ID!!)
                            .update("Quantity", product.Quantity!!.toInt() - product.Count)
                    }
                }
            }
            docRef.update("Number", totalCount)
            updateUserPointBalance()
            MachineManager.dlgLoadingPurchase(this@ActPurchaseUsingStars).apply {
                setOnDismissListener {
                    finishTransaction()
                }
                show()
            }
        }
    }

    private fun createReferenceNumber(): String {
        val currentDate = dateFormat.format(Date())
        val randomFourDigits = getRandomFourDigits()

        return "$currentDate${String.format("%04d", randomFourDigits)}"
    }

    private fun getRandomFourDigits(): Int {
        val random = Random()
        var randomNum = random.nextInt(10000)
        while (usedNumbers.contains(randomNum)) {
            randomNum = random.nextInt(10000)
        }
        usedNumbers.add(randomNum)
        return randomNum
    }

    private fun updateUserPointBalance() {
        userRef.document(UserManager.uid!!.trim())
            .update("points", UserManager.points.toString().toDouble() - grandTotal)
        userRef.document(UserManager.uid!!).get().addOnSuccessListener { data ->
            UserManager.updateUserBalance(data)
        }
    }

    private fun finishTransaction() {
        Utilities.dlgStatus(this@ActPurchaseUsingStars, "success purchase").apply {
            setOnDismissListener {
                machinesRef.document(machineID.toString()).update("User Connected", 0)
                    .addOnSuccessListener {
                        finish()
                    }
            }
            show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfProducts() {
        bind.cover.visibility = View.VISIBLE

        bind.lblMessage.text = "Fetching products available in this machine, please wait..."
        bind.loading.visibility = View.VISIBLE
        bind.listViewProducts.layoutManager = LinearLayoutManager(this@ActPurchaseUsingStars)
        listOfProducts = arrayListOf()

        machinesRef.document(machineID.toString()).get().apply {
            addOnSuccessListener { parent ->

                machineName = parent.get("Name").toString()
                bind.lblDescVendoID.text = "Vendo No. ${parent.get("Name").toString()}"
                bind.lblDescVendoLocation.text = "Located at ${parent.get("Location").toString()}"

                val productsRef = parent.reference.collection("Products")
                    .orderBy("Slot", Query.Direction.ASCENDING)

                productsRef.get().apply {
                    addOnSuccessListener { child ->
                        listOfProducts.clear()
                        if (!child.isEmpty) {
                            bind.cover.visibility = View.GONE
                            for (item in child.documents) {
                                val product = DataProducts(
                                    item.id,
                                    item.get("Name").toString(),
                                    item.get("Price").toString(),
                                    item.get("Quantity").toString(),
                                    item.get("Slot").toString().toInt(),
                                    item.get("Status").toString().toInt(),
                                    item.get("Price in Points").toString().toDouble()
                                )
                                listOfProducts.add(product)
                            }
                            bind.listViewProducts.adapter = ArrAdpProductsUsingPoints(
                                listOfProducts, this@ActPurchaseUsingStars
                            )
                            if (bind.listViewProducts.adapter?.itemCount == 0) {
                                bind.cover.visibility = View.VISIBLE
                                bind.lblMessage.text =
                                    "Sorry for the inconvenience. There are no available items for this vending machine."
                                bind.loading.visibility = View.GONE
                            }
                        }
                    }
                    addOnFailureListener {
                        this@ActPurchaseUsingStars.msg("Please try again.")
                    }
                }
            }
            addOnFailureListener {
                this@ActPurchaseUsingStars.msg("Please try again.")
            }
        }
    }

    private fun updatePurchaseBreakdown() {
        with(bind) {
            lblGrandTotalNumber.text = Utilities.formatPoints(grandTotal)
        }
    }

    override fun onAddOrMinusClick(productPrice: Double) {
        grandTotal = productPrice
        updatePurchaseBreakdown()
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            machinesRef.document(MachineManager.machineId!!).update("User Connected", 0)
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        } else {
            machinesRef.document(MachineManager.machineId!!).update("User Connected", 0)
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        machinesRef.document(MachineManager.machineId!!).update("User Connected", 1)
        UserManager.setUserOnline()
    }
}