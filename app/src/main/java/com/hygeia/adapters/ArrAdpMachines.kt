package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
        val btnEdit : ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnSales : ImageButton = itemView.findViewById(R.id.btnSales)
        val btnAdd : ImageButton = itemView.findViewById(R.id.btnAdd)
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
                if (MachineID.toString() == "New Machine") {
                    name.text = "Add new machine"
                    location.visibility = View.GONE
                    btnSales.visibility = View.GONE
                    btnEdit.visibility = View.GONE
                    btnAdd.visibility = View.VISIBLE
                    image.setImageResource(R.drawable.ic_machine_offline)
                    image.setBackgroundResource(R.drawable.bg_circle_300)

                    btnAdd.setOnClickListener {
                        clickListener.onMachineAddItemClick()
                    }
                } else {
                    btnAdd.visibility = View.GONE
                    name.text = "Vendo No. $Name"
                    location.text = Location
                    if (Status == "Online") {
                        image.setImageResource(R.drawable.ic_machine_online)
                        image.setBackgroundResource(R.drawable.bg_circle_50)
                    } else if (Status == "Offline") {
                        image.setImageResource(R.drawable.ic_machine_offline)
                        image.setBackgroundResource(R.drawable.bg_circle_300)
                    }

                    btnEdit.setOnClickListener {
                        clickListener.onMachineInventoryItemClick(MachineID!!)
                    }

                    btnSales.setOnClickListener {
                        clickListener.onMachineSalesItemClick(MachineID!!)
                    }
                }
            }
        }
    }

    interface OnMachineItemClickListener {
        fun onMachineInventoryItemClick(machineID: String)
        fun onMachineSalesItemClick(machineID: String)
        fun onMachineAddItemClick()
    }
}