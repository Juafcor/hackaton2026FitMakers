package com.example.voicebiometry

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
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
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.util.Collections

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PERMISSION_CODE = 200
    }

    private lateinit var audioHelper: AudioHelper
    private lateinit var etName: EditText
    private lateinit var tvStatus: TextView
    private lateinit var btnRecord: Button
    private var permissionGranted = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        tvStatus = findViewById(R.id.tvStatus)
        btnRecord = findViewById(R.id.btnRecord)
        audioHelper = AudioHelper()

        if (checkPermissions()) {
            permissionGranted = true
        } else {
            requestPermissions()
        }

        btnRecord.setOnTouchListener { _, event ->
            if (!permissionGranted) {
                Toast.makeText(this@RegisterActivity, "Microphone permission required", Toast.LENGTH_SHORT).show()
                return@setOnTouchListener false
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tvStatus.text = "Recording..."
                    audioHelper.startRecording()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    tvStatus.text = "Processing..."
                    audioHelper.stopRecording { mfccFeatures ->
                        saveUser(mfccFeatures)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun saveUser(features: List<FloatArray>) {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            runOnUiThread {
                tvStatus.text = "Registration Failed: Enter Name"
                Toast.makeText(this@RegisterActivity, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (features.isEmpty()) {
            runOnUiThread { tvStatus.text = "Registration Failed: No Audio" }
            return
        }

        try {
            val file = File(filesDir, "$name.features")
            val fos = FileOutputStream(file)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(ArrayList(features)) // Ensure it is serializable (ArrayList is)
            oos.close()
            fos.close()
            runOnUiThread {
                Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                finish() // Go back to main
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread { tvStatus.text = "Error saving: ${e.message}" }
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
