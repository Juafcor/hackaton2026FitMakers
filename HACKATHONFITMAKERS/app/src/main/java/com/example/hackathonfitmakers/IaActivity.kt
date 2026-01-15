package com.example.hackathonfitmakers

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import java.util.Locale

class IaActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ia)

        // Initialize Gemini
        generativeModel = GenerativeModel(
             modelName = "gemini-2.5-flash",
             apiKey = "AIzaSyAcTFlDWSJRN43yGKBB70HG7R1GEJbMfls" // TODO: Replace with your actual API Key or retrieve from BuildConfig
        )

        val btnSend = findViewById<Button>(R.id.button_gchat_send)
        val etMessage = findViewById<EditText>(R.id.edit_gchat_message)
        val recycler = findViewById<RecyclerView>(R.id.recycler_gchat)
        val btnHome = findViewById<ImageButton>(R.id.btn_home)
        val btnInfo = findViewById<ImageButton>(R.id.btn_info)

        adapter = MessageAdapter(messages)
        recycler.layoutManager = LinearLayoutManager(this).apply {
             stackFromEnd = true
        }
        recycler.adapter = adapter

        // Initial welcome message
        addMessage(Message("¡Hola! Soy tu asistente IA. ¿En qué puedo ayudarte hoy?", false))

        btnSend.setOnClickListener {
            val userText = etMessage.text.toString()
            if (userText.isNotBlank()) {
                // 1. Show user message
                addMessage(Message(userText, true))
                etMessage.setText("")

                // 2. Call AI
                lifecycleScope.launch {
                    try {
                        val response = generativeModel.generateContent(userText)
                        response.text?.let { output ->
                            addMessage(Message(output, false))
                        }
                    } catch (e: Exception) {
                        Log.e("IaActivity", "Error generating content", e)
                        addMessage(Message("Lo siento, hubo un error al conectar con la IA.", false))
                    }
                }
            }
        }

        btnHome.setOnClickListener {
            finish()
        }

        btnInfo.setOnClickListener {
             Toast.makeText(this, "Gemini 2.5 Flash Chatbot", Toast.LENGTH_SHORT).show()
        }
        
    }

    private fun addMessage(message: Message) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        findViewById<RecyclerView>(R.id.recycler_gchat).scrollToPosition(messages.size - 1)
    }
}