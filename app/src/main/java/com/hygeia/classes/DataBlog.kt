package com.hygeia.classes

import com.google.firebase.Timestamp

data class DataBlog(
    val BlogID: String? = null,
    val Content: String? = null,
    val DateCreated: Timestamp? = null,
    val FullName: String? = null,
    val Title: String? = null,
    val Type: String? = null,
    val isVisible: Boolean? = null
)