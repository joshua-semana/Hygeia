package com.hygeia.objects

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserManager {

    private var userRef = FirebaseFirestore.getInstance().collection("User")

    var uid: String? = null
    var balance: Any? = null
    var birthdate: String? = null
    var email: String? = null
    var firstname: String? = null
    var gender: String? = null
    var lastname: String? = null
    var password: String? = null
    var phoneNumber: String? = null
    var role: String? = null
    var status: String? = null

    fun setUserInformation(userInfo: DocumentSnapshot) {
        with(userInfo) {
            uid = id
            balance = get("balance")
            birthdate = get("birthdate") as String
            email = get("email") as String
            firstname = get("firstname") as String
            gender = get("gender") as String
            lastname = get("lastname") as String
            password = get("password") as String
            phoneNumber = get("phoneNumber") as String
            role = get("role") as String
            status = get("status") as String
        }
    }
    fun updateUserBalance(number : DocumentSnapshot){
        balance = number.get("balance")
    }
//
//    suspend fun getWalletBalance() {
//        val query = userRef.document(uid!!).get().await()
//        balance = query.get("balance")
//    }
}