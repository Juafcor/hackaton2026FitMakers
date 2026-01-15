package com.example.hackathonfitmakers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador para mostrar la lista de ejercicios en el Plan Individual.
 * Se encarga de coger los datos de un ejercicio y pintarlos en la tarjeta.
 */
class ExerciseAdapter(
    private val exerciseList: List<Exercise>,
    private val onVideoClick: (Exercise) -> Unit // Acción a ejecutar cuando se pulsa para ver el video
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    /**
     * Clase interna que busca y guarda las referencias a las vistas de cada tarjeta (título, descripción, etc.)
     */
    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvItemDescription)
        val btnVideo: Button = view.findViewById(R.id.btnWatchVideo)
    }

    /**
     * Aquí "inflamos" el diseño XML de la tarjeta (item_exercise) para crear una nueva vista cuando hace falta.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    /**
     * Aquí rellenamos los datos de la tarjeta con la información del ejercicio correspondiente.
     */
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val item = exerciseList[position]
        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.description
        
        // Buscamos la imagen manualmente y le ponemos la foto que toca
        val ivThumb = holder.itemView.findViewById<android.widget.ImageView>(R.id.ivExerciseThumb)
        ivThumb.setImageResource(item.imageResId)

        // Hacemos que al tocar cualquier parte de la tarjeta se abra el video
        holder.itemView.setOnClickListener {
            onVideoClick(item)
        }
        
        // También configuramos el botón específico, por si acaso
        holder.btnVideo.setOnClickListener {
            onVideoClick(item)
        }
    }

    // Devuelve cuántos ejercicios hay en total
    override fun getItemCount() = exerciseList.size
}