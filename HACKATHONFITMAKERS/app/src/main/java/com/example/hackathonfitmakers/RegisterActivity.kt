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
        setContentView(R.layout.activity_register)

        // Buscamos los controles en la pantalla
        val etName = findViewById<EditText>(R.id.etName)
        val btnFinish = findViewById<Button>(R.id.btnFinishRegister)

        // Cuando pulsamos el botón de terminar registro
        btnFinish.setOnClickListener {
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

                // Volvemos al menú principal y cerramos el registro
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}