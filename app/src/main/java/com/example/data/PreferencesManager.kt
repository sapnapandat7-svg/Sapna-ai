package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sapna_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LANG_MODE = "key_lang_mode"
        private const val KEY_SPEECH_RATE = "key_speech_rate"
        private const val KEY_SPEECH_PITCH = "key_speech_pitch"
        private const val KEY_AUTO_SPEAK = "key_auto_speak"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_AI_PROVIDER = "key_ai_provider"
        private const val KEY_CUSTOM_API_KEY = "key_custom_api_key"
        private const val KEY_CUSTOM_ENDPOINT = "key_custom_endpoint"
    }

    fun loadSettings(): AppSettings {
        val langCode = prefs.getString(KEY_LANG_MODE, LanguageMode.AUTO.code) ?: LanguageMode.AUTO.code
        val langMode = LanguageMode.values().firstOrNull { it.code == langCode } ?: LanguageMode.AUTO
        val speechRate = prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
        val pitch = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f)
        val autoSpeak = prefs.getBoolean(KEY_AUTO_SPEAK, true)
        val haptic = prefs.getBoolean(KEY_HAPTIC, true)
        val aiProvider = prefs.getString(KEY_AI_PROVIDER, "Gemini 3.5 Flash") ?: "Gemini 3.5 Flash"
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        val customEndpoint = prefs.getString(KEY_CUSTOM_ENDPOINT, "") ?: ""

        return AppSettings(
            languageMode = langMode,
            speechRate = speechRate,
            pitch = pitch,
            autoSpeak = autoSpeak,
            hapticFeedback = haptic,
            aiProvider = aiProvider,
            customApiKey = customKey,
            customEndpoint = customEndpoint
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putString(KEY_LANG_MODE, settings.languageMode.code)
            putFloat(KEY_SPEECH_RATE, settings.speechRate)
            putFloat(KEY_SPEECH_PITCH, settings.pitch)
            putBoolean(KEY_AUTO_SPEAK, settings.autoSpeak)
            putBoolean(KEY_HAPTIC, settings.hapticFeedback)
            putString(KEY_AI_PROVIDER, settings.aiProvider)
            putString(KEY_CUSTOM_API_KEY, settings.customApiKey)
            putString(KEY_CUSTOM_ENDPOINT, settings.customEndpoint)
            apply()
        }
    }
}
