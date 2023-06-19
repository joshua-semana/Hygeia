package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.R
import com.hygeia.classes.DataBlog
import com.hygeia.classes.DataVoucher
import com.hygeia.objects.UserManager
import com.hygeia.objects.Utilities
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ArrAdpVoucher(
    private val listBlogs: ArrayList<DataVoucher>,
    private val clickListener: OnItemClickListener
) : RecyclerView.Adapter<ArrAdpVoucher.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMMM d, y", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.lblTitle)
        val badge: TextView = itemView.findViewById(R.id.badge)
        val content: TextView = itemView.findViewById(R.id.lblContent)
        val date: TextView = itemView.findViewById(R.id.lblDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_voucher, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listBlogs.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(listBlogs[position]) {

                val amount = Utilities.formatPoints(Amount.toString().toDouble())

                when (Type) {
                    "Create" -> {
                        title.text = "Created $amount voucher"
                        content.text = "Code is: $Code"
                        badge.setBackgroundResource(R.drawable.badge_green)

                        itemView.setOnLongClickListener {
                            clickListener.onItemLongClick(listBlogs[position].ID!!,
                                listBlogs[position].Amount.toString().toDouble()
                            )
                            true
                        }
                    }
                    "Redeem" -> {
                        badge.setBackgroundResource(R.drawable.badge_blue)
                        when (UserManager.role) {
                            "admin" -> {
                                title.text = "$amount voucher redeemed"
                                content.text = "Redeemed by: $RedeemerName"
                            }
                            "standard" -> {
                                title.text = "Voucher claimed"
                                content.text = "You have successfully claimed $amount voucher."
                            }
                         }

                    }
                    "Cancel" -> {
                        title.text = "$amount voucher cancelled"
                        content.text = "Cancelled code is: $Code"
                        badge.setBackgroundResource(R.drawable.badge_gray)
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
        }
    }

    interface OnItemClickListener {
        fun onItemLongClick(ID: String, amount: Double)
    }
}