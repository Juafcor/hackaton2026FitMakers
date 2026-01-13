package com.example.voicebiometry

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PERMISSION_CODE = 200
        // Normalized Threshold
        private const val DTW_THRESHOLD = 50.0 
    }

    private lateinit var audioHelper: AudioHelper
    private lateinit var etNameLogin: EditText
    private lateinit var tvStatusLogin: TextView
    private lateinit var btnRecordLogin: Button
    private var permissionGranted = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etNameLogin = findViewById(R.id.etNameLogin)
        tvStatusLogin = findViewById(R.id.tvStatusLogin)
        btnRecordLogin = findViewById(R.id.btnRecordLogin)
        audioHelper = AudioHelper()

        if (checkPermissions()) {
            permissionGranted = true
        } else {
            requestPermissions()
        }

        btnRecordLogin.setOnTouchListener { _, event ->
            if (!permissionGranted) {
                Toast.makeText(this@LoginActivity, "Microphone permission required", Toast.LENGTH_SHORT).show()
                return@setOnTouchListener false
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tvStatusLogin.text = "Recording..."
                    audioHelper.startRecording()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    tvStatusLogin.text = "Verifying..."
                    audioHelper.stopRecording { featureList ->
                        verifyUser(featureList)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun verifyUser(capturedFeatures: List<FloatArray>) {
        val name = etNameLogin.text.toString().trim()
        if (name.isEmpty()) {
            updateStatus("Enter Username")
            return
        }

        val file = File(filesDir, "$name.features")
        if (!file.exists()) {
            updateStatus("User not found")
            return
        }

        try {
            val fis = FileInputStream(file)
            val ois = ObjectInputStream(fis)
            @Suppress("UNCHECKED_CAST")
            val storedFeatures = ois.readObject() as? List<FloatArray>
            ois.close()
            fis.close()

            if (storedFeatures.isNullOrEmpty() || capturedFeatures.isEmpty()) {
                updateStatus("Invalid audio data")
                return
            }

            // Perform DTW
            val distance = AudioHelper.calculateDTW(storedFeatures, capturedFeatures)

            // Normalize distance by total length
            val normalizedDistance = distance / (storedFeatures.size + capturedFeatures.size)

            Log.d("VoiceAuth", "DTW Distance: $distance Normalized: $normalizedDistance")

            if (normalizedDistance < DTW_THRESHOLD) {
                runOnUiThread {
                    startActivity(Intent(this@LoginActivity, SuccessActivity::class.java))
                    finish()
                }
            } else {
                updateStatus("Verification Failed (Score: ${normalizedDistance.toInt()})")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            updateStatus("Error: ${e.message}")
        }
    }

    private fun updateStatus(msg: String) {
        runOnUiThread {
            tvStatusLogin.text = msg
            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
        }
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
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
