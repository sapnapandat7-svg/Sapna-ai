package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechManager(
    private val context: Context,
    private val onInitResult: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val hindiLocale = Locale.forLanguageTag("hi-IN")
    private val englishLocale = Locale.US

    var onSpeakingStarted: (() -> Unit)? = null
    var onSpeakingFinished: (() -> Unit)? = null
    var onSpeakingError: ((String) -> Unit)? = null

    companion object {
        private const val TAG = "TextToSpeechManager"
        private const val UTTERANCE_ID = "sapna_ai_response_utterance"
    }

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupUtteranceListener()
            Log.d(TAG, "TTS successfully initialized.")
            onInitResult(true)
        } else {
            isInitialized = false
            Log.e(TAG, "TTS initialization failed with status $status")
            onInitResult(false)
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onSpeakingStarted?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                onSpeakingFinished?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onSpeakingError?.invoke("Text to speech error")
                onSpeakingFinished?.invoke()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                onSpeakingError?.invoke("TTS error (code $errorCode)")
                onSpeakingFinished?.invoke()
            }
        })
    }

    fun isReady(): Boolean = isInitialized

    fun speak(
        text: String,
        speechRate: Float = 1.0f,
        pitch: Float = 1.0f
    ) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS is not initialized yet")
            return
        }

        try {
            stop()

            tts?.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

            // Detect if text contains Hindi (Devanagari) characters
            val containsHindi = text.any { it in '\u0900'..'\u097F' }
            val targetLocale = if (containsHindi) hindiLocale else englishLocale

            val langResult = tts?.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English
                tts?.setLanguage(englishLocale)
            }

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
            }

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error in TTS speak: ${e.message}")
            onSpeakingFinished?.invoke()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS: ${e.message}")
        }
    }

    fun isSpeaking(): Boolean {
        return try {
            tts?.isSpeaking ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS: ${e.message}")
        }
    }
}
