package com.hygeia.classes

import com.google.firebase.Timestamp

data class DataTransactions (
    val TransactionID : String? = null,
    val Amount : Any? = null,
    val DateCreated : Timestamp? = null,
    val Number : String? = null,
    val ReferenceNumber : String? = null,
    val Type : String? = null
)
