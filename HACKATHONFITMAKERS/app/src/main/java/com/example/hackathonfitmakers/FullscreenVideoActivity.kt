package com.example.hackathonfitmakers

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class FullscreenVideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_video)

        // Ocultar barras de sistema para inmersión total (pantalla completa real)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        val videoView = findViewById<VideoView>(R.id.vvFullscreen)
        val videoUrl = intent.getStringExtra("VIDEO_URL")

        if (videoUrl != null) {
            val uri = Uri.parse(videoUrl)
            videoView.setVideoURI(uri)

            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            videoView.start()
        }

        findViewById<android.view.View>(R.id.btnCloseFullscreen).setOnClickListener {
            finish()
        }
    }
}
