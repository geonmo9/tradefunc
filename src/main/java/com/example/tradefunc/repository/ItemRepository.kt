package com.example.tradefunc.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.tradefunc.data.Item

class ItemRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getItems(onSuccess: (List<Item>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("items")
            .get()
            .addOnSuccessListener { result ->
                val itemList = result.map { it.toObject(Item::class.java).copy(id = it.id) }
                onSuccess(itemList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
