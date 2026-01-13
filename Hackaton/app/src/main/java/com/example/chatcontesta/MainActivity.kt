package com.example.chatcontesta

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var scriptIndex = 0
    
    // RESPUESTAS PREPARADAS - Agrega o edita tus respuestas aquí
    private val script = listOf(
        "Hola, soy un chatbot creado para el Hackathon.",
        "Hoy es martes, 13 de enero de 2026.",
        "¡Muy bien, gracias! 😊 ¿Y tú qué tal estás hoy?",
        "Lo siento 😕. Descansa, pon hielo y evita forzarla. Si no mejora o se inflama mucho, consulta al médico.",
        "Sí, te ayudo: entra en el apartado de trabajo individual de la app para empezar.",
        "¡De nada! 😊"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TextToSpeech
        tts = TextToSpeech(this, this)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_gchat)
        val editMessage = findViewById<EditText>(R.id.edit_gchat_message)
        val btnSend = findViewById<Button>(R.id.button_gchat_send)

        adapter = MessageAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = false // Start from top
        }

        // Show first message immediately
        val initialAnswer = getNextAnswer()
        addMessage(Message(initialAnswer, false))

        btnSend.setOnClickListener {
            val text = editMessage.text.toString()
            if (text.isNotEmpty()) {
                // 1. Add User Message
                addMessage(Message(text, true))
                editMessage.text.clear()

                // 2. Add Bot Answer (from script)
                val answer = getNextAnswer()
                addMessage(Message(answer, false))
                
                // 3. Speak the answer
                speak(answer)
                
                // Scroll to bottom
                if (messages.size > 0) {
                     recyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
    }

    private fun addMessage(message: Message) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
    }

    private fun getNextAnswer(): String {
        if (script.isEmpty()) return "..."
        val answer = script[scriptIndex]
        scriptIndex = (scriptIndex + 1) % script.size
        return answer
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                 // Handle error if needed (e.g. prompt to install data)
            } else {
                // Speak the initial message once TTS is ready
                if (messages.isNotEmpty() && !messages.last().isUser) {
                    speak(messages.last().content)
                }
            }
        }
    }
    
    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}