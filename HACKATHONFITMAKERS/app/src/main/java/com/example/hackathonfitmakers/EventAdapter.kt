package com.example.hackathonfitmakers

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private val eventList: List<Event>,
    private val onButtonClick: (Event, Int) -> Unit // Qué pasa al pulsar el botón
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvItemEventTitle)
        val tvDate: TextView = view.findViewById(R.id.tvItemEventDate)
        val tvPlace: TextView = view.findViewById(R.id.tvItemEventPlace)
        val tvDesc: TextView = view.findViewById(R.id.tvItemEventDesc)
        val imgEvent: ImageView = view.findViewById(R.id.imgItemEvent)
        val btnJoin: Button = view.findViewById(R.id.btnItemJoin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        // Ponemos la información en la tarjeta
        holder.tvTitle.text = event.title
        holder.tvDate.text = event.date
        holder.tvPlace.text = event.location
        holder.tvDesc.text = event.description
        holder.imgEvent.setImageResource(event.imageResId)

        // Actualizamos el botón (si está verde o naranja)
        updateButtonState(holder.btnJoin, event.isJoined)

        // Al pulsar el botón
        holder.btnJoin.setOnClickListener {
            onButtonClick(event, position)
        }
    }

    override fun getItemCount() = eventList.size

    // Cambiamos el color del botón según si estamos apuntados
    private fun updateButtonState(btn: Button, isJoined: Boolean) {
        // Obtenemos los colores de nuestros recursos
        val context = btn.context
        val orange = androidx.core.content.ContextCompat.getColor(context, R.color.orange)
        val green = androidx.core.content.ContextCompat.getColor(context, R.color.custom_green)

        if (isJoined) {
            btn.text = "DESAPUNTARSE"
            btn.setBackgroundColor(orange) // Naranja para salir
        } else {
            btn.text = "APUNTARSE"
            btn.setBackgroundColor(green) // Verde para entrar
        }
    }
}