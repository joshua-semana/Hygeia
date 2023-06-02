package com.hygeia

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.hygeia.adapters.ArrAdpMachines
import com.hygeia.classes.DataMachines
import com.hygeia.databinding.FrgAdminToolsBinding
import com.hygeia.objects.Utilities.dlgError

class FrgAdminTools : Fragment() {
    private lateinit var bind : FrgAdminToolsBinding
    private lateinit var adapter : ArrAdpMachines
    private lateinit var auth: FirebaseAuth

    private var db = FirebaseFirestore.getInstance()
    private var machinesRef = db.collection("Machines")
    private lateinit var listener: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FrgAdminToolsBinding.inflate(layoutInflater, container, false)

        with(bind) {
            listMachines.setHasFixedSize(true)
            listMachines.layoutManager = LinearLayoutManager(context)
            listMachines.adapter = ArrAdpMachines(emptyList())
            getListOfMachines()
            return root
        }
    }

    private fun getListOfMachines() {
        listener = machinesRef.addSnapshotListener { snapshot, ex ->
            if (ex != null) {
                dlgError(requireContext(), ex.message.toString())
                return@addSnapshotListener
            }

            val machinesList = mutableListOf<DataMachines>()
            for (document in snapshot?.documents ?: emptyList()) {
                val name = document.getString("Name") ?: ""
                val location = document.getString("Location") ?: ""
                val machine = DataMachines(name, location)
                machinesList.add(machine)
            }

            //adapter.setData(machinesList)
            bind.listMachines.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener.remove()
    }
}