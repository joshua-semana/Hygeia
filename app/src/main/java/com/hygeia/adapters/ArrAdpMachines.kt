package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.R
import com.hygeia.classes.DataMachines

class ArrAdpMachines(
    private val listMachines: ArrayList<DataMachines>,
    private val clickListener: OnMachineItemClickListener
    ) : RecyclerView.Adapter<ArrAdpMachines.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name : TextView = itemView.findViewById(R.id.lblListMachineName)
        val location : TextView = itemView.findViewById(R.id.lblListMachineLocation)
        val image : ImageView = itemView.findViewById(R.id.imgListMachine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_machine, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listMachines.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(listMachines[position]) {
                name.text = "Vendo No. $Name"
                location.text = Location

                if (Status == "Online") {
                    image.setImageResource(R.drawable.ic_machine_online)
                    image.setBackgroundResource(R.drawable.bg_circle_50)
                } else if (Status == "Offline") {
                    image.setImageResource(R.drawable.ic_machine_offline)
                    image.setBackgroundResource(R.drawable.bg_circle_300)
                }

                itemView.setOnClickListener {
                    clickListener.onMachineItemClick(MachineID!!)
                }
            }
        }
    }

    interface OnMachineItemClickListener {
        fun onMachineItemClick(machineID: String)
    }
}