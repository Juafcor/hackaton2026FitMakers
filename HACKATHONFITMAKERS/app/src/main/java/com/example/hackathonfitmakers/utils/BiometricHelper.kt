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

class BiometricHelper(private val context: Context, private val onStatus: (String) -> Unit) {

    private var model: Model? = null
    private var spkModel: SpeakerModel? = null
    private var speechService: SpeechService? = null
    private var currentRecognizer: Recognizer? = null

    interface BiometricListener {
        fun onSpeakerVectorExtracted(vector: String)
        fun onError(message: String)
    }

    fun setup(onReady: () -> Unit) {
        Thread {
            try {
                // 1. Setup Speech Model
                val modelPath = File(context.getExternalFilesDir(null), "model")
                if (!modelPath.exists() || modelPath.list()?.isEmpty() == true) {
                     // Check if assets/model contains the subdirectory or files directly
                     copyAssets(context, "model/vosk-model-small-en-us-0.15", modelPath)
                }
                this.model = Model(modelPath.absolutePath)

                // 2. Setup Speaker Model
                val spkPath = File(context.getExternalFilesDir(null), "model-spk")
                if (!spkPath.exists() || spkPath.list()?.isEmpty() == true) {
                     copyAssets(context, "model-spk/vosk-model-spk-0.4", spkPath)
                }
                this.spkModel = SpeakerModel(spkPath.absolutePath)

                checkReady(onReady)
            } catch (e: Exception) {
                onStatus("Error initializing models: ${e.message}")
                Log.e("BiometricHelper", "Init error", e)
            }
        }.start()
    }

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
            Log.e("BiometricHelper", "Failed to copy $assetPath", e)
        }
    }

    private fun checkReady(onReady: () -> Unit) {
        if (model != null && spkModel != null) {
            onStatus("Models Ready")
            onReady()
        }
    }

    fun startRecording(listener: BiometricListener) {
        if (model == null || spkModel == null) {
            listener.onError("Models not initialized")
            return
        }

        try {
            // Include SpkModel in Recognizer
            currentRecognizer = Recognizer(model, 16000.0f, spkModel)
            speechService = SpeechService(currentRecognizer, 16000.0f)

            speechService?.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {
                    Log.d("BiometricHelper", "onPartialResult: $hypothesis")
                    onStatus("Listening... (partial)")
                }

                override fun onResult(hypothesis: String?) {
                    Log.d("BiometricHelper", "onResult: $hypothesis")
                    hypothesis?.let {
                        val json = JSONObject(it)
                        if (json.has("spk")) {
                            Log.d("BiometricHelper", "Speaker Vector found!")
                            onStatus("Voice Print Computed.")
                            val spkVector = json.getJSONArray("spk").toString()
                            listener.onSpeakerVectorExtracted(spkVector)
                            stopListening() // Use stopListening instead of shutdown
                        } else {
                            Log.d("BiometricHelper", "No speaker vector in result.")
                            if (json.has("text") && json.getString("text").isNotEmpty()) {
                                 onStatus("Heard: ${json.getString("text")} (Wait for signature...)")
                            }
                        }
                    }
                }

                override fun onFinalResult(hypothesis: String?) {
                     Log.d("BiometricHelper", "onFinalResult: $hypothesis")
                     hypothesis?.let {
                        val json = JSONObject(it)
                        if (json.has("spk")) {
                            val spkVector = json.getJSONArray("spk").toString()
                            listener.onSpeakerVectorExtracted(spkVector)
                            stopListening() // Use stopListening instead of shutdown
                        } else {
                            onStatus("Stopped without valid voice print. Try again.")
                        }
                    }
                }

                override fun onError(exception: Exception?) {
                    Log.e("BiometricHelper", "onError", exception)
                    listener.onError(exception?.message ?: "Unknown error")
                }

                override fun onTimeout() {
                    Log.d("BiometricHelper", "onTimeout")
                    listener.onError("Timeout")
                }
            })
        } catch (e: Exception) {
            listener.onError(e.message ?: "Error starting recognizer")
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
