package com.example.hackathonfitmakers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hackathonfitmakers.model.User
import com.example.hackathonfitmakers.utils.BiometricHelper
import com.example.hackathonfitmakers.utils.FirestoreHelper
import org.json.JSONArray

/**
 * Actividad de Registro. Aquí recogemos todos los datos del usuario, 
 * incluyendo la biometría de voz.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var biometricHelper: BiometricHelper
    // Aquí guardaremos temporalmente el vector de voz capturado
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

        // Inicializamos el helper de reconocimiento de voz
        biometricHelper = BiometricHelper(this) { status ->
             // Podemos usar esto para mostrar estado si queremos
        }
        
        biometricHelper.setup {
             runOnUiThread { Toast.makeText(this, "Sistema de voz listo", Toast.LENGTH_SHORT).show() }
        }

        // Configuración del botón para grabar la voz
        btnMic.setOnClickListener {
            // Si ya estamos grabando, paramos
            if (isRecording) {
                 biometricHelper.stopListening()
                 isRecording = false
                 btnMic.setColorFilter(ContextCompat.getColor(this, R.color.dark_blue)) // Volvemos al color azul
                 Toast.makeText(this, "Grabación detenida", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
            }

            // Verificamos permisos de micrófono
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                 ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
                 return@setOnClickListener
            }

            // Iniciamos grabación
            isRecording = true
            btnMic.setColorFilter(android.graphics.Color.RED) // Rojo para indicar que graba
            Toast.makeText(this, "Escuchando... Hable ahora.", Toast.LENGTH_SHORT).show()
            
            // Empezamos a escuchar con el helper
            biometricHelper.startRecording(object : BiometricHelper.BiometricListener {
                override fun onSpeakerVectorExtracted(vector: String) {
                    // ¡Éxito! Guardamos el vector de voz
                    capturedVector = vector
                    runOnUiThread {
                        isRecording = false
                        btnMic.setColorFilter(ContextCompat.getColor(this@RegisterActivity, R.color.dark_blue)) // Resetear color
                        Toast.makeText(this@RegisterActivity, "¡Captura de voz exitosa!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(message: String) {
                    runOnUiThread {
                        isRecording = false
                        btnMic.setColorFilter(ContextCompat.getColor(this@RegisterActivity, R.color.dark_blue))
                        Toast.makeText(this@RegisterActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        // Cuando el usuario pulsa "Terminar Registro"
        btnFinish.setOnClickListener {
            // Recogemos todos los datos escritos
            val name = etName.text.toString().trim()
            val dni = etDni.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            val weightStr = findViewById<EditText>(R.id.etWeight).text.toString().trim()
            val heightStr = findViewById<EditText>(R.id.etHeight).text.toString().trim()
            
            // Sexo (RadioGroup)
            val rgSex = findViewById<android.widget.RadioGroup>(R.id.rgSex)
            val genderStr = when (rgSex.checkedRadioButtonId) {
                R.id.rbMale -> "Hombre"
                R.id.rbFemale -> "Mujer"
                else -> ""
            }

            // Patologías (CheckBoxes) - Miramos si marcó la parte superior o inferior
            val cbUpper = findViewById<CheckBox>(R.id.cbUpperBody)
            val cbLower = findViewById<CheckBox>(R.id.cbLowerBody)
            val typePathologies = mutableListOf<String>()
            if (cbUpper.isChecked) typePathologies.add("Superior")
            if (cbLower.isChecked) typePathologies.add("Inferior")
            
            // Dirección
            val street = findViewById<EditText>(R.id.etAddressStreet).text.toString().trim()
            val num = findViewById<EditText>(R.id.etAddressNum).text.toString().trim()
            val cp = findViewById<EditText>(R.id.etAddressCP).text.toString().trim()

            // Validaciones básicas
            if (name.isEmpty() || dni.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor complete Nombre, DNI y Contraseña",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (capturedVector == null) {
                Toast.makeText(
                    this,
                    "Por favor registre su voz primero pulsando el micrófono",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Preparamos el objeto User y mostramos los términos
                val userToRegister = prepareUserObject(name, dni, password, weightStr, heightStr, genderStr, typePathologies, street, num, cp)
                if (userToRegister != null) {
                    showTermsDialog(userToRegister)
                }
            }
        }
    }

    /**
     * Esta función coge todos los datos sueltos y crea un objeto User ordenado.
     * También convierte el vector de voz que estaba en texto a una lista de números.
     */
    private fun prepareUserObject(name: String, dni: String, pass: String, weight: String, height: String, sex: String, pathologies: List<String>, street: String, num: String, cp: String): User? {
        val vozList = ArrayList<Float>()
        try {
            // Convertimos el String "[1.2, 3.4...]" a una lista real de Floats
            val jsonArray = JSONArray(capturedVector)
            for (i in 0 until jsonArray.length()) {
                vozList.add(jsonArray.getDouble(i).toFloat())
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error procesando los datos de voz", Toast.LENGTH_SHORT).show()
            return null
        }

        // Convertimos campos numéricos de forma segura
        val peso = weight.toIntOrNull() ?: 0
        val altura = height.toIntOrNull() ?: 0
        val sexoList = if (sex.isNotEmpty()) listOf(sex) else emptyList()
        
        // Creamos el mapa de dirección
        val residencia = hashMapOf<String, Any>(
            "calle" to street,
            "numero" to num,
            "cp" to cp
        )

        // Devolvemos el usuario completo
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

    /**
     * Mostramos un diálogo emergente con los términos y condiciones.
     * El usuario debe aceptar el CheckBox para poder continuar.
     */
    private fun showTermsDialog(user: User) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_terms, null)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // No se puede cerrar pinchando fuera

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cbAccept = dialogView.findViewById<CheckBox>(R.id.cbAcceptTerms)
        val btnAccept = dialogView.findViewById<Button>(R.id.btnAcceptTerms)
        val btnMoreInfo = dialogView.findViewById<Button>(R.id.btnMoreInfo)

        // Solo habilitamos el botón de aceptar si marcan el check
        cbAccept.setOnCheckedChangeListener { _, isChecked ->
            btnAccept.isEnabled = isChecked
            btnAccept.alpha = if (isChecked) 1.0f else 0.4f
        }

        // Botón para ver más información (abre otro diálogo)
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

        // Al aceptar, cerramos el diálogo y procedemos a guardar
        btnAccept.setOnClickListener {
            dialog.dismiss()
            completeRegistration(user)
        }

        dialog.show()
    }

    /**
     * Guardamos el usuario en Firebase Firestore.
     */
    private fun completeRegistration(user: User) {
        FirestoreHelper.addUser(user, onSuccess = {
            Toast.makeText(
                this,
                "¡Registro completado!",
                Toast.LENGTH_LONG
            ).show()

            // Volvemos al Login limpiando la pila de actividades
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }, onFailure = { errorMsg ->
            Toast.makeText(this, "Error en el registro: $errorMsg", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiamos helper al salir
        biometricHelper.stop()
    }
}