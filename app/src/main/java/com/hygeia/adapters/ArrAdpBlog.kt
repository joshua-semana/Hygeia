package com.hygeia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hygeia.R
import com.hygeia.classes.DataBlog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ArrAdpBlog(
    private val listBlogs: ArrayList<DataBlog>,
    private val clickListener: OnBlogItemClickListener
) : RecyclerView.Adapter<ArrAdpBlog.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMMM d, y", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.lblListBlogTitle)
        val badge: TextView = itemView.findViewById(R.id.badge)
        val content: TextView = itemView.findViewById(R.id.lblListBlogContent)
        val date: TextView = itemView.findViewById(R.id.lblListBlogDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_blog, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listBlogs.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(listBlogs[position]) {

                when (isVisible) {
                    true -> {
                        when (Type) {
                            "Announcement" -> badge.setBackgroundResource(R.drawable.badge_yellow)
                            "Information" -> badge.setBackgroundResource(R.drawable.badge_green)
                            "Update" -> badge.setBackgroundResource(R.drawable.badge_blue)
                        }
                    }
                    else -> {
                        badge.setBackgroundResource(R.drawable.badge_gray)
                    }
                }

                title.text = Title
                content.text = Content

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
                clickListener.onBlogItemClick(listBlogs[position].BlogID!!)
            }

            itemView.setOnLongClickListener {
                clickListener.onBlogItemLongClick(listBlogs[position].BlogID!!)
                true
            }

        }
    }

    interface OnBlogItemClickListener {
        fun onBlogItemClick(ID: String)
        fun onBlogItemLongClick(ID: String)
    }
}