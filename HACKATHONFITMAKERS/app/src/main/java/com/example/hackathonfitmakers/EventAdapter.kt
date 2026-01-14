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
    private val onButtonClick: (Event, Int) -> Unit // Pasamos el evento y su posición en la lista
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

        holder.tvTitle.text = event.title
        holder.tvDate.text = event.date
        holder.tvPlace.text = event.location
        holder.tvDesc.text = event.description
        holder.imgEvent.setImageResource(event.imageResId)

        // Configurar estado del botón (Visual)
        updateButtonState(holder.btnJoin, event.isJoined)

        // Click en el botón
        holder.btnJoin.setOnClickListener {
            onButtonClick(event, position)
        }
    }

    override fun getItemCount() = eventList.size

    // Función auxiliar para pintar el botón
    private fun updateButtonState(btn: Button, isJoined: Boolean) {
        if (isJoined) {
            btn.text = "DESAPUNTARSE"
            btn.setBackgroundColor(Color.RED)
        } else {
            btn.text = "APUNTARSE"
            btn.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
        }
    }
}