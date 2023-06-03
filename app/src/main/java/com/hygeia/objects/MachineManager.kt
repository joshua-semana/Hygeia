package com.hygeia.objects

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object MachineManager {

    var uid: String? = null
    var location: String? = null
    var name: String? = null
    var status: String? = null
    var userConnected: String? = null

    fun setMachineInformation(machineInfo: DocumentSnapshot) {
        with(machineInfo) {
            uid = id
            location = get("Location") as String?
            name = get("Name") as String?
            status = get("Status") as String?
            userConnected = get("User Connected") as String?
        }
    }
}