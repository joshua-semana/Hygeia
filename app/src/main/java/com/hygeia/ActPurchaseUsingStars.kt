package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.adapters.ArrAdpProducts
import com.hygeia.adapters.ArrAdpProductsUsingPoints
import com.hygeia.classes.DataProducts
import com.hygeia.databinding.ActPurchaseUsingStarsBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import java.text.SimpleDateFormat
import java.util.Locale

class ActPurchaseUsingStars : AppCompatActivity(), ArrAdpProductsUsingPoints.OnProductItemClickListener {
    private lateinit var bind : ActPurchaseUsingStarsBinding
    private lateinit var listOfProducts: ArrayList<DataProducts>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")
    private var transactionRef = db.collection("Transactions")
    private var userRef = db.collection("User")

    private var grandTotal = 0.00

    private val machineID = "GP1wZD9P9NVZfGjS1gJp"
    private var machineName = ""

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val usedNumbers = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActPurchaseUsingStarsBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActPurchaseUsingStars)
        setContentView(bind.root)

        bind.lblHygeiaStarsBalanceNumber.text = Utilities.formatPoints(UserManager.points)
        getListOfProducts()
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfProducts() {
        loading.show()
        bind.listViewProducts.layoutManager = LinearLayoutManager(this@ActPurchaseUsingStars)
        listOfProducts = arrayListOf()

        machinesRef.document(machineID).get().apply {
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
                                productPoints.toString().toInt()
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