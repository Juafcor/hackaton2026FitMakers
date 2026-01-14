package com.example.hackathonfitmakers

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IaActivity : AppCompatActivity() {

    private lateinit var llMessageContainer: LinearLayout
    private lateinit var etMessage: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var ivAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ia)

        llMessageContainer = findViewById(R.id.llMessageContainer)
        etMessage = findViewById(R.id.etMessage)
        scrollView = findViewById(R.id.scrollChat)
        ivAvatar = findViewById(R.id.ivAiAvatar)
        val btnSend = findViewById<Button>(R.id.btnSend)

        // Animación pequeña del avatar al entrar
        ivAvatar.animate().scaleX(1.1f).scaleY(1.1f).setDuration(500).withEndAction {
            ivAvatar.animate().scaleX(1.0f).scaleY(1.0f).setDuration(500)
        }

        btnSend.setOnClickListener {
            val userText = etMessage.text.toString()
            if (userText.isNotEmpty()) {
                // 1. Mensaje que enviamos nosotros
                addMessageToChat(userText, true)
                etMessage.setText("")

                // 2. Respuesta de la IA (esperamos un poco para que parezca real)
                Handler(Looper.getMainLooper()).postDelayed({
                    ivAvatar.animate().rotation(360f).setDuration(500)
                    addMessageToChat("Interesante... Para eso te recomiendo enfocarte en la técnica.", false)
                }, 1000)
            }
        }
    }

    private fun addMessageToChat(text: String, isUser: Boolean) {
        val tv = TextView(this)
        tv.text = text
        tv.setPadding(20, 20, 20, 20)
        tv.textSize = 16f

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)

        if (isUser) {
            tv.setBackgroundColor(Color.parseColor("#BBDEFB")) // Azul
            params.gravity = Gravity.END
        } else {
            tv.setBackgroundColor(Color.parseColor("#E1E1E1")) // Gris
            params.gravity = Gravity.START
        }

        tv.layoutParams = params
        llMessageContainer.addView(tv)

        // Auto-scroll hacia abajo para ver lo último
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}