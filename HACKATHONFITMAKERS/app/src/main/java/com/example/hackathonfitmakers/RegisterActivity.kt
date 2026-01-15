package com.example.hackathonfitmakers

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Buscamos los controles en la pantalla
        val etName = findViewById<EditText>(R.id.etName)
        val btnFinish = findViewById<Button>(R.id.btnFinishRegister)
<<<<<<< Updated upstream

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
=======
        val btnMic = findViewById<ImageButton>(R.id.btnMicRegister)

        // Initialize BiometricHelper
        biometricHelper = BiometricHelper(this) { status ->
             // Optional: Display status
        }
        
        biometricHelper.setup {
             runOnUiThread { Toast.makeText(this, "Sistema de voz listo", Toast.LENGTH_SHORT).show() }
        }

        // Configurar botón del micrófono
        btnMic.setOnClickListener {
            if (isRecording) {
                 biometricHelper.stopListening()
                 isRecording = false
                 btnMic.setColorFilter(ContextCompat.getColor(this, R.color.dark_blue)) // Reset color
                 Toast.makeText(this, "Grabación detenida", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                 ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                 return@setOnClickListener
            }

            isRecording = true
            btnMic.setColorFilter(android.graphics.Color.RED) // Visual feedback
            Toast.makeText(this, "Escuchando... Hable ahora.", Toast.LENGTH_SHORT).show()
            
            biometricHelper.startRecording(object : BiometricHelper.BiometricListener {
                override fun onSpeakerVectorExtracted(vector: String) {
                    capturedVector = vector
                    runOnUiThread {
                        isRecording = false
                        btnMic.setColorFilter(ContextCompat.getColor(this@RegisterActivity, R.color.dark_blue)) // Reset color
                        Toast.makeText(this@RegisterActivity, "¡Captura de voz exitosa!", Toast.LENGTH_SHORT).show()
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

            // Pathologies (CheckBoxes) - Layout nuevo usa IDs CheckBox (cbUpperBody, cbLowerBody) dentro de LinearLayout
            val cbUpper = findViewById<CheckBox>(R.id.cbUpperBody)
            val cbLower = findViewById<CheckBox>(R.id.cbLowerBody)
            val typePathologies = mutableListOf<String>()
            if (cbUpper.isChecked) typePathologies.add("Superior")
            if (cbLower.isChecked) typePathologies.add("Inferior")
            
            val street = findViewById<EditText>(R.id.etAddressStreet).text.toString().trim()
            val num = findViewById<EditText>(R.id.etAddressNum).text.toString().trim()
            val cp = findViewById<EditText>(R.id.etAddressCP).text.toString().trim()

            if (name.isEmpty() || dni.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor complete Nombre, DNI y Contraseña",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (capturedVector == null) {
                Toast.makeText(
                    this,
                    "Por favor registre su voz primero",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Aquí en lugar de guardar directamente, mostramos el diálogo de términos
                val userToRegister = prepareUserObject(name, dni, password, weightStr, heightStr, genderStr, typePathologies, street, num, cp)
                if (userToRegister != null) {
                    showTermsDialog(userToRegister)
                }
            }
        }
    }

    private fun prepareUserObject(name: String, dni: String, pass: String, weight: String, height: String, sex: String, pathologies: List<String>, street: String, num: String, cp: String): User? {
         // Parse captured vector string to List<Float>
        val vozList = ArrayList<Float>()
        try {
            val jsonArray = JSONArray(capturedVector)
            for (i in 0 until jsonArray.length()) {
                vozList.add(jsonArray.getDouble(i).toFloat())
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing voice data", Toast.LENGTH_SHORT).show()
            return null
        }

        // Prepare complex fields
        val peso = weight.toIntOrNull() ?: 0
        val altura = height.toIntOrNull() ?: 0
        val sexoList = if (sex.isNotEmpty()) listOf(sex) else emptyList()
        
        val residencia = hashMapOf<String, Any>(
            "calle" to street,
            "numero" to num,
            "cp" to cp
        )

        return User(
            dni = dni,
            nombre = name,
            contrasena = pass,
            peso = peso,
            altura = altura,
            sexo = sexoList,
            patologias = pathologies,
            residencia = residencia,
            voz = vozList
        )
    }

    private fun showTermsDialog(user: User) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_terms, null)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cbAccept = dialogView.findViewById<CheckBox>(R.id.cbAcceptTerms)
        val btnAccept = dialogView.findViewById<Button>(R.id.btnAcceptTerms)
        val btnMoreInfo = dialogView.findViewById<Button>(R.id.btnMoreInfo)

        cbAccept.setOnCheckedChangeListener { _, isChecked ->
            btnAccept.isEnabled = isChecked
            btnAccept.alpha = if (isChecked) 1.0f else 0.4f
        }

        btnMoreInfo.setOnClickListener {
            val infoView = LayoutInflater.from(this).inflate(R.layout.dialog_more_info, null)
            val infoBuilder = AlertDialog.Builder(this)
            infoBuilder.setView(infoView)
            val infoDialog = infoBuilder.create()
            infoDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val btnClose = infoView.findViewById<Button>(R.id.btnCloseInfo)
            btnClose.setOnClickListener {
                infoDialog.dismiss()
            }
            
            infoDialog.show()
        }

        btnAccept.setOnClickListener {
            dialog.dismiss()
            completeRegistration(user)
        }

        dialog.show()
    }

    private fun completeRegistration(user: User) {
        FirestoreHelper.addUser(user, onSuccess = {
            Toast.makeText(
                this,
                "¡Registro completado!",
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }, onFailure = { errorMsg ->
            Toast.makeText(this, "Error in registration: $errorMsg", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricHelper.stop()
    }
>>>>>>> Stashed changes
}