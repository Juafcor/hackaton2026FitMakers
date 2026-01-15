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

        loadEventsFromFirestore()
    }

    private fun loadEventsFromFirestore() {
        val prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE)
        val currentUserDni = prefs.getString("current_user_dni", "") ?: ""

        if (currentUserDni.isEmpty()) {
            Toast.makeText(this, "Session error: Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 1. Fetch Events
        FirestoreHelper.getCommunityEvents(onSuccess = { eventsData ->
            // 2. Fetch User Relations (Joined events)
            FirestoreHelper.getUserCommunityRelations(currentUserDni, onSuccess = { joinedIds ->
                
                eventList.clear()
                for (data in eventsData) {
                    val id = data["id"] as? String ?: ""
                    val title = data["titulo"] as? String ?: "No Title"
                    val date = data["fecha"] as? String ?: "No Date"
                    val desc = data["mensaje"] as? String ?: "No Description"
                    // Location not in schema, using default
                    val location = "FitMakers Community" 
                    
                    // Determine if joined
                    val isJoined = joinedIds.contains(id)

                    // Provide Default Image
                    val imageResId = R.drawable.gente_corriendo

                    eventList.add(Event(id, title, date, location, desc, imageResId, isJoined))
                }

                // Adapter
                eventAdapter = EventAdapter(eventList) { event, position ->
                    showEventDialog(event, position, currentUserDni)
                }
                rvEvents.adapter = eventAdapter

            }, onFailure = { e ->
                 Toast.makeText(this, "Error fetching relations: $e", Toast.LENGTH_SHORT).show()
            })
        }, onFailure = { e ->
            Toast.makeText(this, "Error loading events: $e", Toast.LENGTH_SHORT).show()
        })
    }

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
        updateDialogButton(btnAction, event.isJoined)
        
        // Initial clean state
        tvList.text = "Cargando participantes..."

        // Fetch participants
        FirestoreHelper.getEventParticipants(event.id, onSuccess = { names ->
            if (names.isEmpty()) {
                tvList.text = "Participantes:\nNadíe se ha apuntado aún."
            } else {
                val formattedList = names.joinToString(separator = "\n") { "- $it" }
                tvList.text = "Participantes:\n$formattedList"
            }
        }, onFailure = {
            tvList.text = "Participantes:\nError al cargar."
        })

        btnAction.setOnClickListener {
             if (event.isJoined) {
                 // Leave
                 FirestoreHelper.leaveEvent(userDni, event.id, onSuccess = {
                     event.isJoined = false
                     Toast.makeText(this, "Te has desapuntado.", Toast.LENGTH_SHORT).show()
                     eventAdapter.notifyItemChanged(position)
                     dialog.dismiss()
                 }, onFailure = { e ->
                     Toast.makeText(this, "Error leaving: $e", Toast.LENGTH_SHORT).show()
                 })
             } else {
                 // Join
                 FirestoreHelper.joinEvent(userDni, event.id, onSuccess = {
                     event.isJoined = true
                     Toast.makeText(this, "¡Te has apuntado!", Toast.LENGTH_SHORT).show()
                     eventAdapter.notifyItemChanged(position)
                     dialog.dismiss()
                 }, onFailure = { e ->
                     Toast.makeText(this, "Error joining: $e", Toast.LENGTH_SHORT).show()
                 })
             }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

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