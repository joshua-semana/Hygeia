package com.hygeia

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hygeia.adapters.ArrAdpBlog
import com.hygeia.adapters.ArrAdpHistory
import com.hygeia.classes.DataBlog
import com.hygeia.classes.DataHistory
import com.hygeia.databinding.ActHistoryBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.Utilities

class ActHistory : AppCompatActivity() {

    private lateinit var bind : ActHistoryBinding
    private lateinit var listOfHistory: ArrayList<DataHistory>
    private lateinit var loading: Dialog

    private var db = FirebaseFirestore.getInstance()
    private var historyRef = db.collection("History")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActHistoryBinding.inflate(layoutInflater)
        loading = Utilities.dlgLoading(this@ActHistory)
        setContentView(bind.root)

        getListOfHistory()
    }

    private fun getListOfHistory() {
        listOfHistory = arrayListOf()
        loading.show()
        bind.listView.layoutManager = LinearLayoutManager(this@ActHistory)

        val query = historyRef.whereEqualTo("Machine Name", MachineManager.name).orderBy("Date Created", Query.Direction.DESCENDING)

        query.get().apply {
            addOnSuccessListener { data ->
                bind.coverListView.visibility = View.GONE
                listOfHistory.clear()
                if (!data.isEmpty) {
                    for (item in data.documents) {
                        val blog = DataHistory(
                            item.id,
                            item.get("Content").toString(),
                            item.get("Date Created") as Timestamp,
                            item.get("Full Name").toString(),
                            item.get("Machine Name").toString(),
                            item.get("Type").toString()
                        )

                        listOfHistory.add(blog)
                    }
                    bind.listView.adapter = ArrAdpHistory(listOfHistory)
                } else {
                    bind.coverListView.visibility = View.VISIBLE
                }
                loading.dismiss()
            }
        }
    }
}