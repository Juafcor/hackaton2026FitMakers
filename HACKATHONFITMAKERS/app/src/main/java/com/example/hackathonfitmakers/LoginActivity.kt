package com.example.hackathonfitmakers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referencias a los botones y campos de texto
        val etDni = findViewById<EditText>(R.id.etDniLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnSubmitLogin)
        val btnRegister = findViewById<Button>(R.id.btnGoToRegister)
        val btnMic = findViewById<ImageButton>(R.id.btnMicLogin)

        // Al pulsar iniciar sesión
        btnLogin.setOnClickListener {
            // Obtenemos los textos
            val dni = etDni.text.toString()
            val pass = etPassword.text.toString()

            if (dni.isNotEmpty() && pass.isNotEmpty()) {
                // Si está bien, vamos al menú principal
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cerramos esta pantalla
            } else {
                Toast.makeText(this, "Introduce DNI y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        // Al pulsar registrarse, vamos a la pantalla de registro
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Botón del micrófono (aún no hace nada real)
        btnMic.setOnClickListener {
            Toast.makeText(this, "Escuchando...", Toast.LENGTH_SHORT).show()
        }
    }
}