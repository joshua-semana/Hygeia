package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.ActPurchase
import com.hygeia.R
import com.hygeia.classes.DataProducts
import com.hygeia.objects.Utilities.formatNumber

class ArrAdpProducts(
    private val listProducts: ArrayList<DataProducts>,
    private val clickListener: ActPurchase // either ActPurchase or ActPurchaseUsingPoints
    ) : RecyclerView.Adapter<ArrAdpProducts.ViewHolder>() {

    private var totalPrice: Double = 0.00

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
        return listProducts.count { it.Quantity!!.toDouble() > 0 && it.Status != 0}
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = getNonZeroQuantityProduct(position)
        if (product != null) {
            with(holder) {
                with(product) {
                    nameAndPrice.text = "$Name (${formatNumber(Price?.toDouble())})"
                    quantity.text = "$Quantity items available"
                    count.text = "$Count"

                    btnAdd.setOnClickListener {
                        if (Count < Quantity!!.toInt()) {
                            Count++
                            count.text = "$Count"
                            totalPrice += Price!!.toDouble()
                        }
                        clickListener.onAddOrMinusClick(totalPrice)
                    }

                    btnMinus.setOnClickListener {
                        if (Count != 0) {
                            Count--
                            count.text = "$Count"
                            totalPrice -= Price!!.toDouble()
                        }
                        clickListener.onAddOrMinusClick(totalPrice)
                    }
                }
            }
        }
    }

    fun getNonZeroQuantityProduct(position: Int): DataProducts? {
        var count = 0
        for (product in listProducts) {
            if (product.Quantity!!.toDouble() > 0 && product.Status != 0) {
                if (count == position) {
                    return product
                }
                count++
            }
        }
        return null
    }

    interface OnProductItemClickListener {
        fun onAddOrMinusClick(productPrice: Double)
    }

}