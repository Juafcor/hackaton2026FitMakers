package com.example.hackathonfitmakers

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

// Wrapper for Vosk Voice Biometry
class BiometricHelper(private val context: Context, private val onStatus: (String) -> Unit) {

    private var model: Model? = null
    private var spkModel: SpeakerModel? = null
    private var audioRecord: android.media.AudioRecord? = null
    private var isRecording = false
    private var currentRecognizer: Recognizer? = null

    interface BiometricListener {
        fun onSpeakerVectorExtracted(vector: String)
        fun onError(message: String)
        fun onAudioLevel(rms: Float)
    }


    // Initialize Models (copy assets if needed)
    fun setup(onReady: () -> Unit) {
        Thread {
            try {
                // 1. Setup Speech Model
                val modelPath = File(context.getExternalFilesDir(null), "model")
                if (!modelPath.exists() || modelPath.list()?.isEmpty() == true) {
                     // Check if specific model folder exists in assets
                     val assetModelPath = "model/vosk-model-small-en-us-0.15"
                     copyAssets(context, assetModelPath, modelPath)
                }
                this.model = Model(modelPath.absolutePath)

                // 2. Setup Speaker Model
                val spkPath = File(context.getExternalFilesDir(null), "model-spk")
                if (!spkPath.exists() || spkPath.list()?.isEmpty() == true) {
                     val assetSpkPath = "model-spk/vosk-model-spk-0.4"
                     copyAssets(context, assetSpkPath, spkPath)
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

        stopRecording() // Ensure clean state

        Thread {
            try {
                // Configure AudioRecord
                val sampleRate = 16000
                val minBufferSize = android.media.AudioRecord.getMinBufferSize(
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_IN_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT
                )
                // Use a larger buffer (10x) to avoid "pcm_readi was late" errors on Emulator
                val bufferSize = Math.max(minBufferSize * 10, 8192)

                if (androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    listener.onError("Permission not granted")
                    return@Thread
                }

                audioRecord = android.media.AudioRecord(
                    android.media.MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_IN_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                if (audioRecord?.state != android.media.AudioRecord.STATE_INITIALIZED) {
                    listener.onError("AudioRecord init failed")
                    return@Thread
                }

                audioRecord?.startRecording()
                isRecording = true
                onStatus("Listening... (Speak Now)")

                currentRecognizer = Recognizer(model, sampleRate.toFloat(), spkModel)
                
                val buffer = ByteArray(4096)
                var silenceCount = 0
                val timeoutSamples = sampleRate * 8 * 2 // 8 seconds of bytes (approx)

                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (read > 0) {
                        // 1. Calculate RMS to check volume
                        var sum = 0.0
                        for (i in 0 until read step 2) {
                            val sample = (buffer[i].toInt() and 0xFF) or (buffer[i+1].toInt() shl 8)
                            sum += sample * sample
                        }
                        val rms = sqrt(sum / (read / 2))
                        
                        listener.onAudioLevel(rms.toFloat())

                        if (rms > 50) { // Threshold for "some sound"
                             // Log.d("BiometricHelper", "Mic Input RMS: $rms") 
                        }

                        // 2. Feed to Vosk
                        if (currentRecognizer!!.acceptWaveForm(buffer, read)) {
                            // Final Result
                            val result = currentRecognizer!!.result
                            handleResult(result, listener)
                        } else {
                            // Partial Result
                            val partial = currentRecognizer!!.partialResult
                            // Log.d("BiometricHelper", "Partial: $partial")
                        }
                    } else {
                        isRecording = false
                        listener.onError("Read error: $read")
                    }
                }
            } catch (e: Exception) {
                Log.e("BiometricHelper", "Recording error", e)
                listener.onError("Rec Error: ${e.message}")
            }
        }.start()
    }

    private fun handleResult(jsonResult: String, listener: BiometricListener) {
        try {
            Log.d("BiometricHelper", "Result: $jsonResult")
            val json = JSONObject(jsonResult)
            if (json.has("spk")) {
                 val spkVector = json.getJSONArray("spk").toString()
                 isRecording = false
                 stopRecording()
                 listener.onSpeakerVectorExtracted(spkVector)
            } else if (json.has("text") && json.getString("text").isNotEmpty()) {
                 Log.w("BiometricHelper", "Text recognized but NO Vector. Text: ${json.getString("text")}")
                 onStatus("Heard text, but no voice print yet...")
            }
        } catch (e: Exception) {
            Log.e("BiometricHelper", "JSON Error", e)
        }
    }

    fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            
            // Check final result logic if needed, but for now just cleanup
            currentRecognizer = null // Or keep it if you want to reuse (but we recreate each time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        stopRecording()
        // shutdown if needed
    }

    fun shutdown() {
        stop()
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
