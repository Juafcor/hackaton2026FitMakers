package com.example.hackathonfitmakers

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hackathonfitmakers.utils.FirestoreHelper

class IndividualActivity : AppCompatActivity() {

    private lateinit var rvExercises: RecyclerView

    // Variables para los textos de los días
    private lateinit var tvMon: TextView
    private lateinit var tvTue: TextView
    private lateinit var tvWed: TextView
    private lateinit var tvThu: TextView
    private lateinit var tvFri: TextView

    // Lista para guardar todos los días juntos
    private lateinit var dayViews: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual)

        // 1. Preparamos la lista de ejercicios
        rvExercises = findViewById(R.id.rvExercises)
        rvExercises.layoutManager = LinearLayoutManager(this)

        // 2. Buscamos los textos de los días
        tvMon = findViewById(R.id.tvDayMon)
        tvTue = findViewById(R.id.tvDayTue)
        tvWed = findViewById(R.id.tvDayWed)
        tvThu = findViewById(R.id.tvDayThu)
        tvFri = findViewById(R.id.tvDayFri)

        dayViews = listOf(tvMon, tvTue, tvWed, tvThu, tvFri)

        // Boton volver
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Setup the listeners and load default day immediately
        setupDayListeners()
        selectDay(tvMon, "Lunes")
    }

    private fun setupDayListeners() {
        tvMon.setOnClickListener { selectDay(tvMon, "Lunes") }
        tvTue.setOnClickListener { selectDay(tvTue, "Martes") }
        tvWed.setOnClickListener { selectDay(tvWed, "Miercoles") }
        tvThu.setOnClickListener { selectDay(tvThu, "Jueves") }
        tvFri.setOnClickListener { selectDay(tvFri, "Viernes") }
    }

    // Función que marca el día elegido y carga sus ejercicios
    private fun selectDay(selectedView: TextView, dayCode: String) {
        // Ponemos todos los días normal (sin color de fondo)
        val darkBlue = ContextCompat.getColor(this, R.color.dark_blue)

        for (view in dayViews) {
            view.setBackgroundColor(Color.TRANSPARENT)
            view.setTextColor(darkBlue)
        }

        // Al día seleccionado le ponemos estilo naranja y texto blanco
        selectedView.setBackgroundResource(R.drawable.bg_input_rounded)
        selectedView.background.setTint(ContextCompat.getColor(this, R.color.orange))
        selectedView.setTextColor(Color.WHITE)

        // Mostramos la rutina de ese día
        cargarRutinaPorDia(dayCode)
    }

    private fun cargarRutinaPorDia(dia: String) {
        // Fetch up to 3 exercises for the day
        FirestoreHelper.getExercisesForDay(dia, onSuccess = { exercisesData ->
            val listaEjercicios = mutableListOf<Exercise>()
            
            // Available local videos for exercises
            val availableVideos = listOf(
                R.raw.ejemplo_video, 
                R.raw.video_martes
            )

            for ((index, data) in exercisesData.withIndex()) {
                 val title = data["titulo"] as? String ?: "Exercise"
                 val desc = data["descripcion"] as? String ?: ""
                 
                 // Assign video round-robin style
                 val videoResId = availableVideos[index % availableVideos.size]
                 
                 listaEjercicios.add(Exercise(title, desc, videoResId))
            }

            val exerciseAdapter = ExerciseAdapter(listaEjercicios) { exercise ->
                showVideoPopup(exercise)
            }
            rvExercises.adapter = exerciseAdapter

            if (listaEjercicios.isEmpty()) {
                // If fetching Lunes fails (likely first time), offer to seed data
                if (dia == "Lunes") {
                   AlertDialog.Builder(this)
                       .setTitle("No hay rutinas")
                       .setMessage("La base de datos de ejercicios parece vacía. ¿Quieres generar rutinas de prueba para toda la semana?")
                       .setPositiveButton("Sí, Generar") { _, _ ->
                           Toast.makeText(this, "Generando ejercicios...", Toast.LENGTH_SHORT).show()
                           FirestoreHelper.seedExercises(
                               onSuccess = {
                                   Toast.makeText(this, "¡Datos generados! Recargando...", Toast.LENGTH_SHORT).show()
                                   cargarRutinaPorDia(dia)
                               },
                               onFailure = { error ->
                                   Toast.makeText(this, "Error generando datos: $error", Toast.LENGTH_SHORT).show()
                               }
                           )
                       }
                       .setNegativeButton("No", null)
                       .show()
                } else {
                    Toast.makeText(this, "No routine found for $dia", Toast.LENGTH_SHORT).show()
                }
            }
        }, onFailure = { e ->
            Toast.makeText(this, "Error loading exercises: $e", Toast.LENGTH_SHORT).show()
        })
    }

    private fun showVideoPopup(exercise: Exercise) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_video_player, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvPopupTitle)
        val videoView = dialogView.findViewById<VideoView>(R.id.vvPopup)
        val btnClose = dialogView.findViewById<Button>(R.id.btnClosePopup)

        tvTitle.text = exercise.title

        try {
            val videoPath = "android.resource://" + packageName + "/" + exercise.videoResId
            val uri = Uri.parse(videoPath)
            videoView.setVideoURI(uri)

            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            videoView.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Error video: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}