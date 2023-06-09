package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.adapters.ArrAdpProductAdmin
import com.hygeia.classes.ButtonType
import com.hygeia.classes.DataProductAdmin
import com.hygeia.databinding.ActMachineBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.MachineManager.dlgEditProduct
import com.hygeia.objects.MachineManager.dlgEditProductPoints
import com.hygeia.objects.MachineManager.dlgEditVendoLocation
import com.hygeia.objects.MachineManager.machineId
import com.hygeia.objects.MachineManager.machineRef
import com.hygeia.objects.MachineManager.name
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected
import java.util.Date

class ActMachine : AppCompatActivity(), ArrAdpProductAdmin.OnProductEditItemClickListener {
    private lateinit var bind: ActMachineBinding
    private lateinit var listOfProducts: ArrayList<DataProductAdmin>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")
    private var historyRef = db.collection("History")

    @SuppressLint("SetTextI18n")
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
            vendoDetails.setOnClickListener {
                dlgEditVendoLocation(this@ActMachine) {
                    if (isInternetConnected(applicationContext)) {
                        if (it == ButtonType.PRIMARY) {
                            loading.show()
                            val historyData = hashMapOf(
                                "Content" to "Updated machine location information.",
                                "Date Created" to Timestamp(Date()),
                                "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
                                "Machine ID" to machineId,
                                "Machine Name" to name,
                                "Type" to "Update",
                                "User Reference" to UserManager.uid
                            )
                            historyRef.document().set(historyData)
                            machineRef.document(machineId!!).get().addOnSuccessListener { data ->
                                lblDescVendoLocation.text =
                                    "Located at ${data.getString("Location")}"
                                loading.dismiss()
                            }
                        }
                    } else {
                        dlgStatus(this@ActMachine, "no internet").show()
                    }
                }.show()
            }

            switchVendoStatus.setOnCheckedChangeListener { _, isChecked ->
                loading.show()
                if (isChecked) {
                    machinesRef.document(machineId!!.trim()).update("Status", "Online")
                        .addOnSuccessListener {
                            val historyData = hashMapOf(
                                "Content" to "Updated machine status information, from Offline to Online.",
                                "Date Created" to Timestamp(Date()),
                                "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
                                "Machine ID" to machineId,
                                "Machine Name" to name,
                                "Type" to "Update",
                                "User Reference" to UserManager.uid
                            )
                            historyRef.document().set(historyData).addOnSuccessListener {
                                MachineManager.status = "Online"
                                populateView()
                                loading.dismiss()
                            }
                        }
                } else {
                    machinesRef.document(machineId!!.trim()).update("Status", "Offline")
                        .addOnSuccessListener {
                            val historyData = hashMapOf(
                                "Content" to "Updated machine status information, from Online to Offline.",
                                "Date Created" to Timestamp(Date()),
                                "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
                                "Machine ID" to machineId,
                                "Machine Name" to name,
                                "Type" to "Update",
                                "User Reference" to UserManager.uid
                            )
                            historyRef.document().set(historyData).addOnSuccessListener {
                                MachineManager.status = "Offline"
                                populateView()
                                loading.dismiss()
                            }
                        }
                }
            }

            btnHistory.setOnClickListener {
                startActivity(Intent(this@ActMachine, ActHistory::class.java))
            }

            btnDelete.setOnClickListener {
                dlgConfirmation(this@ActMachine, "delete machine") {
                    if (it == ButtonType.PRIMARY) {
                        loading.show()
                        deleteMachine()
                    }
                }.show()
            }

            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateView() {
        bind.lblDescVendoID.text = "Vendo No. $name"
        bind.lblDescVendoLocation.text = "Located at ${MachineManager.location}"
        bind.lblVendoStatus.text = "Vendo status is currently: \"${MachineManager.status}\""
    }

