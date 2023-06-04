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
import com.hygeia.classes.DataTransactions
import com.hygeia.objects.Utilities

class ArrAdpTransactions(
    private val listTransactions: ArrayList<DataTransactions>,
    private val clickListener: OnTransactionItemClickListener
) : RecyclerView.Adapter<ArrAdpTransactions.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imgListTransaction)
        val type: TextView = itemView.findViewById(R.id.lblListTransactionType)
        val number: TextView = itemView.findViewById(R.id.lblListTransactionNumber)
        val date: TextView = itemView.findViewById(R.id.lblListTransactionDate)
        val amount: TextView = itemView.findViewById(R.id.lblListTransactionAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_transaction, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listTransactions.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(listTransactions[position]) {
                if (Type == "Send Money") {
                    image.setImageResource(R.drawable.ic_money_send)
                    type.text = "Send money to"
                    amount.text = "- ${Utilities.formatNumber(Amount)}"
                } else if (Type == "Receive Money") {
                    image.setImageResource(R.drawable.ic_money_receive)
                    type.text = "Receive money from"
                    amount.text = "+ ${Utilities.formatNumber(Amount)}"
                } else if (Type == "Purchase") {
                    image.setImageResource(R.drawable.ic_product)
                    type.text = "Purchase items"
                    amount.text = "- ${Utilities.formatNumber(Amount)}"
                }

                number.text = Number
            }

            itemView.setOnClickListener {
                clickListener.onTransactionItemClick(listTransactions[position].TransactionID.toString())
            }
        }
    }

    interface OnTransactionItemClickListener {
        fun onTransactionItemClick(ID: String)
    }
}