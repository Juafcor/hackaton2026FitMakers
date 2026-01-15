package com.example.hackathonfitmakers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

/**
 * Esta es la pantalla de carga (Splash) que se muestra al abrir la app.
 * Reproduce un video de introducción y luego pasa a la pantalla de Login.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Nos aseguramos de que la app se vea siempre en modo claro (fondo blanco), ignorando el modo noche del móvil
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoViewSplash)

        // Preparamos la ruta donde está guardado el video del logo animado
        val videoPath = "android.resource://" + packageName + "/" + R.raw.logo_animado
        val uri = Uri.parse(videoPath)
        videoView.setVideoURI(uri)

        // Configuramos qué hacer cuando el video termina: ir al login
        videoView.setOnCompletionListener {
            goToLogin()
        }

        // Si por alguna razón el video falla, no dejamos la app colgada, vamos al login directamente
        videoView.setOnErrorListener { _, _, _ ->
            goToLogin()
            true
        }

        // ¡Empezamos el video!
        videoView.start()
    }

    /**
     * Esta función se encarga de cambiar de pantalla hacia el Login.
     * También añade una transición suave para que quede bonito.
     */
    private fun goToLogin() {
        if (!isFinishing) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Hacemos que la pantalla nueva aparezca suavemente y la vieja se desvanezca
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
