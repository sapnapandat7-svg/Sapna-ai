package com.example.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionHelper {
    const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    const val CALL_PERMISSION = Manifest.permission.CALL_PHONE

    fun isAudioPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            AUDIO_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isCallPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CALL_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getRationale(permission: String): String {
        return when (permission) {
            AUDIO_PERMISSION ->
                "Sapna AI needs Microphone access to listen to your voice commands in Hindi and English. Your voice is converted directly to text on your device using Android Speech Recognition."
            CALL_PERMISSION ->
                "Sapna AI requires Phone Call access only when you explicitly confirm placing a call. The assistant will never dial numbers automatically without your confirmation."
            else -> "This permission is needed for assistant functionality."
        }
    }
}
