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

/**
 * Actividad para el Plan Individual de Ejercicios.
 * Aquí el usuario ve su rutina diaria según el día de la semana.
 */
class IndividualActivity : AppCompatActivity() {

    private lateinit var rvExercises: RecyclerView

    // Variables donde guardamos las etiquetas de texto de los días (Lunes, Martes...)
    private lateinit var tvMon: TextView
    private lateinit var tvTue: TextView
    private lateinit var tvWed: TextView
    private lateinit var tvThu: TextView
    private lateinit var tvFri: TextView

    // Lista auxiliar para gestionar todos los días juntos
    private lateinit var dayViews: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual)

        // 1. Preparamos el listado donde se verán los ejercicios
        rvExercises = findViewById(R.id.rvExercises)
        rvExercises.layoutManager = LinearLayoutManager(this)

        // 2. Buscamos las etiquetas de los días en la pantalla
        tvMon = findViewById(R.id.tvDayMon)
        tvTue = findViewById(R.id.tvDayTue)
        tvWed = findViewById(R.id.tvDayWed)
        tvThu = findViewById(R.id.tvDayThu)
        tvFri = findViewById(R.id.tvDayFri)

        // Metemos todas en una lista para poder recorrerlas fácil
        dayViews = listOf(tvMon, tvTue, tvWed, tvThu, tvFri)

        // Configuración del botón para voler atrás
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Configuramos qué pasa al pulsar cada día y cargamos el Lunes por defecto
        setupDayListeners()
        selectDay(tvMon, "Lunes")
    }

    /**
     * Asignamos la acción de pulsar a cada etiqueta de día.
     */
    private fun setupDayListeners() {
        tvMon.setOnClickListener { selectDay(tvMon, "Lunes") }
        tvTue.setOnClickListener { selectDay(tvTue, "Martes") }
        tvWed.setOnClickListener { selectDay(tvWed, "Miercoles") }
        tvThu.setOnClickListener { selectDay(tvThu, "Jueves") }
        tvFri.setOnClickListener { selectDay(tvFri, "Viernes") }
    }

    /**
     * Esta función se encarga de cambiar visualmente el día seleccionado
     * y pedir que se carguen los datos correspondientes.
     */
    private fun selectDay(selectedView: TextView, dayCode: String) {
        // Primero ponemos todos los días en su estado normal (sin resaltar)
        val darkBlue = ContextCompat.getColor(this, R.color.dark_blue)

        for (view in dayViews) {
            view.setBackgroundColor(Color.TRANSPARENT)
            view.setTextColor(darkBlue)
        }

        // Ahora resaltamos en naranja el día que acabamos de tocar
        selectedView.setBackgroundResource(R.drawable.bg_input_rounded)
        // Truco para cambiar el color del fondo drawable programáticamente
        selectedView.background.mutate().setTint(ContextCompat.getColor(this, R.color.orange))
        selectedView.setTextColor(Color.WHITE)

        // Finalmente cargamos la rutina de ejercicios
        cargarRutinaPorDia(dayCode)
    }

    /**
     * Consulta a Firebase los ejercicios para el día indicado.
     */
    private fun cargarRutinaPorDia(dia: String) {
        // Pedimos los ejercicios del día (máximo 3)
        FirestoreHelper.getExercisesForDay(dia, onSuccess = { exercisesData ->
            val listaEjercicios = mutableListOf<Exercise>()
            
            // Lista de videos disponibles localmente en la app
            val availableVideos = listOf(
                R.raw.ejercicio02,
                R.raw.ejercicio01
            )

            // Lista de imágenes disponibles para usar como portada
            val availableImages = listOf(
                R.drawable.capturavideo,
                R.drawable.capturavideo2,
                R.drawable.capturavideo3,
                R.drawable.capturavideo4,
                R.drawable.capturavideo5,
                R.drawable.capturavideo6
            )

            // Recorremos los datos que llegaron de la base de datos
            for ((index, data) in exercisesData.withIndex()) {
                 val title = data["titulo"] as? String ?: "Ejercicio"
                 val desc = data["descripcion"] as? String ?: ""
                 
                 // Asignamos video e imagen de forma rotativa (para que no se repitan seguido)
                 val videoResId = availableVideos[index % availableVideos.size]
                 val imageResId = availableImages[index % availableImages.size]
                 
                 // Añadimos el ejercicio completo a la lista
                 listaEjercicios.add(Exercise(title, desc, videoResId, imageResId))
            }

            // Creamos el adaptador y le DECIMOS qué hacer cunado se pulse un ejercicio: mostrar el video
            val exerciseAdapter = ExerciseAdapter(listaEjercicios) { exercise ->
                showVideoPopup(exercise)
            }
            rvExercises.adapter = exerciseAdapter

            // Si la lista está vacía, puede ser la primera vez. Ofrecemos crear datos de prueba.
            if (listaEjercicios.isEmpty()) {
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
                                   Toast.makeText(this, "Hubo un error generando los datos: $error", Toast.LENGTH_SHORT).show()
                               }
                           )
                       }
                       .setNegativeButton("No", null)
                       .show()
                } else {
                    Toast.makeText(this, "No se encontró rutina para el $dia", Toast.LENGTH_SHORT).show()
                }
            }
        }, onFailure = { e ->
            Toast.makeText(this, "Error cargando ejercicios: $e", Toast.LENGTH_SHORT).show()
        })
    }

    /**
     * Muestra una ventana emergente (Popup) con el reproductor de video.
     */
    private fun showVideoPopup(exercise: Exercise) {
        // Inflamos el diseño de la ventana
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_video_player, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        
        // Ponemos el fondo transparente para que se vean bien los bordes redondeados
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvPopupTitle)
        val videoView = dialogView.findViewById<VideoView>(R.id.vvPopup)
        val btnClose = dialogView.findViewById<Button>(R.id.btnClosePopup)
        val btnFullscreen = dialogView.findViewById<android.widget.ImageButton>(R.id.btnFullscreen)

        tvTitle.text = exercise.title

        // Preparamos la ruta del video
        val videoPath = "android.resource://" + packageName + "/" + exercise.videoResId
        val uri = Uri.parse(videoPath)
        
        try {
            videoView.setVideoURI(uri)

            // Añadimos controles
            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            // Arrancamos cuando el video esté listo
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

        // Botón para ver en Pantalla Completa
        btnFullscreen.setOnClickListener {
            val intent = android.content.Intent(this, FullscreenVideoActivity::class.java)
            intent.putExtra("VIDEO_URL", videoPath)
            startActivity(intent)
            // Pausamos aquí para que no suene doble
            videoView.pause()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}