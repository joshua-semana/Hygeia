package com.hygeia.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.hygeia.R
import com.hygeia.models.Transaction

class ArrAdpTransactions (context: Context, resource: Int, items: ArrayList<Transaction>)
    : ArrayAdapter<Transaction>(context, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_transaction, parent, false)
        }

        // Get the current transaction from the array
        val currentTransaction = getItem(position)

        // Populate the views in the custom list item layout with data from the current transaction
//        val imageView = .findViewById<ImageView>(R.id.transaction_image)
//        imageView.setImageResource(currentTransaction.imageResourceId)
//
//        val titleView = itemView.findViewById<TextView>(R.id.transaction_title)
//        titleView.text = currentTransaction.title
//
//        val dateView = itemView.findViewById<TextView>(R.id.transaction_date)
//        dateView.text = currentTransaction.date
//
//        val amountView = itemView.findViewById<TextView>(R.id.transaction_amount)
//        amountView.text = currentTransaction.amount.toString()

        return itemView!!
    }
}