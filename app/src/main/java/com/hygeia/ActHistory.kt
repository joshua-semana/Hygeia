package com.hygeia

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hygeia.adapters.ArrAdpHistory
import com.hygeia.classes.DataHistory
import com.hygeia.databinding.ActHistoryBinding
import com.hygeia.objects.MachineManager
import com.hygeia.objects.UserManager

class ActHistory : AppCompatActivity() {

    private lateinit var bind: ActHistoryBinding
    private lateinit var listOfHistory: ArrayList<DataHistory>

    private var db = FirebaseFirestore.getInstance()
    private var historyRef = db.collection("History")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActHistoryBinding.inflate(layoutInflater)
        setContentView(bind.root)

        UserManager.isOnAnotherActivity = true
        UserManager.setUserOnline()
        getListOfHistory()

        bind.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getListOfHistory() {
        bind.cover.visibility = View.VISIBLE

        bind.lblMessage.text = "Fetching record of activities for this machine, please wait..."
        bind.loading.visibility = View.VISIBLE
        bind.listView.layoutManager = LinearLayoutManager(this@ActHistory)
        listOfHistory = arrayListOf()

        val query = historyRef.whereEqualTo("Machine ID", MachineManager.machineId)
            .orderBy("Date Created", Query.Direction.DESCENDING)

        query.get().apply {
            addOnSuccessListener { data ->
                listOfHistory.clear()
                if (!data.isEmpty) {
                    bind.cover.visibility = View.GONE
                    for (item in data.documents) {
                        val items = DataHistory(
                            item.id,
                            item.get("Content").toString(),
                            item.get("Date Created") as Timestamp,
                            item.get("Full Name").toString(),
                            item.get("Machine Name").toString(),
                            item.get("Type").toString()
                        )
                        listOfHistory.add(items)
                    }
                    bind.listView.adapter = ArrAdpHistory(listOfHistory)
                } else {
                    bind.lblMessage.text = "There are no activities for this machine."
                    bind.loading.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        UserManager.setUserOnline()
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        } else {
            if (UserManager.isOnAnotherActivity) UserManager.setUserOffline()
        }
    }
}