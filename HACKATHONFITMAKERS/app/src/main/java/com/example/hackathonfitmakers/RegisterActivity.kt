package com.example.hackathonfitmakers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hackathonfitmakers.utils.BiometricHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var biometricHelper: BiometricHelper
    private var capturedVector: String? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Buscamos los controles en la pantalla
        val etName = findViewById<EditText>(R.id.etName)
        val etDni = findViewById<EditText>(R.id.etDniRegister)
        val etPassword = findViewById<EditText>(R.id.etPasswordRegister)
        val btnFinish = findViewById<Button>(R.id.btnFinishRegister)
        val btnMic = findViewById<ImageButton>(R.id.btnMicRegister)

        // Initialize BiometricHelper
        biometricHelper = BiometricHelper(this) { status ->
             // Optional: Display status in a Toast or Log if needed
             // runOnUiThread { Toast.makeText(this, status, Toast.LENGTH_SHORT).show() }
        }
        
        biometricHelper.setup {
             runOnUiThread { Toast.makeText(this, "Voice System Ready", Toast.LENGTH_SHORT).show() }
        }

        // Configurar botón del micrófono
        btnMic.setOnClickListener {
            if (isRecording) {
                 biometricHelper.stopListening()
                 isRecording = false
                 btnMic.setColorFilter(ContextCompat.getColor(this, R.color.dark_blue)) // Reset color
                 Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                 ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                 return@setOnClickListener
            }

            isRecording = true
            btnMic.setColorFilter(android.graphics.Color.RED) // Visual feedback
            Toast.makeText(this, "Listening... Speak now.", Toast.LENGTH_SHORT).show()
            
            biometricHelper.startRecording(object : BiometricHelper.BiometricListener {
                override fun onSpeakerVectorExtracted(vector: String) {
                    capturedVector = vector
                    runOnUiThread {
                        isRecording = false
                        btnMic.setColorFilter(ContextCompat.getColor(this@RegisterActivity, R.color.dark_blue)) // Reset color
                        Toast.makeText(this@RegisterActivity, "Voice Capture Successful!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(message: String) {
                    runOnUiThread {
                        isRecording = false
                        btnMic.setColorFilter(ContextCompat.getColor(this@RegisterActivity, R.color.dark_blue)) // Reset color
                        Toast.makeText(this@RegisterActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        // Cuando pulsamos el botón de terminar registro
        btnFinish.setOnClickListener {
            val name = etName.text.toString()
            val dni = etDni.text.toString()
            val password = etPassword.text.toString()

            if (name.isEmpty() || dni.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter Name, DNI and Password",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (capturedVector == null) {
                Toast.makeText(
                    this,
                    "Please record your voice first",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Save to SharedPreferences using DNI as Key
                val prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString(dni, capturedVector)
                editor.putString(dni + "_pass", password) // Save password with suffix
                editor.apply()

                Toast.makeText(
                    this,
                    "¡Registro completado!",
                    Toast.LENGTH_LONG
                ).show()

                // Volvemos al menú principal (Login) y cerramos el registro
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricHelper.stop()
    }
}