package com.example.hackathonfitmakers

import android.content.Intent
import android.net.Uri // Para abrir enlaces web
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a los botones
        // Buscamos las tarjetas del menú en la pantalla
        val btnIndividual = findViewById<android.view.View>(R.id.btnIndividual)
        val btnCommunity = findViewById<android.view.View>(R.id.btnCommunity)
        val btnAI = findViewById<android.view.View>(R.id.btnAI)
        val btnWeb = findViewById<android.view.View>(R.id.btnWeb)

        // Botón para ir al plan individual
        btnIndividual.setOnClickListener {
            startActivity(Intent(this, IndividualActivity::class.java))
        }

        // Botón para ir a la comunidad
        btnCommunity.setOnClickListener {
            startActivity(Intent(this, CommunityActivity::class.java))
        }

        // Botón para hablar con el entrenador IA
        btnAI.setOnClickListener {
            startActivity(Intent(this, IaActivity::class.java))
        }

        // Cargar GIF con Glide
        val ivAiLogo = findViewById<android.widget.ImageView>(R.id.ivAiLogo)
        com.bumptech.glide.Glide.with(this).load(R.drawable.robot_animacion).into(ivAiLogo)
        
        // Botón para abrir la web de ejercicios
        btnWeb.setOnClickListener {
            // Abrimos el navegador con la web
            val url = "https://www.tusitioweb.com/ejercicios"

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}