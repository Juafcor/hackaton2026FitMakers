package com.example.hackathonfitmakers

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class IaActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var scriptIndex = 0
    
    // RESPUESTAS PREPARADAS
    private val script = listOf(
        "Hola, soy un chatbot creado para el Hackathon.",
        "Hoy es martes, 14 de enero de 2026.",
        "¡Muy bien, gracias! 😊 ¿Y tú qué tal estás hoy?",
        "Lo siento 😕. Descansa, pon hielo y evita forzarla. Si no mejora o se inflama mucho, consulta al médico.",
        "Sí, te ayudo: entra en el apartado de trabajo individual de la app para empezar.",
        "¡De nada! 😊"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ia)

        // Initialize TextToSpeech
        tts = TextToSpeech(this, this)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_gchat)
        val editMessage = findViewById<EditText>(R.id.edit_gchat_message)
        val btnSend = findViewById<Button>(R.id.button_gchat_send)
        // Ensure we find the views, even if casted generally
        val btnHome = findViewById<ImageButton>(R.id.btn_home)
        val btnInfo = findViewById<ImageButton>(R.id.btn_info)

        btnHome.setOnClickListener {
            // Debug feedback
            Toast.makeText(this, "Volviendo al menú...", Toast.LENGTH_SHORT).show()
            
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        btnInfo.setOnClickListener {
            // Debug feedback
            Toast.makeText(this, "Mostrando información...", Toast.LENGTH_SHORT).show()
            
            android.app.AlertDialog.Builder(this)
                .setTitle("Información")
                .setMessage("Este asistente virtual está aquí para ayudarte. Escribe tu consulta y recibirás una respuesta.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        adapter = MessageAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = false 
        }

        // Show first message
        val initialAnswer = getNextAnswer()
        addMessage(Message(initialAnswer, false))

        btnSend.setOnClickListener {
            val text = editMessage.text.toString()
            if (text.isNotEmpty()) {
                addMessage(Message(text, true))
                editMessage.text.clear()

                val answer = getNextAnswer()
                addMessage(Message(answer, false))
                
                speak(answer)
                
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
                 // Handle error
            } else {
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