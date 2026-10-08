package com.example.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log

sealed class AppLaunchResult {
    data class Success(val appName: String, val packageName: String) : AppLaunchResult()
    data class NotFound(val requestedName: String) : AppLaunchResult()
    data class Error(val appName: String, val message: String) : AppLaunchResult()
}

class AppManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private var cachedApps: List<InstalledApp> = emptyList()

    private val knownAppAliases = mapOf(
        "youtube" to listOf("com.google.android.youtube"),
        "यूट्यूब" to listOf("com.google.android.youtube"),
        "whatsapp" to listOf("com.whatsapp", "com.whatsapp.w4b"),
        "व्हाट्सएप" to listOf("com.whatsapp", "com.whatsapp.w4b"),
        "वाट्सएप" to listOf("com.whatsapp"),
        "chrome" to listOf("com.android.chrome"),
        "क्रोम" to listOf("com.android.chrome"),
        "instagram" to listOf("com.instagram.android"),
        "इंस्टाग्राम" to listOf("com.instagram.android"),
        "इन्स्टाग्राम" to listOf("com.instagram.android"),
        "facebook" to listOf("com.facebook.katana", "com.facebook.lite"),
        "फेसबुक" to listOf("com.facebook.katana"),
        "gmail" to listOf("com.google.android.gm"),
        "जीमेल" to listOf("com.google.android.gm"),
        "maps" to listOf("com.google.android.apps.maps"),
        "मैप्स" to listOf("com.google.android.apps.maps"),
        "गूगल मैप्स" to listOf("com.google.android.apps.maps"),
        "play store" to listOf("com.android.vending"),
        "प्ले स्टोर" to listOf("com.android.vending"),
        "spotify" to listOf("com.spotify.music"),
        "स्पॉटिफ़ाई" to listOf("com.spotify.music"),
        "स्पॉटिफाई" to listOf("com.spotify.music"),
        "telegram" to listOf("org.telegram.messenger"),
        "टेलीग्राम" to listOf("org.telegram.messenger"),
        "twitter" to listOf("com.twitter.android"),
        "ट्विटर" to listOf("com.twitter.android")
    )

    init {
        refreshInstalledApps()
    }

    fun refreshInstalledApps(): List<InstalledApp> {
        val apps = mutableListOf<InstalledApp>()
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)

            for (resolveInfo in resolveInfos) {
                val pkgName = resolveInfo.activityInfo.packageName
                // Skip self
                if (pkgName == context.packageName) continue

                val label = resolveInfo.loadLabel(packageManager).toString()
                val icon = resolveInfo.loadIcon(packageManager)
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                apps.add(
                    InstalledApp(
                        label = label,
                        packageName = pkgName,
                        isSystemApp = isSystem,
                        icon = icon
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("AppManager", "Error querying installed apps: ${e.message}")
        }

        cachedApps = apps.sortedBy { it.label.lowercase() }
        return cachedApps
    }

    fun getInstalledApps(): List<InstalledApp> {
        if (cachedApps.isEmpty()) {
            return refreshInstalledApps()
        }
        return cachedApps
    }

    fun findApp(query: String): InstalledApp? {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) return null

        // 1. Check alias mappings first
        val targetPackages = knownAppAliases[cleanQuery]
        if (targetPackages != null) {
            for (pkg in targetPackages) {
                val matched = cachedApps.firstOrNull { it.packageName == pkg }
                if (matched != null) return matched
                // Or check packageManager directly
                if (isPackageInstalled(pkg)) {
                    val appLabel = getAppLabel(pkg) ?: cleanQuery
                    return InstalledApp(label = appLabel, packageName = pkg)
                }
            }
        }

        // 2. Check exact label match
        val exactMatch = cachedApps.firstOrNull { it.label.trim().equals(cleanQuery, ignoreCase = true) }
        if (exactMatch != null) return exactMatch

        // 3. Check contains / word boundary match
        val containsMatch = cachedApps.firstOrNull {
            it.label.lowercase().contains(cleanQuery) || cleanQuery.contains(it.label.lowercase())
        }
        if (containsMatch != null) return containsMatch

        // 4. Check package name substring
        val pkgMatch = cachedApps.firstOrNull {
            it.packageName.lowercase().contains(cleanQuery)
        }
        return pkgMatch
    }

    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getAppLabel(packageName: String): String? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            null
        }
    }

    fun launchApp(packageName: String): AppLaunchResult {
        val appLabel = getAppLabel(packageName) ?: packageName
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                AppLaunchResult.Success(appName = appLabel, packageName = packageName)
            } else {
                AppLaunchResult.NotFound(requestedName = appLabel)
            }
        } catch (e: Exception) {
            AppLaunchResult.Error(appName = appLabel, message = e.localizedMessage ?: "Failed to launch")
        }
    }

    fun launchAppByName(name: String): AppLaunchResult {
        val app = findApp(name)
        return if (app != null) {
            launchApp(app.packageName)
        } else {
            AppLaunchResult.NotFound(requestedName = name)
        }
    }
}
