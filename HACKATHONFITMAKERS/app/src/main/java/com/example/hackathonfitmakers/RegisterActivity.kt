package com.example.hackathonfitmakers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PERMISSION_CODE = 200
    }

    private lateinit var biometricHelper: BiometricHelper
    private lateinit var etDni: EditText
    private lateinit var tvStatus: TextView
    private lateinit var btnRecord: Button
    private var permissionGranted = false
    private var capturedVector: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etDni = findViewById(R.id.etDniRegister)
        val btnFinish = findViewById<Button>(R.id.btnFinishRegister)
        btnRecord = findViewById(R.id.btnRecordVoice)
        tvStatus = findViewById(R.id.tvVoiceStatus)

        biometricHelper = BiometricHelper(this) { status ->
             runOnUiThread { tvStatus.text = status }
        }

        tvStatus.text = "Inicializando modelos..."
        biometricHelper.setup {
             runOnUiThread { tvStatus.text = "Listo para grabar" }
        }

        if (checkPermissions()) {
            permissionGranted = true
        } else {
            requestPermissions()
        }

        // Changed to Click Listener because Vosk stops automatically when it finds the vector
        btnRecord.setOnClickListener {
            if (!permissionGranted) {
                Toast.makeText(this, "Permiso de micrófono requerido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tvStatus.text = "Escuchando... Hable ahora."
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            
            biometricHelper.startRecording(object : BiometricHelper.BiometricListener {
                override fun onSpeakerVectorExtracted(vector: String) {
                    capturedVector = vector
                    runOnUiThread {
                        tvStatus.text = "Huella de voz capturada!"
                        tvStatus.setTextColor(ContextCompat.getColor(this@RegisterActivity, android.R.color.holo_green_dark))
                    }
                }

                override fun onError(message: String) {
                    runOnUiThread {
                        tvStatus.text = "Error: $message"
                        tvStatus.setTextColor(ContextCompat.getColor(this@RegisterActivity, android.R.color.holo_red_dark))
                    }
                }

                override fun onAudioLevel(rms: Float) {
                    runOnUiThread {
                        if (rms > 100) {
                            tvStatus.text = "Escuchando... Vol: ${rms.toInt()}"
                            tvStatus.setTextColor(ContextCompat.getColor(this@RegisterActivity, android.R.color.holo_green_dark))
                        } else {
                            tvStatus.text = "Escuchando... (Hable más fuerte)"
                            tvStatus.setTextColor(ContextCompat.getColor(this@RegisterActivity, android.R.color.holo_orange_dark))
                        }
                    }
                }
            })
        }

        btnFinish.setOnClickListener {
            val dni = etDni.text.toString().trim()
            val password = findViewById<EditText>(R.id.etPasswordRegister).text.toString().trim()

            if (dni.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce tu DNI", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce una contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (capturedVector == null) {
                Toast.makeText(this, "Por favor, graba tu voz como contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save Data
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString("password_$dni", password)
                putString("spk_$dni", capturedVector) // Save Speaker Vector
                apply()
            }

            Toast.makeText(this, "¡Registro completado!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
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