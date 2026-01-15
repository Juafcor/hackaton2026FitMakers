package com.example.hackathonfitmakers.utils

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.SpeakerModel
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import kotlin.math.sqrt

/**
 * Clase que gestiona el reconocimiento de voz biométrico usando VOSK.
 * Se encarga de cargar los modelos de IA, grabar audio y extraer la "huella de voz".
 */
class BiometricHelper(private val context: Context, private val onStatus: (String) -> Unit) {

    private var model: Model? = null
    private var spkModel: SpeakerModel? = null
    private var speechService: SpeechService? = null
    private var currentRecognizer: Recognizer? = null

    /**
     * Interfaz para comunicarnos con la Actividad cuando pase algo (éxito o error).
     */
    interface BiometricListener {
        fun onSpeakerVectorExtracted(vector: String)
        fun onError(message: String)
    }

    /**
     * Prepara todo el sistema. Carga los modelos de reconocimiento desde los assets de la app.
     * Esto se hace en un hilo secundario porque puede tardar un poco.
     */
    fun setup(onReady: () -> Unit) {
        Thread {
            try {
                // 1. Configurar Modelo de Voz (texto)
                val modelPath = File(context.getExternalFilesDir(null), "model")
                if (!modelPath.exists() || modelPath.list()?.isEmpty() == true) {
                     // Si no lo tenemos descomprimido, lo copiamos de assets
                     copyAssets(context, "model/vosk-model-small-en-us-0.15", modelPath)
                }
                this.model = Model(modelPath.absolutePath)

                // 2. Configurar Modelo del Hablante (identificación biométrica)
                val spkPath = File(context.getExternalFilesDir(null), "model-spk")
                if (!spkPath.exists() || spkPath.list()?.isEmpty() == true) {
                     copyAssets(context, "model-spk/vosk-model-spk-0.4", spkPath)
                }
                this.spkModel = SpeakerModel(spkPath.absolutePath)

                checkReady(onReady)
            } catch (e: Exception) {
                onStatus("Error inicializando modelos: ${e.message}")
                Log.e("BiometricHelper", "Error de Init", e)
            }
        }.start()
    }

    /**
     * Copia recursivamente archivos desde la carpeta assets a una carpeta del dispositivo.
     */
    private fun copyAssets(context: Context, assetPath: String, targetDir: File) {
        if (!targetDir.exists()) targetDir.mkdirs()
        val assets = context.assets.list(assetPath) ?: return
        if (assets.isEmpty()) {
            copyFile(context, assetPath, targetDir)
        } else {
            for (asset in assets) {
                val subPath = if (assetPath == "") asset else "$assetPath/$asset"
                val subTarget = File(targetDir, asset)
                if (context.assets.list(subPath)?.isNotEmpty() == true) {
                    copyAssets(context, subPath, subTarget)
                } else {
                    copyFile(context, subPath, targetDir)
                }
            }
        }
    }

    /**
     * Copia un solo archivo físico.
     */
    private fun copyFile(context: Context, assetPath: String, targetDir: File) {
        val fileName = File(assetPath).name
        val outFile = File(targetDir, fileName)
        try {
            val inputStream = context.assets.open(assetPath)
            val outputStream = java.io.FileOutputStream(outFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("BiometricHelper", "Fallo al copiar $assetPath", e)
        }
    }

    /**
     * Comprueba si ambos modelos están cargados correctamente.
     */
    private fun checkReady(onReady: () -> Unit) {
        if (model != null && spkModel != null) {
            onStatus("Modelos Listos")
            onReady()
        }
    }

    /**
     * Empieza a escuchar por el micrófono para sacar la huella de voz.
     */
    fun startRecording(listener: BiometricListener) {
        if (model == null || spkModel == null) {
            listener.onError("Modelos no inicializados")
            return
        }

        try {
            // Creamos el reconocedor con soporte para identificación de hablante (spkModel)
            currentRecognizer = Recognizer(model, 16000.0f, spkModel)
            speechService = SpeechService(currentRecognizer, 16000.0f)

            speechService?.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {
                    Log.d("BiometricHelper", "Resultado Parcial: $hypothesis")
                    onStatus("Escuchando... (analizando)")
                }

                override fun onResult(hypothesis: String?) {
                    Log.d("BiometricHelper", "Resultado: $hypothesis")
                    hypothesis?.let {
                        val json = JSONObject(it)
                        if (json.has("spk")) {
                            Log.d("BiometricHelper", "¡Huella de voz encontrada!")
                            onStatus("Huella de voz calculada.")
                            // "spk" es un array de floats en JSON
                            val spkVector = json.getJSONArray("spk").toString()
                            listener.onSpeakerVectorExtracted(spkVector)
                            stopListening() 
                        } else {
                            Log.d("BiometricHelper", "No se detectó huella de voz en este fragmento.")
                            if (json.has("text") && json.getString("text").isNotEmpty()) {
                                 onStatus("Texto oído: ${json.getString("text")} (Esperando firma de voz...)")
                            }
                        }
                    }
                }

                override fun onFinalResult(hypothesis: String?) {
                     Log.d("BiometricHelper", "Resultado Final: $hypothesis")
                     hypothesis?.let {
                        val json = JSONObject(it)
                        if (json.has("spk")) {
                            val spkVector = json.getJSONArray("spk").toString()
                            listener.onSpeakerVectorExtracted(spkVector)
                            stopListening()
                        } else {
                            onStatus("Se detuvo sin detectar una voz clara. Inténtalo de nuevo.")
                        }
                    }
                }

                override fun onError(exception: Exception?) {
                    Log.e("BiometricHelper", "onError", exception)
                    listener.onError(exception?.message ?: "Error desconocido")
                }

                override fun onTimeout() {
                    Log.d("BiometricHelper", "onTimeout")
                    listener.onError("Tiempo agotado")
                }
            })
        } catch (e: Exception) {
            listener.onError(e.message ?: "Error iniciando el reconocedor")
        }
    }

    fun stopListening() {
        speechService?.stop()
    }

    fun stop() {
        shutdown()
    }

    fun shutdown() {
        speechService?.shutdown()
        speechService = null
        currentRecognizer = null
    }

    companion object {
        /**
         * Calcula matemáticamente cuánto se parecen dos vectores de voz.
         * Devuelve un número entre 0 (nada iguales) y 1 (idénticos).
         * Usamos la Similitud del Coseno.
         */
        fun calculateCosineSimilarity(vecAStr: String, vecBStr: String): Float {
            try {
                val jsonA = org.json.JSONArray(vecAStr)
                val jsonB = org.json.JSONArray(vecBStr)

                if (jsonA.length() != jsonB.length()) return 0f

                var dotProduct = 0.0
                var normA = 0.0
                var normB = 0.0

                for (i in 0 until jsonA.length()) {
                    val a = jsonA.getDouble(i)
                    val b = jsonB.getDouble(i)
                    dotProduct += a * b
                    normA += a * a
                    normB += b * b
                }

                return (dotProduct / (sqrt(normA) * sqrt(normB))).toFloat()
            } catch (e: Exception) {
                return 0f
            }
        }
    }
}
