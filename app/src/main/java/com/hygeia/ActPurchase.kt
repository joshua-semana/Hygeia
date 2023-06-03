package com.hygeia

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.adapters.ArrAdpProducts
import com.hygeia.classes.DataProducts
import com.hygeia.databinding.ActPurchaseBinding
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities.dlgError
import com.hygeia.objects.Utilities.dlgLoading
import com.hygeia.objects.Utilities.formatNumber
import java.text.DecimalFormat

class ActPurchase : AppCompatActivity(), ArrAdpProducts.OnProductItemClickListener {
    private lateinit var bind : ActPurchaseBinding
    private lateinit var listOfProducts: ArrayList<DataProducts>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")

    private var subTotal = 0.00
    private var vat = 0.00
    private var grandTotal = 0.00

    private val machineID = "GP1wZD9P9NVZfGjS1gJp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActPurchaseBinding.inflate(layoutInflater)
        loading = dlgLoading(this@ActPurchase)
        setContentView(bind.root)

        //POPULATE
        getListOfProducts()
        updatePurchaseBreakdown()
    }

    private fun updatePurchaseBreakdown() {
        with(bind) {
            lblWalletBalanceNumber.text = formatNumber(UserManager.balance)
            lblSubTotalNumber.text = formatNumber(subTotal)
            lblTaxNumber.text = formatNumber(vat)
            lblGrandTotalNumber.text = formatNumber(grandTotal)
        }
    }

    private fun getListOfProducts() {
        loading.show()
        bind.listViewProducts.layoutManager = LinearLayoutManager(this@ActPurchase)
        listOfProducts = arrayListOf()

        machinesRef.document(machineID).get().apply {
            addOnSuccessListener { parent ->

                val vendoName = "Vendo #${parent.get("Name").toString()}"
                bind.lblDescVendoID.text = vendoName

                val productsRef = parent.reference.collection("Products")

                productsRef.get().apply {
                    addOnSuccessListener { child ->
                        for (item in child.documents) {
                            //val productId = item.id
                            val productName = item.get("Name")
                            val productPrice = item.get("Price")
                            val productQuantity = item.get("Quantity")

                            val product = DataProducts(
                                productName.toString(),
                                productPrice.toString(),
                                productQuantity.toString()
                            )

                            listOfProducts.add(product)
                        }
                        bind.listViewProducts.adapter = ArrAdpProducts(listOfProducts, this@ActPurchase)
                        loading.dismiss()
                    }
                }
            }
            addOnFailureListener {
                dlgError(this@ActPurchase, it.toString()).show()
                loading.dismiss()
            }
        }
    }


    override fun onAddOrMinusClick(productPrice: Double) {
        grandTotal = productPrice
        vat = productPrice * 0.12
        subTotal = grandTotal - vat
        updatePurchaseBreakdown()
    }
}