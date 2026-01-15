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

/**
 * Pantalla de inicio de sesión. Permite entrar con contraseña o usando reconocimiento de voz.
 */
class LoginActivity : AppCompatActivity() {

    // Helper para manejar todo lo relacionado con el reconocimiento de voz
    private lateinit var biometricHelper: BiometricHelper
    // Variable para saber si estamos grabando audio en este momento
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Buscamos los elementos de la interfaz: campos de texto y botones
        val etDni = findViewById<EditText>(R.id.etDniLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnSubmitLogin)
        val btnRegister = findViewById<Button>(R.id.btnGoToRegister)
        val btnMic = findViewById<ImageButton>(R.id.btnMicLogin)

        // Inicializamos el sistema de biometría de voz
        biometricHelper = BiometricHelper(this) { status ->
             // Aquí podríamos mostrar mensajes de estado si quisiéramos
        }
        
        // Configuramos el sistema y avisamos cuando esté listo
        biometricHelper.setup {
             runOnUiThread { Toast.makeText(this, "Sistema de voz listo", Toast.LENGTH_SHORT).show() }
        }

        // Configuración del botón de iniciar sesión (Método tradicional con contraseña)
        btnLogin.setOnClickListener {
            // Leemos lo que ha escrito el usuario
            val dni = etDni.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (dni.isNotEmpty() && pass.isNotEmpty()) {
                // Consultamos a la base de datos para ver si existe el usuario
                FirestoreHelper.getUser(dni, onSuccess = { user ->
                    // Comprobamos si la contraseña coincide
                    if (user.contrasena == pass) {
                         // ¡Correcto! Guardamos el DNI y entramos a la app
                         getSharedPreferences("BiometricPrefs", MODE_PRIVATE).edit().putString("current_user_dni", dni).apply()
                         val intent = Intent(this, MainActivity::class.java)
                         startActivity(intent)
                         finish()
                    } else {
                         // Contraseña mal
                         Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
                }, onFailure = { error ->
                    // Algo falló al conectar con la base de datos
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(this, "Introduce DNI y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para ir a registrarse si no tienes cuenta
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Configuración del botón de micrófono (Inicio de sesión por Voz)
        btnMic.setOnClickListener {
            val dni = etDni.text.toString().trim()
            
            // Necesitamos el DNI para saber qué usuario buscar
            if (dni.isEmpty()) {
                Toast.makeText(this, "Por favor, escribe tu DNI primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si ya estábamos grabando, paramos la grabación
            if (isRecording) {
                 biometricHelper.stopListening()
                 isRecording = false
                 btnMic.setColorFilter(null) // Quitamos el color rojo
                 return@setOnClickListener
            }

            // Primero verificamos que el usuario existe y recuperamos sus datos
            FirestoreHelper.getUser(dni, onSuccess = { user ->
                // Comprobamos si este usuario tiene su voz registrada
                if (user.voz.isEmpty()) {
                    Toast.makeText(this, "Este usuario no tiene voz registrada", Toast.LENGTH_SHORT).show()
                    return@getUser
                }

                // Pedimos permiso para usar el micrófono si no lo tenemos
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                     ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                     return@getUser
                }

                // Empezamos a grabar
                isRecording = true
                btnMic.setColorFilter(android.graphics.Color.RED) // Ponemos el icono en rojo para indicar grabación
                Toast.makeText(this, "Escuchando... Di algo para verificar.", Toast.LENGTH_SHORT).show()

                // Llamamos al helper para que capture la voz y la compare
                biometricHelper.startRecording(object : BiometricHelper.BiometricListener {
                    override fun onSpeakerVectorExtracted(vector: String) {
                        runOnUiThread {
                            // Recuperamos el vector de voz guardado del usuario (es una lista de números)
                            val storedVectorStr = user.voz.toString()
                            
                            // Calculamos cuánto se parece la voz actual a la guardada
                            val similarity = BiometricHelper.calculateCosineSimilarity(storedVectorStr, vector)
                            isRecording = false
                            btnMic.setColorFilter(null) // Reseteamos color
                            
                            // Si la similitud supera 0.45, lo damos por válido (umbral ajustado)
                        if (similarity > 0.45) {
                            val prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE)
                            prefs.edit().putString("current_user_dni", dni).apply()
                            
                            // Mostramos la puntuación y entramos
                            Toast.makeText(this@LoginActivity, "¡Voz Verificada! Confianza: %.2f".format(similarity), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                                // No se parece suficiente
                                Toast.makeText(this@LoginActivity, "Acceso Denegado. Confianza: %.2f".format(similarity), Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onError(message: String) {
                        runOnUiThread {
                            isRecording = false
                            btnMic.setColorFilter(null)
                            Toast.makeText(this@LoginActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                })

            }, onFailure = { error ->
                Toast.makeText(this, "No se encontró el usuario: $error", Toast.LENGTH_SHORT).show()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiamos los recursos del helper
        biometricHelper.stop()
    }
}