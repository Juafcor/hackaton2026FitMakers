package com.example.hackathonfitmakers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PERMISSION_CODE = 200
        // Similarity Threshold (> 0.45 suggests same speaker)
        private const val SIMILARITY_THRESHOLD = 0.45f
    }

    private lateinit var llLoginFields: LinearLayout
    private lateinit var etDni: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvStatus: TextView
    private lateinit var btnVoice: Button
    
    private lateinit var biometricHelper: BiometricHelper
    private var permissionGranted = false
    


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnShowLogin = findViewById<Button>(R.id.btnShowLogin)
        val btnSubmitLogin = findViewById<Button>(R.id.btnSubmitLogin)
        val btnGoToRegister = findViewById<Button>(R.id.btnGoToRegister)
        
        llLoginFields = findViewById(R.id.llLoginFields)
        etDni = findViewById(R.id.etDniLogin)
        etPassword = findViewById(R.id.etPassword)
        
        // Voice UI
        tvStatus = findViewById(R.id.tvLoginStatus)
        btnVoice = findViewById(R.id.btnVoiceLogin)

        biometricHelper = BiometricHelper(this) { status ->
             runOnUiThread { tvStatus.text = status }
        }

        tvStatus.text = "Inicializando modelos..."
        biometricHelper.setup {
             runOnUiThread { tvStatus.text = "Listo para verificar" }
        }

        if (checkPermissions()) {
            permissionGranted = true
        } else {
            requestPermissions()
        }

        btnShowLogin.setOnClickListener {
            if (llLoginFields.visibility == View.GONE) {
                llLoginFields.visibility = View.VISIBLE
                btnShowLogin.visibility = View.GONE
            }
        }

        btnSubmitLogin.setOnClickListener {
            val dni = etDni.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (dni.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Verify Password
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val storedPass = sharedPref.getString("password_$dni", null)

                if (storedPass == null) {
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                } else if (storedPass == pass) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnVoice.setOnClickListener {
            val dni = etDni.text.toString().trim()
            if (dni.isEmpty()) {
                tvStatus.text = "Introduce tu DNI primero"
                return@setOnClickListener
            }

            if (!permissionGranted) {
                Toast.makeText(this, "Permiso de micrófono requerido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val storedVector = sharedPref.getString("spk_$dni", null)

            if (storedVector == null) {
                tvStatus.text = "Usuario no registrado o sin huella de voz"
                return@setOnClickListener
            }

            tvStatus.text = "Escuchando... Hable ahora."
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))

            biometricHelper.startRecording(object : BiometricHelper.BiometricListener {
                override fun onSpeakerVectorExtracted(vector: String) {
                    val similarity = BiometricHelper.calculateCosineSimilarity(storedVector, vector)
                    Log.d("VoiceAuth", "Similarity: $similarity")

                    runOnUiThread {
                        if (similarity > SIMILARITY_THRESHOLD) {
                             Toast.makeText(this@LoginActivity, "¡Autenticación exitosa! Score: $similarity", Toast.LENGTH_LONG).show()
                             startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                             finish()
                        } else {
                             tvStatus.text = "Acceso Denegado. Score: $similarity"
                             tvStatus.setTextColor(ContextCompat.getColor(this@LoginActivity, android.R.color.holo_red_dark))
                        }
                    }
                }

                override fun onError(message: String) {
                    runOnUiThread {
                        tvStatus.text = "Error: $message"
                        tvStatus.setTextColor(ContextCompat.getColor(this@LoginActivity, android.R.color.holo_red_dark))
                    }
                }

                override fun onAudioLevel(rms: Float) {
                     runOnUiThread {
                        if (rms > 100) {
                            tvStatus.text = "Escuchando... Vol: ${rms.toInt()}"
                        }
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricHelper.stop()
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}