package com.hygeia.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.classes.DataMachines
import com.hygeia.databinding.ListMachineBinding

class ArrAdpMachines(private val list: List<DataMachines>) :
    RecyclerView.Adapter<ArrAdpMachines.ViewHolder>() {

    inner class ViewHolder(val bind: ListMachineBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val bind = ListMachineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(bind)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                bind.lblMachineName.text = this.name
                bind.lblMachineLocation.text = this.location
            }
        }
    }

//    interface ArrAdpMachinesClicksInterface {
//
//    }
}