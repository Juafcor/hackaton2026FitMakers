package com.example.hackathonfitmakers

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.VideoView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class IndividualActivity : AppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual)

        // 1. Configurar Spinner
        val spinner = findViewById<Spinner>(R.id.spinnerDays)
        val dias = arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dias)
        spinner.adapter = adapter

        // 2. Configurar Videos
        setupVideoPlayer(R.id.videoEx1, R.id.ivPlayIcon1)
        setupVideoPlayer(R.id.videoEx2, R.id.ivPlayIcon2)
        setupVideoPlayer(R.id.videoEx3, R.id.ivPlayIcon3)
    }

    // Función auxiliar para no repetir código
    private fun setupVideoPlayer(videoViewId: Int, playIconId: Int) {
        val videoView = findViewById<VideoView>(videoViewId)
        val playIcon = findViewById<ImageView>(playIconId)

        playIcon.setOnClickListener {
            Toast.makeText(this, "Reproduciendo ejercicio...", Toast.LENGTH_SHORT).show()
            playIcon.visibility = View.GONE
            // videoView.start() // Descomentar si tuvieras un video real
        }

        videoView.setOnClickListener {
            playIcon.visibility = View.VISIBLE
            // videoView.pause()
        }
    }
}