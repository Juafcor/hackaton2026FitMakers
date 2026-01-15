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
import com.example.hackathonfitmakers.utils.FirestoreHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var biometricHelper: BiometricHelper
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referencias a los botones y campos de texto
        val etDni = findViewById<EditText>(R.id.etDniLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnSubmitLogin)
        val btnRegister = findViewById<Button>(R.id.btnGoToRegister)
        val btnMic = findViewById<ImageButton>(R.id.btnMicLogin)

        // Initialize BiometricHelper
        biometricHelper = BiometricHelper(this) { status ->
             // Optional feedback
        }
        
        biometricHelper.setup {
             runOnUiThread { Toast.makeText(this, "Voice System Ready", Toast.LENGTH_SHORT).show() }
        }

        // Al pulsar iniciar sesión (Password only)
        btnLogin.setOnClickListener {
            // Obtenemos los textos
            val dni = etDni.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (dni.isNotEmpty() && pass.isNotEmpty()) {
                FirestoreHelper.getUser(dni, onSuccess = { user ->
                    if (user.contrasena == pass) {
                         // Success via Password
                         getSharedPreferences("BiometricPrefs", MODE_PRIVATE).edit().putString("current_user_dni", dni).apply()
                         val intent = Intent(this, MainActivity::class.java)
                         startActivity(intent)
                         finish()
                    } else {
                         Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
                    }
                }, onFailure = { error ->
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(this, "Introduce DNI y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        // Al pulsar registrarse, vamos a la pantalla de registro
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Botón del micrófono (Voice only)
        btnMic.setOnClickListener {
            val dni = etDni.text.toString().trim()
            
            if (dni.isEmpty()) {
                Toast.makeText(this, "Please enter DNI first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isRecording) {
                 biometricHelper.stopListening()
                 isRecording = false
                 btnMic.setColorFilter(null) // Reset
                 return@setOnClickListener
            }

            // Verify user exists first to get their vector
            FirestoreHelper.getUser(dni, onSuccess = { user ->
                // Check if user has voice data
                if (user.voz.isEmpty()) {
                    Toast.makeText(this, "User has no voice data registered", Toast.LENGTH_SHORT).show()
                    return@getUser
                }

                // Start recording
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                     ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                     return@getUser
                }

                isRecording = true
                btnMic.setColorFilter(android.graphics.Color.RED)
                Toast.makeText(this, "Listening for verification...", Toast.LENGTH_SHORT).show()

                biometricHelper.startRecording(object : BiometricHelper.BiometricListener {
                    override fun onSpeakerVectorExtracted(vector: String) {
                        runOnUiThread {
                            // user.voz is List<Float>. Convert to String format "[1.0, 2.0...]"
                            val storedVectorStr = user.voz.toString()
                            
                            val similarity = BiometricHelper.calculateCosineSimilarity(storedVectorStr, vector)
                            isRecording = false
                            btnMic.setColorFilter(null) // Reset
                            
                            // Threshold 0.45 as per sample
                        if (similarity > 0.45) {
                            val prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE)
                            prefs.edit().putString("current_user_dni", dni).apply()
                            
                            Toast.makeText(this@LoginActivity, "Voice Verified! Score: %.2f".format(similarity), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                                Toast.makeText(this@LoginActivity, "Access Denied. Score: %.2f".format(similarity), Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onError(message: String) {
                        runOnUiThread {
                            isRecording = false
                            btnMic.setColorFilter(null) // Reset
                            Toast.makeText(this@LoginActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                })

            }, onFailure = { error ->
                Toast.makeText(this, "User lookup failed: $error", Toast.LENGTH_SHORT).show()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricHelper.stop()
    }
}