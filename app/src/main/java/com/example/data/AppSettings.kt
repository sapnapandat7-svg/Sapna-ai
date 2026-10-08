package com.example.data

enum class LanguageMode(val code: String, val displayName: String, val speechTag: String) {
    AUTO("auto", "Auto Detect (Hindi / English)", "hi-IN"),
    HINDI("hi-IN", "हिंदी (Hindi)", "hi-IN"),
    ENGLISH("en-US", "English (United States)", "en-US")
}

data class AppSettings(
    val languageMode: LanguageMode = LanguageMode.AUTO,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val autoSpeak: Boolean = true,
    val hapticFeedback: Boolean = true,
    val aiProvider: String = "Gemini 3.5 Flash",
    val customApiKey: String = "",
    val customEndpoint: String = ""
)
