package com.example.hackathonfitmakers

data class Exercise(
    val title: String,
    val description: String,
    val videoResId: Int // Usamos Int porque referenciamos R.raw.video
)