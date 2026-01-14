package com.example.hackathonfitmakers

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CommunityActivity : AppCompatActivity() {

    private lateinit var rvEvents: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private var eventList = mutableListOf<Event>() // Lista de eventos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        // Botón Volver
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // Configuramos la lista
        rvEvents = findViewById(R.id.rvEvents)
        rvEvents.layoutManager = LinearLayoutManager(this)

        // Cargamos los datos
        cargarDatosDePrueba()

        // Configuramos el adaptador (qué pasa cuando pulsas un ítem)
        eventAdapter = EventAdapter(eventList) { event, position ->
            // Abrimos el diálogo con los detalles
            showEventDialog(event, position)
        }
        rvEvents.adapter = eventAdapter
    }

    // Cargamos eventos falsos para probar
    private fun cargarDatosDePrueba() {
        eventList.add(Event(
            1,
            "Maratón Solidaria FITMAKERS",
            "Fecha: 24 Oct | Hora: 09:00 AM",
            "Lugar: Parque Central",
            "Únete a nosotros para una carrera de 5km con fines benéficos.",
            R.drawable.gente_corriendo,
            false // Al principio no estamos apuntados
        ))

        eventList.add(Event(
            2,
            "Yoga al aire libre",
            "Fecha: 26 Oct | Hora: 18:00 PM",
            "Lugar: Playa de la Malagueta",
            "Clase relajante de Yoga Vinyasa al atardecer. Trae agua.",
            android.R.drawable.ic_menu_gallery,
            false
        ))

        // Puedes añadir más eventos aquí abajo
        eventList.add(Event(
            3,
            "Torneo de Petanca",
            "Fecha: 28 Oct | Hora: 11:00 AM",
            "Lugar: Plaza Mayor",
            "Competición amistosa por parejas. Premios para los ganadores.",
            android.R.drawable.ic_menu_gallery,
            false
        ))
    }

    // Mostramos la ventanita con los detalles
    private fun showEventDialog(event: Event, position: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_event_details, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvList = dialogView.findViewById<TextView>(R.id.tvParticipantsList)
        val btnAction = dialogView.findViewById<Button>(R.id.btnDialogAction)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)

        tvTitle.text = event.title
        updateDialogButton(btnAction, event.isJoined)

        // Cambiamos la lista de gente para que se note si estás tú
        if (event.isJoined) {
            tvList.text = "- Juan Pérez\n- María López\n- Carlos García\n- ¡TÚ!"
        } else {
            tvList.text = "- Juan Pérez\n- María López\n- Carlos García"
        }

        btnAction.setOnClickListener {
            // Invertimos estado
            val newState = !event.isJoined 
            event.isJoined = newState 

            if (newState) Toast.makeText(this, "¡Te has apuntado!", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Te has desapuntado.", Toast.LENGTH_SHORT).show()

            // Avisamos a la lista para que se actualice el color del botón
            eventAdapter.notifyItemChanged(position)

            dialog.dismiss()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun updateDialogButton(btn: Button, isJoined: Boolean) {
        // Colores consistentes
        val orange = androidx.core.content.ContextCompat.getColor(this, R.color.orange)
        val green = androidx.core.content.ContextCompat.getColor(this, R.color.custom_green)

        if (isJoined) {
            btn.text = "Desapuntarse"
            btn.setBackgroundColor(orange)
        } else {
            btn.text = "Apuntarse"
            btn.setBackgroundColor(green)
        }
    }
}