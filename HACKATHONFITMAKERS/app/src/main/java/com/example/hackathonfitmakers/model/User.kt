package com.example.hackathonfitmakers.model

data class User(
    val dni: String = "",
    val nombre: String = "",
    val contrasena: String = "",
    val peso: Int = 0,
    val altura: Int = 0,
    val sexo: List<String> = emptyList(),
    val patologias: List<String> = emptyList(),
    val residencia: Map<String, Any> = emptyMap(),
    val voz: List<Float> = emptyList()
)
