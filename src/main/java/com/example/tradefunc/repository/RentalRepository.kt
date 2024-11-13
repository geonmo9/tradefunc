package com.example.tradefunc.repository

import android.content.Context
import android.widget.Toast
import com.example.tradefunc.data.RentalHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class RentalRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    // 대여 이력 추가
    fun addRentalHistory(
        userId: String,
        itemId: String,
        itemName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val rentalHistory = RentalHistory(
            userId = userId,
            itemId = itemId,
            itemName = itemName,
            rentalDate = Timestamp.now(),
            returnDate = null
        )

        db.collection("rentalHistory")
            .add(rentalHistory)
            .addOnSuccessListener {
                onSuccess()
                Toast.makeText(context, "Rental added successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to add rental: ${exception.message}", Toast.LENGTH_SHORT).show()
                onFailure(exception)
            }
    }

    // 아이템 대여 상태 변경
    fun updateItemAvailability(
        itemId: String,
        available: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("items")
            .document(itemId)
            .update("available", available)
            .addOnSuccessListener {
                onSuccess()
                Toast.makeText(context, "Item availability updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to update: ${exception.message}", Toast.LENGTH_SHORT).show()
                onFailure(exception)
            }
    }

    // 사용자 대여 이력 가져오기
    fun getUserRentalHistory(
        userId: String,
        onSuccess: (List<RentalHistory>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("rentalHistory")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val historyList = result.documents.mapNotNull { it.toObject(RentalHistory::class.java) }
                onSuccess(historyList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to get history: ${exception.message}", Toast.LENGTH_SHORT).show()
                onFailure(exception)
            }
    }
}
