package com.example.hackathonfitmakers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExerciseAdapter(
    private val exerciseList: List<Exercise>,
    private val onVideoClick: (Exercise) -> Unit // Qué pasa al pulsar ver video
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvItemDescription)
        val btnVideo: Button = view.findViewById(R.id.btnWatchVideo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val item = exerciseList[position]
        holder.tvTitle.text = item.title
        holder.tvDesc.text = item.description

        holder.btnVideo.setOnClickListener {
            onVideoClick(item) // Avisamos que han pulsado el video
        }
    }

    override fun getItemCount() = exerciseList.size
}