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

/**
 * Actividad para el Chat con Inteligencia Artificial.
 * Usa Gemini para generar respuestas y TextToSpeech para leerlas en voz alta.
 */
class IaActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ia)

        // Preparamos el modelo de IA de Gemini
        generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            // API Key del proyecto. En una app real de producción, esto debería ir más protegido.
            apiKey = "AIzaSyAsE8gGN8kx2OE31qdm89keHjw6ruv_m8Y"
        )

        // Inicializamos el motor de Texto a Voz (TTS)
        tts = TextToSpeech(this, this)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_gchat)
        val editMessage = findViewById<EditText>(R.id.edit_gchat_message)
        val btnSend = findViewById<Button>(R.id.button_gchat_send)
        // Buscamos los botones del encabezado
        val btnHome = findViewById<ImageButton>(R.id.btn_home)
        val btnInfo = findViewById<ImageButton>(R.id.btn_info)

        // Botón Home para volver al menú principal
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Botón Info para saber qué es esto
        btnInfo.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Información")
                .setMessage("Este asistente virtual usa Gemini AI para ayudarte. Escribe tu consulta y escucharás la respuesta.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        // Configuración de la lista de mensajes (Chat)
        adapter = MessageAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Hacemos que la lista empiece desde abajo, como un chat normal
        }

        // Mensaje de bienvenida inicial
        val initialMessage = "¡Hola! Soy tu asistente IA. ¿En qué puedo ayudarte hoy?"
        addMessage(Message(initialMessage, false))
    
        // Acción al pulsar Enviar
        btnSend.setOnClickListener {
            val text = editMessage.text.toString()
            if (text.isNotBlank()) {
                // 1. Añadimos el mensaje del usuario a la lista visual
                addMessage(Message(text, true))
                editMessage.text.clear()

                // Hacemos scroll abajo del todo
                if (messages.size > 0) {
                     recyclerView.smoothScrollToPosition(messages.size - 1)
                }

                // 2. Llamamos a la IA en segundo plano (para no bloquear la pantalla)
                lifecycleScope.launch {
                    try {
                        val response = generativeModel.generateContent(text)
                        
                        // Si tenemos respuesta, la mostramos y la leemos
                        response.text?.let { output ->
                            addMessage(Message(output, false))
                            speak(output)
                            
                            // Scroll a la nueva respuesta
                            if (messages.size > 0) {
                                recyclerView.smoothScrollToPosition(messages.size - 1)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("IaActivity", "Error generando contenido", e)
                        val errorMessage = "Lo siento, hubo un error al conectar con la IA."
                        addMessage(Message(errorMessage, false))
                        speak(errorMessage)
                    }
                }
            }
        }
    }

    /**
     * Función auxiliar para añadir un mensaje a la lista y notificar al adaptador.
     */
    private fun addMessage(message: Message) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
    }

    /**
     * Se llama cuando el motor de voz (TTS) termina de iniciarse.
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Intentamos poner el idioma en Español de España
            val result = tts.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                 Log.e("TTS", "El idioma no está soportado o faltan datos")
            } else {
                // Todo listo
            }
        } else {
            Log.e("TTS", "Fallo al inicializar TTS")
        }
    }
    
    /**
     * Hace que el móvil hable el texto que le pasemos.
     */
    private fun speak(text: String) {
        if (::tts.isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Al salir de la pantalla, liberamos el motor de voz
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}