package com.example.hackathonfitmakers

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CommunityActivity : AppCompatActivity() {

    // Variables para simular el estado de cada evento (true = apuntado, false = no apuntado)
    private var isJoinedEvent1 = false
    private var isJoinedEvent2 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        val btnJoin1 = findViewById<Button>(R.id.btnJoinEvent1)
        val btnJoin2 = findViewById<Button>(R.id.btnJoinEvent2)

        // Evento 1: Maratón
        btnJoin1.setOnClickListener {
            showEventDialog("Maratón Solidaria", isJoinedEvent1) { newState ->
                isJoinedEvent1 = newState // Actualizamos el estado
                // Cambiamos el texto del botón de la pantalla principal también
                btnJoin1.text = if (isJoinedEvent1) "DESAPUNTARSE" else "APUNTARSE"
            }
        }

        // Evento 2: Yoga
        btnJoin2.setOnClickListener {
            showEventDialog("Yoga al aire libre", isJoinedEvent2) { newState ->
                isJoinedEvent2 = newState
                btnJoin2.text = if (isJoinedEvent2) "DESAPUNTARSE" else "APUNTARSE"
            }
        }
    }

    // Función para mostrar el Popup
    private fun showEventDialog(eventName: String, currentState: Boolean, onStateChanged: (Boolean) -> Unit) {
        // 1. Inflar el diseño del XML que creamos
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_event_details, null)

        // 2. Crear el Dialog
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        // 3. Referencias a los elementos del Popup
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvList = dialogView.findViewById<TextView>(R.id.tvParticipantsList)
        val btnAction = dialogView.findViewById<Button>(R.id.btnDialogAction)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)

        // Configurar textos iniciales
        tvTitle.text = eventName
        updateDialogButton(btnAction, currentState)

        // Simulación de lista de base de datos
        // Si ya estás apuntado, añadimos "TU USUARIO" a la lista visualmente
        if (currentState) {
            tvList.text = "- Juan Pérez\n- María López\n- Carlos García\n- Ana Torres\n- ¡TÚ!"
        } else {
            tvList.text = "- Juan Pérez\n- María López\n- Carlos García\n- Ana Torres"
        }

        // 4. Lógica del botón Apuntarse/Desapuntarse
        btnAction.setOnClickListener {
            val newState = !currentState // Invertimos el estado (si era true pasa a false)

            if (newState) {
                Toast.makeText(this, "¡Te has apuntado!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Te has desapuntado.", Toast.LENGTH_SHORT).show()
            }

            // Devolvemos el nuevo estado a la actividad principal y cerramos
            onStateChanged(newState)
            dialog.dismiss()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Mostrar fondo transparente para que se vea redondeado si se quiere (opcional)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // Función auxiliar para pintar el botón
    private fun updateDialogButton(btn: Button, isJoined: Boolean) {
        if (isJoined) {
            btn.text = "Desapuntarse"
            btn.setBackgroundColor(android.graphics.Color.RED) // Rojo para salir
        } else {
            btn.text = "Apuntarse"
            btn.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // Verde para entrar
        }
    }
}