package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognitionManager(
    private val context: Context,
    private val onReady: () -> Unit,
    private val onListeningChanged: (Boolean) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    companion object {
        private const val TAG = "SpeechRecognizerMgr"

        fun isAvailable(context: Context): Boolean {
            return SpeechRecognizer.isRecognitionAvailable(context)
        }
    }

    private fun ensureRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }
        }
    }

    fun startListening(preferredLanguage: String = "hi-IN") {
        if (!isAvailable(context)) {
            onError("Speech recognition service is not available on this device.")
            return
        }

        try {
            stopListening()
            ensureRecognizer()

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

                if (preferredLanguage == "auto") {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra("android.speech.extra.ADDITIONAL_LANGUAGES", arrayOf("en-US", "hi-IN"))
                } else {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, preferredLanguage)
                }
            }

            speechRecognizer?.startListening(intent)
            isListening = true
            onListeningChanged(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition: ${e.message}")
            isListening = false
            onListeningChanged(false)
            onError("Failed to start listening: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            if (isListening) {
                speechRecognizer?.stopListening()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listener: ${e.message}")
        } finally {
            isListening = false
            onListeningChanged(false)
            onRmsChanged(0f)
        }
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling listener: ${e.message}")
        } finally {
            isListening = false
            onListeningChanged(false)
            onRmsChanged(0f)
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer: ${e.message}")
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onReady()
            }

            override fun onBeginningOfSpeech() {
                isListening = true
                onListeningChanged(true)
            }

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB usually ranges from -2 to 10 dB. Normalize to 0.0 .. 1.0
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                onRmsChanged(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                onListeningChanged(false)
                onRmsChanged(0f)
            }

            override fun onError(errorCode: Int) {
                isListening = false
                onListeningChanged(false)
                onRmsChanged(0f)

                val message = when (errorCode) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check microphone."
                    SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer client error."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                    SpeechRecognizer.ERROR_NETWORK -> "Network connection issue during speech recognition."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak again."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy, please try again."
                    SpeechRecognizer.ERROR_SERVER -> "Speech server error, please retry."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected within time."
                    else -> "Speech recognition error (Code $errorCode)."
                }
                Log.w(TAG, "SpeechRecognizer error: $errorCode -> $message")
                // Only propagate actionable errors (e.g. not quiet timeouts)
                if (errorCode != SpeechRecognizer.ERROR_NO_MATCH && errorCode != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    onError(message)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim()
                if (!recognizedText.isNullOrEmpty()) {
                    onFinalResult(recognizedText)
                }
                isListening = false
                onListeningChanged(false)
                onRmsChanged(0f)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull()?.trim()
                if (!partialText.isNullOrEmpty()) {
                    onPartialResult(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
