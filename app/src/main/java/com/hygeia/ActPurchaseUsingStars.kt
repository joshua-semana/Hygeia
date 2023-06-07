package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.adapters.ArrAdpProducts
import com.hygeia.adapters.ArrAdpProductsUsingPoints
import com.hygeia.classes.ButtonType
import com.hygeia.classes.DataProducts
import com.hygeia.databinding.ActPurchaseUsingStarsBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class ActPurchaseUsingStars : AppCompatActivity(), ArrAdpProductsUsingPoints.OnProductItemClickListener {
    private lateinit var bind : ActPurchaseUsingStarsBinding
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

        bind.lblHygeiaStarsBalanceNumber.text = Utilities.formatPoints(UserManager.points)
        getListOfProducts()
        updatePurchaseBreakdown()
        with(bind){
            btnPurchase.setOnClickListener {
                if (Utilities.isInternetConnected(applicationContext)){
                    if (grandTotal == 0.00) {
                        Utilities.dlgStatus(this@ActPurchaseUsingStars, "empty cart").show()
                    } else if (grandTotal > UserManager.points.toString().toDouble()) {
                        Utilities.dlgStatus(this@ActPurchaseUsingStars, "insufficient points").show()
                    } else {
                        Utilities.dlgConfirmation(this@ActPurchaseUsingStars, "purchase") {
                            if (it == ButtonType.PRIMARY) {
                                loading.show()
                                saveTransaction()
                            }
                        }.show()
                    }
                }else {
                    Utilities.dlgStatus(this@ActPurchaseUsingStars, "no internet").show()
                }
            }
            btnBack.setOnClickListener{
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
                val viewHolder: RecyclerView.ViewHolder = adapter.createViewHolder(bind.listViewProducts, adapter.getItemViewType(i))
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

                        docRef.collection("Items").document().set(subData)

                        //UPDATE Product Quantity
                        val productsRef = machinesRef.document(machineID.toString()).collection("Products")
                        productsRef.document(product.ID!!).update("Quantity", product.Quantity!!.toInt() - product.Count)
                    }
                }
            }
            docRef.update("Number", totalCount)
            updateUserPointBalance()
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
        userRef.document(UserManager.uid!!.trim()).update("points", UserManager.points.toString().toDouble() - grandTotal)
        userRef.document(UserManager.uid!!).get().addOnSuccessListener { data ->
            UserManager.updateUserBalance(data)
        }
        finishTransaction()
    }
    private fun finishTransaction() {
        loading.dismiss()
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
        loading.show()
        bind.listViewProducts.layoutManager = LinearLayoutManager(this@ActPurchaseUsingStars)
        listOfProducts = arrayListOf()

        machinesRef.document(machineID.toString()).get().apply {
            addOnSuccessListener { parent ->

                machineName = parent.get("Name").toString()
                bind.lblDescVendoID.text = "Vendo No. ${parent.get("Name").toString()}"
                bind.lblDescVendoLocation.text = "Located at ${parent.get("Location").toString()}"

                val productsRef = parent.reference.collection("Products")

                productsRef.get().apply {
                    addOnSuccessListener { child ->
                        for (item in child.documents) {
                            val productId = item.id
                            val productName = item.get("Name")
                            val productPrice = item.get("Price")
                            val productQuantity = item.get("Quantity")
                            val productSlot = item.get("Slot")
                            val productStatus = item.get("Status")
                            val productPoints = item.get("Price in Points")

                            val product = DataProducts(
                                productId,
                                productName.toString(),
                                productPrice.toString(),
                                productQuantity.toString(),
                                productSlot.toString().toInt(),
                                productStatus.toString().toInt(),
                                productPoints.toString().toDouble()
                            )

                            listOfProducts.add(product)
                        }
                        bind.listViewProducts.adapter = ArrAdpProductsUsingPoints(listOfProducts, this@ActPurchaseUsingStars)
                        loading.dismiss()
                    }
                }
            }
            addOnFailureListener {
                Utilities.dlgError(this@ActPurchaseUsingStars, it.toString()).show()
                loading.dismiss()
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
}