package com.hygeia.classes

import com.google.firebase.Timestamp

data class DataVoucher(
    val ID: String? = null,
    val Amount: Any? = null,
    val Code: String? = null,
    val DateCreated: Timestamp? = null,
    val RedeemedBy: String? = null,
    val RedeemerName: String? = null,
    val Type: String? = null,
)