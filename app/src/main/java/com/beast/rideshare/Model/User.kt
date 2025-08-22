package com.beast.rideshare.Model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val gender: String = "", // "male" or "female"
    val femaleMode: Boolean = false // default: false
)
