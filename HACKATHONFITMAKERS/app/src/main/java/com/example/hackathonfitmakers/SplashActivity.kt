package com.example.hackathonfitmakers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoViewSplash)

        // Construimos la ruta del video (logo_animado)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.logo_animado
        val uri = Uri.parse(videoPath)
        videoView.setVideoURI(uri)

        // Cuando el video termine, vamos al Login
        videoView.setOnCompletionListener {
            goToLogin()
        }

        // Si hay un error (ej. codec), vamos al Login directamente sin crashear
        videoView.setOnErrorListener { _, _, _ ->
            goToLogin()
            true
        }

        // Iniciamos el video
        videoView.start()
    }

    private fun goToLogin() {
        if (!isFinishing) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Transición suave: Fade In (Login) y Fade Out (Splash)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
