package com.hygeia.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.ActMachine
import com.hygeia.ActPurchase
import com.hygeia.ActSales
import com.hygeia.adapters.ArrAdpMachines
import com.hygeia.classes.DataMachines
import com.hygeia.classes.DataTransactions
import com.hygeia.databinding.FrgAdminToolsBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgConfirmation
import com.hygeia.objects.Utilities.dlgError
import com.hygeia.objects.Utilities.dlgLoading
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected

class FrgAdminTools : Fragment(), ArrAdpMachines.OnMachineItemClickListener {
    private lateinit var bind: FrgAdminToolsBinding
    private lateinit var listOfMachines: ArrayList<DataMachines>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FrgAdminToolsBinding.inflate(layoutInflater, container, false)
        loading = dlgLoading(requireContext())

        with(bind) {
            if (isInternetConnected(requireContext())) {
                getListOfMachines()
            } else {
                dlgStatus(requireContext(), "no internet").show()
            }

            return root
        }
    }

    private fun getListOfMachines() {
        loading.show()
        bind.listViewMachines.layoutManager = LinearLayoutManager(requireContext())
        listOfMachines = arrayListOf()

        val query = machinesRef.orderBy("Name")

        query.get().apply {
            addOnSuccessListener { data ->
                listOfMachines.clear()
                if (!data.isEmpty) {
                    for (item in data.documents) {
                        val machine: DataMachines? = item.toObject(DataMachines::class.java)
                        listOfMachines.add(machine!!)
                    }

                    val newMachine = DataMachines(
                        "New Machine"
                    )

                    listOfMachines.add(newMachine)

                    bind.listViewMachines.adapter =
                        ArrAdpMachines(listOfMachines, this@FrgAdminTools)
                }
                loading.dismiss()
            }
            addOnFailureListener {
                dlgError(requireContext(), it.toString()).show()
                loading.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getListOfMachines()
    }

    override fun onMachineInventoryItemClick(machineID: String) {
        loading.show()
        machinesRef.document(machineID.trim()).get().addOnSuccessListener { data ->
            MachineManager.setMachineInformation(data)
            startActivity(Intent(requireContext(), ActMachine::class.java))
            loading.dismiss()
        }
    }

    override fun onMachineSalesItemClick(machineID: String) {
        loading.show()
        machinesRef.document(machineID.trim()).get().addOnSuccessListener { data ->
            MachineManager.setMachineInformation(data)
            startActivity(Intent(requireContext(), ActSales::class.java))
            loading.dismiss()
        }
    }

    override fun onMachineAddItemClick() {
    }
}