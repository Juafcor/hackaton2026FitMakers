package com.example.hackathonfitmakers

/**
 * Modelo de datos para un Ejercicio.
 */
data class Exercise(
    val title: String,      // Nombre del ejercicio
    val description: String,// Explicación breve (series, repeticiones)
    val videoResId: Int,    // ID del recurso de video (R.raw.video)
    val imageResId: Int     // ID del recurso de imagen para la miniatura (R.drawable.imagen)
)