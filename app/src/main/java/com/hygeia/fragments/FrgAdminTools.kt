package com.hygeia.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.hygeia.ActMachine
import com.hygeia.ActPurchase
import com.hygeia.adapters.ArrAdpMachines
import com.hygeia.classes.DataMachines
import com.hygeia.databinding.FrgAdminToolsBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.Utilities
import com.hygeia.objects.Utilities.dlgError
import com.hygeia.objects.Utilities.dlgLoading
import com.hygeia.objects.Utilities.dlgStatus
import com.hygeia.objects.Utilities.isInternetConnected

class FrgAdminTools : Fragment(), ArrAdpMachines.OnMachineItemClickListener {
    private lateinit var bind : FrgAdminToolsBinding
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

        machinesRef.get().apply {
            addOnSuccessListener { data ->
                if (!data.isEmpty) {
                    for (item in data.documents) {
                        val machine : DataMachines? = item.toObject(DataMachines::class.java)
                        listOfMachines.add(machine!!)
                    }
                    bind.listViewMachines.adapter = ArrAdpMachines(listOfMachines, this@FrgAdminTools)
                }
                loading.dismiss()
            }
            addOnFailureListener {
                dlgError(requireContext(), it.toString()).show()
                loading.dismiss()
            }
        }
    }

    override fun onMachineItemClick(machineID: String) {
        loading.show()
        machinesRef.document(machineID.trim()).get().addOnSuccessListener { data ->
            MachineManager.setMachineInformation(data)
            loading.dismiss()
            startActivity(Intent(requireContext(), ActMachine::class.java))
        }
    }
}