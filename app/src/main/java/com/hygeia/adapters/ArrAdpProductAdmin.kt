package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.R
import com.hygeia.classes.DataProductAdmin
import com.hygeia.objects.Utilities.formatNumber

class ArrAdpProductAdmin(
    private val listProducts: ArrayList<DataProductAdmin>
    ) : RecyclerView.Adapter<ArrAdpProductAdmin.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameAndPrice : TextView = itemView.findViewById(R.id.lblListProductAdminNameAndPrice)
        val quantity : TextView = itemView.findViewById(R.id.lblListProductAdminQuantity)
        val slot : TextView = itemView.findViewById(R.id.lblListProductAdminSlot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_product_admin, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listProducts.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(listProducts[position]) {
                nameAndPrice.text = "$Name (${formatNumber(Price?.toDouble())})"
                quantity.text = "$Quantity items available"
                slot.text = "Vendo Slot: $Slot"
            }
        }
    }
}