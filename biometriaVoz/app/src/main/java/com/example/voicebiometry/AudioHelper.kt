package com.example.voicebiometry

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.mfcc.MFCC
import java.util.ArrayList
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class AudioHelper {

    companion object {
        private const val SAMPLE_RATE = 22050
        private const val BUFFER_SIZE = 1024
        private const val OVERLAP = 512
        // MFCC Parameters
        private const val AMOUNT_OF_CEPSTRUM_COEF = 13
        private const val AMOUNT_OF_MEL_FILTERS = 30
        private const val LOWER_FILTER_FREQ = 133f
        private const val UPPER_FILTER_FREQ = 3000f

        // Dynamic Time Warping (DTW) implementation to compare two sequences of MFCC vectors
        fun calculateDTW(seq1: List<FloatArray>, seq2: List<FloatArray>): Double {
            val n = seq1.size
            val m = seq2.size

            if (n == 0 || m == 0) return Double.MAX_VALUE

            val dtw = Array(n + 1) { DoubleArray(m + 1) }

            for (i in 0..n) {
                for (j in 0..m) {
                    dtw[i][j] = Double.MAX_VALUE
                }
            }
            dtw[0][0] = 0.0

            for (i in 1..n) {
                for (j in 1..m) {
                    val cost = euclideanDistance(seq1[i - 1], seq2[j - 1])
                    dtw[i][j] = cost + min(
                        dtw[i - 1][j], // insertion
                        min(
                            dtw[i][j - 1], // deletion
                            dtw[i - 1][j - 1] // match
                        )
                    )
                }
            }

            return dtw[n][m]
        }

        private fun euclideanDistance(v1: FloatArray, v2: FloatArray): Double {
            var sum = 0.0
            val length = min(v1.size, v2.size)
            for (i in 0 until length) {
                sum += (v1[i] - v2[i]).toDouble().pow(2.0)
            }
            return sqrt(sum)
        }
    }

    private var dispatcher: AudioDispatcher? = null
    private var audioThread: Thread? = null
    private val mfccList = ArrayList<FloatArray>()
    private var isRecording = false

    fun interface AudioListener {
        fun onProcessingFinished(mfccFeatures: List<FloatArray>)
    }

    fun startRecording() {
        if (isRecording) return
        isRecording = true
        mfccList.clear()

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, OVERLAP)

        val mfccObj = MFCC(BUFFER_SIZE, SAMPLE_RATE, AMOUNT_OF_CEPSTRUM_COEF, AMOUNT_OF_MEL_FILTERS, LOWER_FILTER_FREQ, UPPER_FILTER_FREQ)

        dispatcher?.addAudioProcessor(mfccObj)
        dispatcher?.addAudioProcessor(object : AudioProcessor {
            override fun process(audioEvent: AudioEvent): Boolean {
                if (isRecording) {
                    val mfcc = mfccObj.mfcc
                    // Clone to avoid reference issues
                    val mfccCopy = mfcc.clone()
                    mfccList.add(mfccCopy)
                }
                return true
            }

            override fun processingFinished() {
                // Done
            }
        })

        audioThread = Thread(dispatcher, "Audio Dispatcher")
        audioThread?.start()
    }

    fun stopRecording(listener: AudioListener?) {
        if (!isRecording) return
        isRecording = false

        dispatcher?.stop()
        try {
            audioThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        // Return features
        listener?.onProcessingFinished(ArrayList(mfccList))
    }
}
