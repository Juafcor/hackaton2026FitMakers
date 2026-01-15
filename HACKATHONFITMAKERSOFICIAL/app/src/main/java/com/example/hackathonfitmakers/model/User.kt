package com.example.hackathonfitmakers.model

/**
 * Modelo de datos que representa a un Usuario en la base de datos (Firestore).
 */
data class User(
    val dni: String = "",              // Documento de identidad (ID principal)
    val nombre: String = "",           // Nombre completo
    val contrasena: String = "",       // Contraseña de acceso
    val peso: Int = 0,                 // Peso en kg
    val altura: Int = 0,               // Altura en cm
    val sexo: List<String> = emptyList(), // Sexo (Hombre/Mujer) guardado como lista por diseño previo
    val patologias: List<String> = emptyList(), // Lista de zonas lesionadas o patologías
    val residencia: Map<String, Any> = emptyMap(), // Datos de dirección (Calle, Num, CP)
    val voz: List<Float> = emptyList() // Vector biométrico de la voz (para inicio de sesión por voz)
)
