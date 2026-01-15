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

        // 3. Al pulsar un día, cambiamos la selección
        tvMon.setOnClickListener { selectDay(tvMon, "Lunes") }
        tvTue.setOnClickListener { selectDay(tvTue, "Martes") }
        tvWed.setOnClickListener { selectDay(tvWed, "Miercoles") }
        tvThu.setOnClickListener { selectDay(tvThu, "Jueves") }
        tvFri.setOnClickListener { selectDay(tvFri, "Viernes") }

        // Seleccionamos el Lunes al entrar
        selectDay(tvMon, "Lunes")
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
        selectedView.background.mutate().setTint(ContextCompat.getColor(this, R.color.orange))
        selectedView.setTextColor(Color.WHITE)

        // Mostramos la rutina de ese día
        cargarRutinaPorDia(dayCode)
    }

    private fun cargarRutinaPorDia(dia: String) {
<<<<<<< Updated upstream
        val listaEjercicios = when (dia) {
            "Lunes" -> listOf(
                Exercise("1. Sentadillas Silla", "3 series de 10.", R.raw.ejemplo_video),
                Exercise("2. Elevación Frontal", "Sube los brazos.", R.raw.video_martes),
                Exercise("3. Marcha estática", "Levanta rodillas.", R.raw.ejemplo_video)
=======
        // Fetch up to 3 exercises for the day
        FirestoreHelper.getExercisesForDay(dia, onSuccess = { exercisesData ->
            val listaEjercicios = mutableListOf<Exercise>()
            
            // Available local videos for exercises
            val availableVideos = listOf(
                R.raw.ejercicio02,
                R.raw.ejercicio01
>>>>>>> Stashed changes
            )
            "Martes" -> listOf(
                Exercise("1. Flexiones pared", "Flexiona codos.", R.raw.video_martes),
                Exercise("2. Apertura pecho", "Abre brazos.", R.raw.ejemplo_video)
            )
            // Agrega lógica para Miercoles, Jueves, Viernes...
            else -> emptyList()
        }

        val exerciseAdapter = ExerciseAdapter(listaEjercicios) { exercise ->
            showVideoPopup(exercise)
        }
        rvExercises.adapter = exerciseAdapter

        if (listaEjercicios.isEmpty()) {
            Toast.makeText(this, "Día libre: $dia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showVideoPopup(exercise: Exercise) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_video_player, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        
        // Fondo transparente para bordes redondeados
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvPopupTitle)
        val videoView = dialogView.findViewById<VideoView>(R.id.vvPopup)
        val btnClose = dialogView.findViewById<Button>(R.id.btnClosePopup)
        val btnFullscreen = dialogView.findViewById<android.widget.ImageButton>(R.id.btnFullscreen)

        tvTitle.text = exercise.title

        val videoPath = "android.resource://" + packageName + "/" + exercise.videoResId
        val uri = Uri.parse(videoPath)
        
        try {
            videoView.setVideoURI(uri)

            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            videoView.setOnPreparedListener { mp ->
                mp.start()
            }
            
            videoView.setOnErrorListener { _, what, extra ->
                Toast.makeText(this, "Error reproduciendo video ($what, $extra)", Toast.LENGTH_SHORT).show()
                true
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error iniciando video: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        // Lógica de Pantalla Completa
        btnFullscreen.setOnClickListener {
            val intent = android.content.Intent(this, FullscreenVideoActivity::class.java)
            intent.putExtra("VIDEO_URL", videoPath)
            startActivity(intent)
            // Opcional: Cerrar el popup al ir a fullscreen o pausarlo
            videoView.pause()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}