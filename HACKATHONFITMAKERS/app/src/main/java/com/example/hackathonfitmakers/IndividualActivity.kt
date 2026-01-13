package com.example.hackathonfitmakers

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.MediaController
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton

class IndividualActivity : AppCompatActivity() {

    // Declaramos el RecyclerView a nivel de clase para poder acceder desde el Listener
    private lateinit var rvExercises: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual)

        // Configurar botón volver (si lo mantienes)
        /* val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
        */

        // 1. Configurar RecyclerView
        rvExercises = findViewById(R.id.rvExercises)
        rvExercises.layoutManager = LinearLayoutManager(this)

        // 2. Configurar Spinner
        val spinner = findViewById<Spinner>(R.id.spinnerDays)
        val dias = arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dias)
        spinner.adapter = adapter

        // 3. AÑADIMOS EL LISTENER PARA DETECTAR CAMBIOS EN EL SPINNER
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Obtenemos el texto del día seleccionado (ej: "Lunes")
                val diaSeleccionado = dias[position]

                // Cargamos la rutina específica para ese día
                cargarRutinaPorDia(diaSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacemos nada
            }
        }
    }

    // Función que decide qué ejercicios mostrar según el día
    private fun cargarRutinaPorDia(dia: String) {
        val listaEjercicios = when (dia) {
            "Lunes" -> listOf(
                Exercise("1. Sentadillas Silla", "Siéntate y levántate con espalda recta. 3 series de 10.", R.raw.ejemplo_video),
                Exercise("2. Elevación Frontal", "Sube los brazos hacia el frente hasta los hombros.", R.raw.video_martes),
                Exercise("3. Marcha estática", "Levanta rodillas alternas sin desplazarte.", R.raw.ejemplo_video)
            )
            "Martes" -> listOf(
                // Si has añadido otro video, cambia R.raw.ejemplo_video por R.raw.nombre_de_tu_nuevo_video
                Exercise("1. Flexiones en pared", "Apóyate en la pared y flexiona los codos suavemente.", R.raw.video_martes),
                Exercise("2. Apertura de pecho", "Abre los brazos en cruz y ciérralos al centro.", R.raw.ejemplo_video),
                Exercise("3. Rotación de tronco", "Gira suavemente la cintura a un lado y a otro.", R.raw.ejemplo_video)
            )
            else -> emptyList() // Para el resto de días (Mié-Dom) sale vacío por ahora
        }

        // Si la lista está vacía, podemos mostrar un mensaje (opcional),
        // pero aquí simplemente actualizamos el adaptador.

        val exerciseAdapter = ExerciseAdapter(listaEjercicios) { exercise ->
            showVideoPopup(exercise)
        }
        rvExercises.adapter = exerciseAdapter

        // Mensaje opcional para verificar que cambió
        if (listaEjercicios.isEmpty()) {
            Toast.makeText(this, "Día de descanso: Sin ejercicios", Toast.LENGTH_SHORT).show()
        }
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
            // Construimos la ruta del video
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

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}