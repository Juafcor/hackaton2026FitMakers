package com.example.hackathonfitmakers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    // Usamos 'lateinit' para no tener que poner signos de interrogación (?) ni exclamación (!!) por todo el código
    private lateinit var llLoginFields: LinearLayout
    private lateinit var etDni: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Asegúrate de que este R sea el de tu paquete, no android.R

        // Referencias a los elementos del XML
        val btnShowLogin = findViewById<Button>(R.id.btnShowLogin)
        val btnSubmitLogin = findViewById<Button>(R.id.btnSubmitLogin)
        val btnGoToRegister = findViewById<Button>(R.id.btnGoToRegister)

        // Inicializamos las variables globales
        llLoginFields = findViewById(R.id.llLoginFields)
        etDni = findViewById(R.id.etDniLogin)
        etPassword = findViewById(R.id.etPassword)

        // 1. Botón para mostrar los campos ocultos (Estilo Kotlin con lambda)
        btnShowLogin.setOnClickListener {
            if (llLoginFields.visibility == View.GONE) {
                llLoginFields.visibility = View.VISIBLE
                btnShowLogin.visibility = View.GONE
            }
        }

        // 2. Botón ENTRAR
        btnSubmitLogin.setOnClickListener {
            val dni = etDni.text.toString()
            val pass = etPassword.text.toString()

            if (dni.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Navegar al Main
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cierra el login para que no se pueda volver atrás con el botón 'atrás'
            }
        }

        // 3. Botón REGISTRARSE
        btnGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}