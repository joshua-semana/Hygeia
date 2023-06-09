package com.hygeia.classes

import com.google.firebase.Timestamp

data class DataHistory(
    val ID: String? = null,
    val Content: String? = null,
    val DateCreated: Timestamp? = null,
    val FullName: String? = null,
    val MachineName: String? = null,
    val Type: String? = null
)