    private fun deleteMachine() {
        machinesRef.document(machineId!!).update("isEnabled", false).addOnSuccessListener {
            val historyData = hashMapOf(
                "Content" to "Deleted this vending machine.",
                "Date Created" to Timestamp(Date()),
                "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
                "Machine ID" to machineId,
                "Machine Name" to name,
                "Type" to "Delete",
                "User Reference" to UserManager.uid
            )
            historyRef.document().set(historyData).addOnSuccessListener {
                dlgStatus(this@ActMachine, "delete machine").apply {
                    setOnDismissListener {
                        loading.dismiss()
                        finish()
                    }
                    show()
                }
            }
        }
    }

    private fun getListOfProducts() {
        loading.show()
        bind.listViewMachineDetail.layoutManager = LinearLayoutManager(this@ActMachine)
        listOfProducts = arrayListOf()

        machinesRef.document(MachineManager.uid.toString()).get().apply {
            addOnSuccessListener { parent ->

                val productsRef = parent.reference.collection("Products").orderBy("Slot")

                productsRef.get().apply {
                    addOnSuccessListener { child ->
                        for (item in child.documents) {
                            val productId = item.id
                            val productName = item.get("Name")
                            val productPrice = item.get("Price")
                            val productQuantity = item.get("Quantity")
                            val productSlot = item.get("Slot")
                            val productStatus = item.get("Status")

                            val product = DataProductAdmin(
                                productId,
                                productName.toString(),
                                productPrice.toString(),
                                productQuantity.toString(),
                                productSlot.toString().toInt(),
                                productStatus.toString().toInt()
                            )

                            listOfProducts.add(product)
                        }
                        bind.listViewMachineDetail.adapter =
                            ArrAdpProductAdmin(listOfProducts, this@ActMachine)
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
        if (isInternetConnected(applicationContext)) {
            dlgEditProduct(this@ActMachine, productID) {
                if (it == ButtonType.PRIMARY) {
                    loading.show()
                    machinesRef.document(machineId!!).get().addOnSuccessListener { parent ->
                        parent.reference.collection("Products").document(productID).get()
                            .addOnSuccessListener { child ->
                                val historyData = hashMapOf(
                                    "Content" to "Updated information of vendo slot ${
                                        child.get("Slot").toString()
                                    }, set to:\nName: ${
                                        child.get(
                                            "Name"
                                        ).toString()
                                    }\nProduct Price: ${
                                        child.get(
                                            "Price"
                                        ).toString()
                                    }\nProduct Quantity: ${
                                        child.get("Quantity").toString()
                                    }\nVendo slot status: ${
                                        child.get("Status").toString()
                                    }",
                                    "Date Created" to Timestamp(Date()),
                                    "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
                                    "Machine ID" to machineId,
                                    "Machine Name" to name,
                                    "Type" to "Update",
                                    "User Reference" to UserManager.uid
                                )
                                historyRef.document().set(historyData).addOnSuccessListener {
                                    getListOfProducts()
                                }
                            }
                    }

                }
            }.show()
        } else {
            dlgStatus(this@ActMachine, "no internet").show()
        }
    }

    override fun onProductEditPointsClick(productID: String) {
        if (isInternetConnected(applicationContext)) {
            dlgEditProductPoints(this@ActMachine, productID) {
                if (it == ButtonType.PRIMARY) {
                    loading.show()
                    machinesRef.document(machineId!!).get().addOnSuccessListener { parent ->
                        parent.reference.collection("Products").document(productID).get()
                            .addOnSuccessListener { child ->
                                val historyData = hashMapOf(
                                    "Content" to "Updated stars information of vendo slot ${
                                        child.get("Slot").toString()
                                    }, set to:\nPrice in stars: ${
                                        child.get(
                                            "Price in Points"
                                        ).toString()
                                    }\nReward in stars: ${
                                        child.get(
                                            "Reward Points"
                                        ).toString()
                                    }",
                                    "Date Created" to Timestamp(Date()),
                                    "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
                                    "Machine ID" to machineId,
                                    "Machine Name" to name,
                                    "Type" to "Update",
                                    "User Reference" to UserManager.uid
                                )
                                historyRef.document().set(historyData).addOnSuccessListener {
                                    getListOfProducts()
                                }
                            }
                    }
                }
            }.show()
        } else {
            dlgStatus(this@ActMachine, "no internet").show()
        }
    }
}
