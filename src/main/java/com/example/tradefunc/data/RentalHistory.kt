package com.example.tradefunc.data

import com.google.firebase.Timestamp
import java.util.Date

data class RentalHistory(
    val userId: String = "",
    val itemId: String = "",
    val itemName: String = "",
    val rentalDate: Timestamp = Timestamp.now(),
    val returnDate: Timestamp? = null
)
