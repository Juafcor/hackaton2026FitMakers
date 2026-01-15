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
import com.example.hackathonfitmakers.model.User
import com.example.hackathonfitmakers.utils.BiometricHelper
import com.example.hackathonfitmakers.utils.FirestoreHelper
import org.json.JSONArray

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
             // Optional: Display status
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
            val name = etName.text.toString().trim()
            val dni = etDni.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            // New Fields
            val weightStr = findViewById<EditText>(R.id.etWeight).text.toString().trim()
            val heightStr = findViewById<EditText>(R.id.etHeight).text.toString().trim()
            
            // Sexo (RadioGroup)
            val rgSex = findViewById<android.widget.RadioGroup>(R.id.rgSex)
            val genderStr = when (rgSex.checkedRadioButtonId) {
                R.id.rbMale -> "Hombre"
                R.id.rbFemale -> "Mujer"
                else -> ""
            }

            // Pathologies (CheckBoxes)
            val cbUpper = findViewById<android.widget.CheckBox>(R.id.cbUpperBody)
            val cbLower = findViewById<android.widget.CheckBox>(R.id.cbLowerBody)
            val typePathologies = mutableListOf<String>()
            if (cbUpper.isChecked) typePathologies.add("Superior")
            if (cbLower.isChecked) typePathologies.add("Inferior")
            
            val street = findViewById<EditText>(R.id.etAddressStreet).text.toString().trim()
            val num = findViewById<EditText>(R.id.etAddressNum).text.toString().trim()
            val cp = findViewById<EditText>(R.id.etAddressCP).text.toString().trim()

            if (name.isEmpty() || dni.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please fill in all main fields (Name, DNI, Pass)",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (capturedVector == null) {
                Toast.makeText(
                    this,
                    "Please record your voice first",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Parse captured vector string to List<Float>
                val vozList = ArrayList<Float>()
                try {
                    val jsonArray = JSONArray(capturedVector)
                    for (i in 0 until jsonArray.length()) {
                        vozList.add(jsonArray.getDouble(i).toFloat())
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error processing voice data", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Prepare complex fields
                val peso = weightStr.toIntOrNull() ?: 0
                val altura = heightStr.toIntOrNull() ?: 0
                val sexo = if (genderStr.isNotEmpty()) listOf(genderStr) else emptyList()
                val patologias = typePathologies.toList()
                
                val residencia = hashMapOf<String, Any>(
                    "calle" to street,
                    "numero" to num,
                    "cp" to cp
                )

                val newUser = User(
                    dni = dni,
                    nombre = name,
                    contrasena = password,
                    peso = peso,
                    altura = altura,
                    sexo = sexo,
                    patologias = patologias,
                    residencia = residencia,
                    voz = vozList
                )

                FirestoreHelper.addUser(newUser, onSuccess = {
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
                }, onFailure = { errorMsg ->
                    Toast.makeText(this, "Error in registration: $errorMsg", Toast.LENGTH_SHORT).show()
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricHelper.stop()
    }
}