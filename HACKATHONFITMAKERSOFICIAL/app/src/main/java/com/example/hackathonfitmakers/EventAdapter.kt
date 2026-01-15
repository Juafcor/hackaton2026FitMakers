package com.example.hackathonfitmakers

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador para mostrar la lista de eventos en la Comunidad.
 * Gestiona cómo se ven las tarjetas de eventos y el botón de apuntarse/desapuntarse.
 */
class EventAdapter(
    private val eventList: List<Event>,
    private val onButtonClick: (Event, Int) -> Unit // Acción al pulsar el botón
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    /**
     * Referencias a los elementos visuales de la tarjeta del evento.
     */
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

        // Rellenamos la tarjeta con los datos del evento
        holder.tvTitle.text = event.title
        holder.tvDate.text = event.date
        holder.tvPlace.text = event.location
        holder.tvDesc.text = event.description
        holder.imgEvent.setImageResource(event.imageResId)

        // Actualizamos el aspecto del botón (entrar o salir) según corresponda
        updateButtonState(holder.btnJoin, event.isJoined)

        // Listener para cuando se pulsa el botón
        holder.btnJoin.setOnClickListener {
            onButtonClick(event, position)
        }
    }

    override fun getItemCount() = eventList.size

    /**
     * Esta función cambia el color y el texto del botón:
     * - Verde / "APUNTARSE" si no estás dentro.
     * - Naranja / "DESAPUNTARSE" si ya participas.
     */
    private fun updateButtonState(btn: Button, isJoined: Boolean) {
        val context = btn.context
        val orange = androidx.core.content.ContextCompat.getColor(context, R.color.orange)
        val green = androidx.core.content.ContextCompat.getColor(context, R.color.custom_green)

        if (isJoined) {
            btn.text = "DESAPUNTARSE"
            btn.setBackgroundColor(orange)
        } else {
            btn.text = "APUNTARSE"
            btn.setBackgroundColor(green)
        }
    }
}