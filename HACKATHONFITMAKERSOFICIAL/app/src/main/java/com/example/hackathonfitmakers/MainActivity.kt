package com.example.hackathonfitmakers

import android.content.Intent
import android.net.Uri // Para abrir enlaces web
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Esta es la pantalla principal de la aplicación.
 * Desde aquí podemos navegar a las diferentes secciones:
 * Plan Individual, Comunidad, Entrenador IA y la Web.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Aquí buscamos los botones y elementos de la pantalla para poder usarlos
        val btnIndividual = findViewById<android.view.View>(R.id.btnIndividual)
        val btnCommunity = findViewById<android.view.View>(R.id.btnCommunity)
        val btnAI = findViewById<android.view.View>(R.id.btnAI)
        val btnWeb = findViewById<android.view.View>(R.id.btnWeb)

        // Configuración del botón para ir al plan de ejercicios individual
        btnIndividual.setOnClickListener {
            // Abrimos la actividad de Plan Individual
            startActivity(Intent(this, IndividualActivity::class.java))
        }

        // Configuración del botón para ir a la sección de Comunidad
        btnCommunity.setOnClickListener {
            // Abrimos la actividad de Comunidad
            startActivity(Intent(this, CommunityActivity::class.java))
        }

        // Configuración del botón para hablar con nuestro asistente IA
        btnAI.setOnClickListener {
            // Abrimos la pantalla del chat con Inteligencia Artificial
            startActivity(Intent(this, IaActivity::class.java))
        }

        // Cargamos la animación del robot usando la librería Glide para que se mueva
        val ivAiLogo = findViewById<android.widget.ImageView>(R.id.ivAiLogo)
        com.bumptech.glide.Glide.with(this).load(R.drawable.robot_animacion).into(ivAiLogo)
        
        // Configuración del botón para visitar la página web
        btnWeb.setOnClickListener {
            // Definimos la dirección web a la que queremos ir
            val url = "https://web.mmolinero.cloud/"

            // Creamos una acción para ver esa dirección en el navegador
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}