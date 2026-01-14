package com.example.hackathonfitmakers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnIndividual = findViewById<Button>(R.id.btnIndividual)
        val btnCommunity = findViewById<Button>(R.id.btnCommunity)
        val btnAI = findViewById<Button>(R.id.btnAI)

        // Ir a Rutina Individual
        btnIndividual.setOnClickListener {
            startActivity(Intent(this, IndividualActivity::class.java))
        }

        // Ir a Comunidad
        btnCommunity.setOnClickListener {
            startActivity(Intent(this, CommunityActivity::class.java))
        }

        // Ir a IA
        btnAI.setOnClickListener {
            startActivity(Intent(this, IaActivity::class.java))
        }
    }
}