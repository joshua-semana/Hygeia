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
import com.hygeia.classes.DataProductAdmin
import com.hygeia.objects.Utilities.formatNumber

class ArrAdpProductAdmin(
    private val listProducts: ArrayList<DataProductAdmin>,
    private val clickListener: OnProductEditItemClickListener
    ) : RecyclerView.Adapter<ArrAdpProductAdmin.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameAndPrice : TextView = itemView.findViewById(R.id.lblListProductAdminNameAndPrice)
        val quantity : TextView = itemView.findViewById(R.id.lblListProductAdminQuantity)
        val slot : TextView = itemView.findViewById(R.id.lblListProductAdminSlot)
        val image : ImageView = itemView.findViewById(R.id.imgListProductAdmin)
        val btnEdit : ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnPoints : ImageButton = itemView.findViewById(R.id.btnPoints)
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
                quantity.text = "$Quantity item(s) available"
                slot.text = "Vendo Slot: $Slot"

                if (Status == 0) {
                    image.setImageResource(R.drawable.ic_product_disabled)
                    image.setBackgroundResource(R.drawable.bg_circle_300)
                } else {
                    if (Quantity == "0") {
                        image.setImageResource(R.drawable.ic_product_empty)
                        image.setBackgroundResource(R.drawable.bg_circle_danger_50)
                    } else {
                        image.setImageResource(R.drawable.ic_product)
                        image.setBackgroundResource(R.drawable.bg_circle_50)
                    }
                }

                btnEdit.setOnClickListener {
                    clickListener.onProductEditItemClick(ID!!)
                }

                btnPoints.setOnClickListener {
                    clickListener.onProductEditPointsClick(ID!!)
                }
            }
        }
    }

    interface OnProductEditItemClickListener {
        fun onProductEditItemClick(productID: String)
        fun onProductEditPointsClick(productID: String)
    }
}