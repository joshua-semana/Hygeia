package com.hygeia.objects

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

object BlogManager {

    var uid: String? = null
    var content: String? = null
    var fullname: String? = null
    var title: String? = null
    var type: String? = null
    var isVisible: Boolean? = null
    var dateString: String? = null
    var timeString: String? = null


    fun setData(data: DocumentSnapshot) {
        uid = data.id
        content = data.get("Content") as String
        fullname = data.get("Full Name") as String
        title = data.get("Title") as String
        type = data.get("Type") as String
        isVisible = data.get("isVisible") as Boolean
        val timestamp: Timestamp = data.get("Date Created") as Timestamp
        val date: java.util.Date = timestamp.toDate()
        val formatDate = SimpleDateFormat("MMMM d, y", Locale.getDefault())
        dateString = formatDate.format(date)
        val formatTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        timeString = formatTime.format(date)
    }
}