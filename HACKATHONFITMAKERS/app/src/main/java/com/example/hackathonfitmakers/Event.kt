package com.example.hackathonfitmakers

data class Event(
    val id: String, // String ID for Firestore document ID
    val title: String,
    val date: String,
    val location: String,
    val description: String,
    val imageResId: Int, // Imagen del evento
    var isJoined: Boolean = false // Si estoy apuntado o no
)