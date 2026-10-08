package com.example.apps

import android.graphics.drawable.Drawable

data class InstalledApp(
    val label: String,
    val packageName: String,
    val isSystemApp: Boolean = false,
    val icon: Drawable? = null
)
