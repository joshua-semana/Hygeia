package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.R
import com.hygeia.classes.DataTransactions
import com.hygeia.objects.Utilities
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ArrAdpTransactions(
    private val listTransactions: ArrayList<DataTransactions>,
    private val clickListener: OnTransactionItemClickListener
) : RecyclerView.Adapter<ArrAdpTransactions.ViewHolder>() {

    private val dateFormat =  SimpleDateFormat("MMMM d, y", Locale.getDefault())

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
                when (Type) {
                    "Send Money" -> {
                        image.setImageResource(R.drawable.ic_money_send)
                        type.text = "Send money to"
                        amount.text = "- ${Utilities.formatCredits(Amount)}"
                        number.text = Number
                    }
                    "Receive Money" -> {
                        image.setImageResource(R.drawable.ic_money_receive)
                        type.text = "Receive money from"
                        if (Number == "+639088651408") {
                            number.text = "Hygeia Admin"
                        } else {
                            number.text = Number
                        }
                        amount.text = "+ ${Utilities.formatCredits(Amount)}"
                    }
                    "Purchase" -> {
                        image.setImageResource(R.drawable.ic_buy)
                        amount.text = "- ${Utilities.formatCredits(Amount)}"
                        if (Number == "1") {
                            number.text = "$Number product"
                            type.text = "Purchase item"
                        } else {
                            number.text = "$Number products"
                            type.text = "Purchase items"
                        }
                    }
                    "Purchase Using Star" -> {
                        image.setImageResource(R.drawable.ic_use_points)
                        amount.text = "- 🌟${Utilities.formatPoints(Amount)}"
                        if (Number == "1") {
                            number.text = "$Number product"
                            type.text = "Purchase item using stars"
                        } else {
                            number.text = "$Number products"
                            type.text = "Purchase items using stars"
                        }
                    }
                    "Request" -> {
                        image.setImageResource(R.drawable.ic_money_request)
                        amount.text = "+ ${Utilities.formatCredits(Amount)}"
                        number.text = "Requested money"
                        type.visibility = View.GONE
                    }
                }

                val dataDate = dateFormat.format(DateCreated!!.toDate())
                val todayDate = dateFormat.format(Calendar.getInstance().time)
                val yesterdayDate = Calendar.getInstance()
                yesterdayDate.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayDateFormat = dateFormat.format(yesterdayDate.time)
                val formatTime = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeString = formatTime.format(DateCreated.toDate())

                when (dataDate) {
                    todayDate -> {
                        date.text = "Today at $timeString"
                    }
                    yesterdayDateFormat -> {
                        date.text = "Yesterday"
                    }
                    else -> {
                        date.text = dataDate
                    }
                }
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