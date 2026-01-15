package com.example.hackathonfitmakers

/**
 * Modelo de datos para un Evento de la comunidad.
 */
data class Event(
    val id: String,         // Identificador único (ID del documento en Firebase)
    val title: String,      // Nombre del evento
    val date: String,       // Fecha y hora
    val location: String,   // Lugar donde se realiza
    val description: String,// Descripción de qué va
    val imageResId: Int,    // Imagen de fondo para la tarjeta
    var isJoined: Boolean = false // Indica si el usuario actual ya está apuntado
)