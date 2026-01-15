package com.example.hackathonfitmakers

/**
 * Modelo simple para los mensajes del chat.
 */
data class Message(
    val content: String,   // El texto del mensaje
    val isUser: Boolean    // true si lo escribió el usuario, false si fue la IA
)
