package com.example.hackathonfitmakers

data class Event(
    val id: Int, // Identificador único (útil para bases de datos)
    val title: String,
    val date: String,
    val location: String,
    val description: String,
    val imageResId: Int, // ID de la imagen (R.drawable...)
    var isJoined: Boolean = false // Estado: si el usuario está apuntado o no
)