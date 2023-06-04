package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
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
import com.hygeia.objects.MachineManager.machineId
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.clearTextError
import com.hygeia.objects.Utilities.isInternetConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActMachine : AppCompatActivity(), ArrAdpProductAdmin.OnProductEditItemClickListener {
    private lateinit var bind : ActMachineBinding
    private lateinit var listOfProducts: ArrayList<DataProductAdmin>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActMachineBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActMachine)
        setContentView(bind.root)

        with(bind) {
            //POPULATE
            populateView()
            getListOfProducts()
            bind.switchVendoStatus.isChecked = MachineManager.status == "Online"

            //MAIN FUNCTIONS

            switchVendoStatus.setOnCheckedChangeListener { _, isChecked ->
                loading.show()
                if (isChecked) {
                    machinesRef.document(machineId!!.trim()).update("Status", "Online").addOnSuccessListener {
                        MachineManager.status = "Online"
                        populateView()
                        loading.dismiss()
                    }
                } else {
                    machinesRef.document(machineId!!.trim()).update("Status", "Offline").addOnSuccessListener {
                        MachineManager.status = "Offline"
                        populateView()
                        loading.dismiss()
                    }
                }
            }

            btnVendoDetailEdit.setOnClickListener {
                dlgEditVendoLocation(this@ActMachine).show()
            }

            btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateView() {
        bind.lblDescVendoID.text = "Vendo No. ${MachineManager.name}"
        bind.lblDescVendoLocation.text = "Located at ${MachineManager.location}"
        bind.lblVendoStatus.text = "Vendo status is currently: \"${MachineManager.status}\""
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
        TODO("Not yet implemented")
    }

//    override fun onProductEditItemClick(productID: String) {
//        if (isInternetConnected(applicationContext)){
//            dlgEditProduct(this@ActMachine, productID){
//                if(it == ButtonType.PRIMARY){
//
////                            val productUpdatedData = hashMapOf<String, Any>(
////                                "Name" to txtDlgProductName.text.toString(),
////                                "Price" to txtDlgProductPrice.text.toString().toLong(),
////                                "Quantity" to txtDlgProductQuantity.text.toString().toLong()
////                            )
////                            MachineManager.machineRef.document(MachineManager.uid!!.trim()).get().addOnSuccessListener{ parent ->
////                                parent.reference.collection("Products").document(productID)
////                                    .update(productUpdatedData).addOnSuccessListener {
////                                        dialog.dismiss()
////                                    }
////                            }
//                    }
//                }
//            }
//        }else {
//            Utilities.dlgStatus(this@ActMachine, "no internet").show()
//        }
//    }
}