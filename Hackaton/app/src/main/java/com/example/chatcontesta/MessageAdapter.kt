package com.example.chatcontesta

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.text_message_body)
        val nameText: TextView = view.findViewById(R.id.text_message_name)
        val container: LinearLayout = view as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.content
        
        if (message.isUser) {
            holder.container.gravity = Gravity.END
            holder.nameText.text = "Tú"
            holder.nameText.gravity = Gravity.END
            holder.messageText.setBackgroundColor(Color.parseColor("#2196F3")) // Blue for user
        } else {
            holder.container.gravity = Gravity.START
            holder.nameText.text = "IA Assistant"
            holder.nameText.gravity = Gravity.START
            holder.messageText.setBackgroundColor(Color.parseColor("#4CAF50")) // Green for bot
        }
    }

    override fun getItemCount() = messages.size
}
