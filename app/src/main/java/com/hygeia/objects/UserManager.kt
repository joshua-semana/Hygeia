package com.hygeia.objects

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserManager {

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
    var points: Any? = null
    var isEnabled: Boolean? = null
    var isOnline: Boolean? = null
    var walletBackground: String? = null

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
            points = get("points")
            isEnabled = get("isEnabled") as Boolean
            isOnline = get("isOnline") as Boolean
            walletBackground = get("walletBackground") as String
        }
    }

    fun updateUserBalance(number: DocumentSnapshot) {
        balance = number.get("balance")
        points = number.get("points")
    }

    fun updateUserBackground(image: String) {
        walletBackground = image
    }
}