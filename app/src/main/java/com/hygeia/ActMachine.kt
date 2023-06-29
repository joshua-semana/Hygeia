package com.hygeia

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
import com.hygeia.objects.Utilities.msg
import java.util.Date

class ActMachine : AppCompatActivity(), ArrAdpProductAdmin.OnProductEditItemClickListener {
    private lateinit var bind: ActMachineBinding
    private lateinit var listOfProducts: ArrayList<DataProductAdmin>
    private lateinit var dlgloading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")
    private var historyRef = db.collection("History")

    private var isAlarmed = "On"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActMachineBinding.inflate(layoutInflater)
        dlgloading = Utilities.dlgLoading(this@ActMachine)
        setContentView(bind.root)

        UserManager.isOnAnotherActivity = true
        UserManager.setUserOnline()

        populateView()
        getListOfProducts()

        val alarmRef = FirebaseDatabase.getInstance().getReference("isAlarmed")
        val alarmRefEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                when (dataSnapshot.value.toString()) {
                    "true" -> isAlarmed = "On"
                    "false" -> isAlarmed = "Off"
                }
                bind.switchVendoAlarm.isChecked = dataSnapshot.value.toString().toBoolean()
            }
            override fun onCancelled(databaseError: DatabaseError) { }
        }
        alarmRef.addValueEventListener(alarmRefEventListener)

        with(bind) {
            bind.switchVendoStatus.isChecked = MachineManager.status == "Online"
            vendoDetails.setOnClickListener {
                dlgEditVendoLocation(this@ActMachine) {
                    if (isInternetConnected(applicationContext)) {
                        if (it == ButtonType.PRIMARY) {
                            dlgloading.show()
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
                                if (data.getString("Location") == "Location Not Set") {
                                    lblDescVendoLocation.text = "Location Not Set"
                                } else {
                                    lblDescVendoLocation.text =
                                        "Located at ${data.getString("Location")}"
                                }
                                dlgloading.dismiss()
                            }
                        }
                    } else {
                        dlgStatus(this@ActMachine, "no internet").show()
                    }
                }.show()
            }

            switchVendoStatus.setOnCheckedChangeListener { _, isChecked ->
                if (isInternetConnected(this@ActMachine)) {
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
                                }
                            }
                    }
                } else {
                    dlgStatus(this@ActMachine, "no internet").show()
                    switchVendoStatus.isChecked = !switchVendoStatus.isChecked
                }
            }

            switchVendoAlarm.setOnCheckedChangeListener { _, isChecked ->
                if(isInternetConnected(this@ActMachine)) {
                    if(isChecked)
                    {
                        val database = FirebaseDatabase.getInstance()
                        val reference = database.getReference("isAlarmed")
                        reference.setValue(true).addOnSuccessListener {
                            val historyData = hashMapOf(
                                "Content" to "Updated machine alarm status, from On to Off.",
                                "Date Created" to Timestamp(Date()),
                                "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
                                "Machine ID" to machineId,
                                "Machine Name" to name,
                                "Type" to "Update",
                                "User Reference" to UserManager.uid
                            )
                            historyRef.document().set(historyData).addOnSuccessListener {
                                isAlarmed = "On"
                                populateView()
                            }
                        }
                    } else {
                        val database = FirebaseDatabase.getInstance()
                        val reference = database.getReference("isAlarmed")
                        reference.setValue(false).addOnSuccessListener {
                            val historyData = hashMapOf(
                                "Content" to "Updated machine alarm status, from Off to On.",
                                "Date Created" to Timestamp(Date()),
                                "Full Name" to "${UserManager.firstname} ${UserManager.lastname}",
                                "Machine ID" to machineId,
                                "Machine Name" to name,
                                "Type" to "Update",
                                "User Reference" to UserManager.uid
                            )
                            historyRef.document().set(historyData).addOnSuccessListener {
                                isAlarmed = "Off"
                                populateView()
                            }
                        }
                    }

                } else {
                    dlgStatus(this@ActMachine, "no internet").show()
                    switchVendoAlarm.isChecked = !switchVendoAlarm.isChecked
                }
            }

            btnHistory.setOnClickListener {
                startActivity(Intent(this@ActMachine, ActHistory::class.java))
            }

            btnDelete.setOnClickListener {
                if (isInternetConnected(this@ActMachine)) {
                    dlgConfirmation(this@ActMachine, "delete machine") {
                        if (it == ButtonType.PRIMARY) {
                            dlgloading.show()
                            deleteMachine()
                        }
                    }.show()
                } else {
                    dlgStatus(this@ActMachine, "no internet").show()
                }
            }

            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateView() {
        bind.lblDescVendoID.text = "Vendo No. $name"
        if (MachineManager.location == "Location Not Set") {
            bind.lblDescVendoLocation.text = MachineManager.location
        } else {
            bind.lblDescVendoLocation.text = "Located at ${MachineManager.location}"
        }
        bind.lblAlarmStatus.text = "Alarm is currently: \"$isAlarmed\""
        bind.lblVendoStatus.text = "Vendo status is currently: \"${MachineManager.status}\""
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfProducts() {
        bind.cover.visibility = View.VISIBLE

        bind.lblMessage.text = "Fetching the products of this machine, please wait..."
        bind.loading.visibility = View.VISIBLE
        bind.listViewMachineDetail.layoutManager = LinearLayoutManager(this@ActMachine)
        listOfProducts = arrayListOf()

        machinesRef.document(MachineManager.uid.toString()).get().apply {
            addOnSuccessListener { parent ->
                val productsRef = parent.reference.collection("Products").orderBy("Slot")
                productsRef.get().apply {
                    addOnSuccessListener { child ->
                        bind.cover.visibility = View.GONE
                        for (item in child.documents) {
                            val product = DataProductAdmin(
                                item.id,
                                item.get("Name").toString(),
                                item.get("Price").toString(),
                                item.get("Quantity").toString(),
                                item.get("Slot").toString().toInt(),
                                item.get("Status").toString().toInt()
                            )
                            listOfProducts.add(product)
                        }
                        bind.listViewMachineDetail.adapter = ArrAdpProductAdmin(
                            listOfProducts, this@ActMachine
                        )
                    }
                    addOnFailureListener {
                        this@ActMachine.msg("Please try again.")
                    }
                }
            }
            addOnFailureListener {
                this@ActMachine.msg("Please try again.")
            }
        }
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
                        dlgloading.dismiss()
                        finish()
                    }
                    show()
                }
            }
        }
    }

    override fun onProductEditItemClick(productID: String) {
        if (isInternetConnected(applicationContext)) {
            dlgEditProduct(this@ActMachine, productID) {
                if (it == ButtonType.PRIMARY) {
                    dlgloading.show()
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
                                    dlgloading.dismiss()
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
                    dlgloading.show()
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
                                    dlgloading.dismiss()
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

    override fun onResume() {
        super.onResume()
        UserManager.setUserOnline()
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        } else {
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        }
    }
}
