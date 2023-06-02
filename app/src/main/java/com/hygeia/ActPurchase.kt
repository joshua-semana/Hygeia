package com.hygeia

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.adapters.ArrAdpMachines
import com.hygeia.adapters.ArrAdpProducts
import com.hygeia.classes.DataMachines
import com.hygeia.classes.DataProducts
import com.hygeia.databinding.ActPurchaseBinding
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgError
import com.hygeia.objects.Utilities.dlgLoading

class ActPurchase : AppCompatActivity() {
    private lateinit var bind : ActPurchaseBinding
    private lateinit var listOfProducts: ArrayList<DataProducts>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")

    private val machineID = "GP1wZD9P9NVZfGjS1gJp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActPurchaseBinding.inflate(layoutInflater)
        loading = dlgLoading(this@ActPurchase)
        setContentView(bind.root)
        getListOfProducts()
    }

    private fun getListOfProducts() {
        loading.show()
        bind.listViewProducts.layoutManager = LinearLayoutManager(this@ActPurchase)
        listOfProducts = arrayListOf()

        machinesRef.document(machineID).get().apply {
            addOnSuccessListener { parent ->

                val vendoRef = "Vendo #${parent.get("Name").toString()}"
                bind.lblDescVendoID.text = vendoRef

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
                        bind.listViewProducts.adapter = ArrAdpProducts(listOfProducts)
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
}