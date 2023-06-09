package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.R
import com.hygeia.classes.DataHistory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ArrAdpHistory(
    private val listHistory: ArrayList<DataHistory>
) : RecyclerView.Adapter<ArrAdpHistory.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMMM d, y", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.lblListTitle)
        val content: TextView = itemView.findViewById(R.id.lblListContent)
        val date: TextView = itemView.findViewById(R.id.lblListDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_history, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listHistory.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(listHistory[position]) {

                title.text = "$Type by $FullName"
                content.text = Content

                val dataDate = dateFormat.format(DateCreated!!.toDate())
                val todayDate = dateFormat.format(Calendar.getInstance().time)
                val yesterdayDate = Calendar.getInstance()
                yesterdayDate.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayDateFormat = dateFormat.format(yesterdayDate.time)
                val formatTime = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeString = formatTime.format(DateCreated.toDate())

                when (dataDate) {
                    todayDate -> date.text = "Today at $timeString"
                    yesterdayDateFormat -> date.text = "Yesterday"
                    else -> date.text = dataDate
                }
            }
        }
    }
}