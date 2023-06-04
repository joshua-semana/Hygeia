package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.adapters.ArrAdpProductAdmin
import com.hygeia.classes.ButtonType
import com.hygeia.classes.DataProductAdmin
import com.hygeia.databinding.ActMachineBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.MachineManager.dlgEditProduct
import com.hygeia.objects.MachineManager.dlgEditVendoLocation
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActMachine : AppCompatActivity(), ArrAdpProductAdmin.OnProductEditItemClickListener {
    private lateinit var bind : ActMachineBinding
    private lateinit var listOfProducts: ArrayList<DataProductAdmin>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActMachineBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActMachine)
        setContentView(bind.root)

        with(bind) {
            //POPULATE
            lblDescVendoID.text = "Vendo No. ${MachineManager.name}"
            lblDescVendoLocation.text = "Located at ${MachineManager.location}"

            //MAIN FUNCTIONS
            vendoDetails.setOnClickListener {
                dlgEditVendoLocation(this@ActMachine) {
                    if(isInternetConnected(applicationContext)){
                        if(it == ButtonType.PRIMARY){
                                lblDescVendoID.text = "Vendo No. ${MachineManager.name}"
                                lblDescVendoLocation.text = "Located at ${MachineManager.location}"
                            }
                        }else{
                            dlgStatus(this@ActMachine, "no internet").show()
                        }
                    }.show()
            }
            btnBack.setOnClickListener {
                onBackPressed()
            }
        }

        getListOfProducts()
    }

    private fun getListOfProducts() {
        loading.show()
        bind.listViewMachineDetail.layoutManager = LinearLayoutManager(this@ActMachine)
        listOfProducts = arrayListOf()

        machinesRef.document(MachineManager.uid.toString()).get().apply {
            addOnSuccessListener { parent ->

                val productsRef = parent.reference.collection("Products")

                productsRef.get().apply {
                    addOnSuccessListener { child ->
                        for (item in child.documents) {
                            val productId = item.id
                            val productName = item.get("Name")
                            val productPrice = item.get("Price")
                            val productQuantity = item.get("Quantity")
                            val productSlot = item.get("Slot")

                            val product = DataProductAdmin(
                                productId,
                                productName.toString(),
                                productPrice.toString(),
                                productQuantity.toString(),
                                productSlot.toString().toInt()
                            )

                            listOfProducts.add(product)
                        }
                        bind.listViewMachineDetail.adapter = ArrAdpProductAdmin(listOfProducts, this@ActMachine)
                        loading.dismiss()
                    }
                }
            }
            addOnFailureListener {
                Utilities.dlgError(this@ActMachine, it.toString()).show()
                loading.dismiss()
            }
        }
    }

    override fun onProductEditItemClick(productID: String) {
        if (isInternetConnected(applicationContext)){
            dlgEditProduct(this@ActMachine, productID){
                if(it == ButtonType.PRIMARY) {
                    getListOfProducts()
                }
            }.show()
        }else {
            dlgStatus(this@ActMachine, "no internet").show()
        }
    }
}