package com.example.hackathonfitmakers

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

/**
 * Esta actividad sirve para reproducir los videos en pantalla completa.
 * Se pone en horizontal y oculta las barras del sistema para mejor visualización.
 */
class FullscreenVideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_video)

        // Ocultamos las barras de arriba y abajo para que el video ocupe toda la pantalla de verdad
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        // Buscamos el reproductor de video en el diseño
        val videoView = findViewById<VideoView>(R.id.vvFullscreen)
        // Recuperamos la dirección del video que nos pasaron desde la pantalla anterior
        val videoUrl = intent.getStringExtra("VIDEO_URL")

        if (videoUrl != null) {
            // Preparamos el video para reproducirse
            val uri = Uri.parse(videoUrl)
            videoView.setVideoURI(uri)

            // Añadimos los controles de play/pausa
            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            // ¡Acción! Empezamos a reproducir
            videoView.start()
        }

        // Configuración del botón "X" para cerrar la pantalla completa
        findViewById<android.view.View>(R.id.btnCloseFullscreen).setOnClickListener {
            // Cerramos esta pantalla y volvemos a la anterior
            finish()
        }
    }
}
