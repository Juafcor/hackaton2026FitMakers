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
import com.example.hackathonfitmakers.utils.FirestoreHelper

/**
 * Pantalla de Comunidad.
 * Aquí mostramos eventos sociales (carreras, quedadas...) y el usuario puede apuntarse.
 */
class CommunityActivity : AppCompatActivity() {

    private lateinit var rvEvents: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private var eventList = mutableListOf<Event>() 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        // Botón Volver
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // Configuramos la lista
        rvEvents = findViewById(R.id.rvEvents)
        rvEvents.layoutManager = LinearLayoutManager(this)

        // Empezamos a cargar datos
        loadEventsFromFirestore()
    }

    /**
     * Carga la lista de eventos desde Firebase y verifica en cuáles participa el usuario.
     */
    private fun loadEventsFromFirestore() {
        // Obtenemos el usuario actual
        val prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE)
        val currentUserDni = prefs.getString("current_user_dni", "") ?: ""

        if (currentUserDni.isEmpty()) {
            Toast.makeText(this, "Error de sesión: Por favor inicia sesión de nuevo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 1. Pedimos la lista de eventos
        FirestoreHelper.getCommunityEvents(onSuccess = { eventsData ->
            // 2. Pedimos la lista de eventos a los que YA está apuntado este usuario
            FirestoreHelper.getUserCommunityRelations(currentUserDni, onSuccess = { joinedIds ->
                
                eventList.clear()
                for (data in eventsData) {
                    val id = data["id"] as? String ?: ""
                    val title = data["titulo"] as? String ?: "Sin Título"
                    val date = data["fecha"] as? String ?: "Sin Fecha"
                    val desc = data["mensaje"] as? String ?: "Sin Descripción"
                    // Como no tenemos ubicación en la base de datos, ponemos una por defecto
                    val location = "Comunidad FitMakers" 
                    
                    // Comprobamos si el usuario ya está apuntado a este evento
                    val isJoined = joinedIds.contains(id)

                    // Imagen por defecto
                    val imageResId = R.drawable.gente_corriendo

                    eventList.add(Event(id, title, date, location, desc, imageResId, isJoined))
                }

                // Creamos el adaptador y definimos qué pasa al pulsar un evento
                eventAdapter = EventAdapter(eventList) { event, position ->
                    showEventDialog(event, position, currentUserDni)
                }
                rvEvents.adapter = eventAdapter

            }, onFailure = { e ->
                 Toast.makeText(this, "Error obteniendo relaciones: $e", Toast.LENGTH_SHORT).show()
            })
        }, onFailure = { e ->
            Toast.makeText(this, "Error cargando eventos: $e", Toast.LENGTH_SHORT).show()
        })
    }

    /**
     * Muestra una ventana con los detalles del evento y la opción de Apuntarse/Desapuntarse.
     */
    private fun showEventDialog(event: Event, position: Int, userDni: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_event_details, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvList = dialogView.findViewById<TextView>(R.id.tvParticipantsList)
        val btnAction = dialogView.findViewById<Button>(R.id.btnDialogAction)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)

        tvTitle.text = event.title
        // Actualizamos el color y texto del botón según si ya estás apuntado o no
        updateDialogButton(btnAction, event.isJoined)
        
        // Estado inicial de carga de participantes
        tvList.text = "Cargando participantes..."

        // Consultamos quiénes van a ir
        FirestoreHelper.getEventParticipants(event.id, onSuccess = { names ->
            if (names.isEmpty()) {
                tvList.text = "Participantes:\nNadie se ha apuntado aún."
            } else {
                val formattedList = names.joinToString(separator = "\n") { "- $it" }
                tvList.text = "Participantes:\n$formattedList"
            }
        }, onFailure = {
            tvList.text = "Participantes:\nError al cargar."
        })

        // Acción al pulsar el botón principal (Apuntarse/Salir)
        btnAction.setOnClickListener {
             if (event.isJoined) {
                 // Si ya estoy apuntado, me salgo
                 FirestoreHelper.leaveEvent(userDni, event.id, onSuccess = {
                     event.isJoined = false
                     Toast.makeText(this, "Te has desapuntado.", Toast.LENGTH_SHORT).show()
                     eventAdapter.notifyItemChanged(position) // Actualizamos la lista visual
                     dialog.dismiss()
                 }, onFailure = { e ->
                     Toast.makeText(this, "Error al salir: $e", Toast.LENGTH_SHORT).show()
                 })
             } else {
                 // Si no estoy, me apunto
                 FirestoreHelper.joinEvent(userDni, event.id, onSuccess = {
                     event.isJoined = true
                     Toast.makeText(this, "¡Te has apuntado!", Toast.LENGTH_SHORT).show()
                     eventAdapter.notifyItemChanged(position)
                     dialog.dismiss()
                 }, onFailure = { e ->
                     Toast.makeText(this, "Error al unirse: $e", Toast.LENGTH_SHORT).show()
                 })
             }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    /**
     * Cambia el aspecto del botón dependiendo de si el usuario participa o no.
     */
    private fun updateDialogButton(btn: Button, isJoined: Boolean) {
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