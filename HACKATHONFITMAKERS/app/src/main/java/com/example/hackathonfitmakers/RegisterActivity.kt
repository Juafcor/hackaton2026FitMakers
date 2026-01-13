package com.example.hackathonfitmakers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Usa el R de tu proyecto, NO android.R

        // Referencias a los elementos de la interfaz
        // En Kotlin no hace falta poner <EditText?> con interrogación, asumimos que existe
        val etName = findViewById<EditText>(R.id.etName)
        val btnFinish = findViewById<Button>(R.id.btnFinishRegister)

        // Sintaxis simplificada (Lambda) para el click
        btnFinish.setOnClickListener {
            // En Kotlin se usa .text en vez de .getText()
            if (etName.text.toString().isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, introduce tu nombre",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "¡Registro completado!",
                    Toast.LENGTH_LONG
                ).show()

                // Al terminar registro, vamos directos al Menu Principal
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cierra el registro para no volver atrás
            }
        }
    }
}