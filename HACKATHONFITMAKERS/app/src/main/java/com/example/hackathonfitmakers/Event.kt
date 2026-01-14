package com.example.hackathonfitmakers

data class Event(
    val id: Int, // Número único del evento
    val title: String,
    val date: String,
    val location: String,
    val description: String,
    val imageResId: Int, // Imagen del evento
    var isJoined: Boolean = false // Si estoy apuntado o no
)