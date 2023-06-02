package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.R
import com.hygeia.classes.DataProducts
import java.text.DecimalFormat

class ArrAdpProducts(
    private val listProducts: ArrayList<DataProducts>) : RecyclerView.Adapter<ArrAdpProducts.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameAndPrice : TextView = itemView.findViewById(R.id.lblListProductNameAndPrice)
        val quantity : TextView = itemView.findViewById(R.id.lblListProductQuantity)
        val btnAdd : ImageButton = itemView.findViewById(R.id.btnListAdd)
        val btnMinus : ImageButton = itemView.findViewById(R.id.btnListMinus)
        val count : TextView = itemView.findViewById(R.id.lblListProductCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_product, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listProducts.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(listProducts[position]) {
                nameAndPrice.text = "$Name (₱$Price.00)"
                quantity.text = "$Quantity items available"
                count.text = "$Count"

                btnAdd.setOnClickListener {
                    if (Count < Quantity!!.toInt()) {
                        Count++
                        count.text = "$Count"
                    }
                }

                btnMinus.setOnClickListener {
                    if (Count != 0) {
                        Count--
                        count.text = "$Count"
                    }
                }
            }
        }
    }
}