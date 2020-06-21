package com.example.contactlistwithheader.model

import com.google.gson.annotations.SerializedName

data class Contact(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val email: String,
    val address: String,
    val phone: String,
    val organization: String
)