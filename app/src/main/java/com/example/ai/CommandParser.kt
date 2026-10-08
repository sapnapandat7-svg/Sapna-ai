package com.example.ai

import com.example.actions.SystemAction
import com.example.apps.AppManager

data class ParsedCommandResult(
    val action: SystemAction,
    val spokenResponse: String,
    val isDirectAction: Boolean
)

class CommandParser(private val appManager: AppManager) {

    fun parse(input: String): ParsedCommandResult? {
        val text = input.trim()
        val lower = text.lowercase()

        // 1. Settings Actions
        if (matchesPattern(lower, listOf("open settings", "settings kholo", "setting kholo", "सेटिंग्स खोलो", "सेटिंग खोलो", "open setting"))) {
            val isHindi = lower.contains("खोलो") || lower.contains("kholo")
            return ParsedCommandResult(
                action = SystemAction.OpenSettings,
                spokenResponse = if (isHindi) "सेटिंग्स खोल रही हूँ।" else "Opening system settings.",
                isDirectAction = true
            )
        }

        if (matchesPattern(lower, listOf("open wifi", "open wi-fi", "wifi settings", "wifi kholo", "वाईफाई खोलो", "वाई-फाई खोलो", "open wifi settings"))) {
            val isHindi = lower.contains("खोलो") || lower.contains("kholo")
            return ParsedCommandResult(
                action = SystemAction.OpenWifiSettings,
                spokenResponse = if (isHindi) "वाई-फाई सेटिंग्स खोल रही हूँ।" else "Opening Wi-Fi settings.",
                isDirectAction = true
            )
        }

        if (matchesPattern(lower, listOf("open bluetooth", "bluetooth settings", "bluetooth kholo", "ब्लूटूथ खोलो", "open bluetooth settings"))) {
            val isHindi = lower.contains("खोलो") || lower.contains("kholo")
            return ParsedCommandResult(
                action = SystemAction.OpenBluetoothSettings,
                spokenResponse = if (isHindi) "ब्लूटूथ सेटिंग्स खोल रही हूँ।" else "Opening Bluetooth settings.",
                isDirectAction = true
            )
        }

        // 2. Camera Action
        if (matchesPattern(lower, listOf("open camera", "camera kholo", "take photo", "camera open karo", "कैमरा खोलो", "फोटो खींचो"))) {
            val isHindi = lower.contains("खोलो") || lower.contains("kholo") || lower.contains("कैमरा")
            return ParsedCommandResult(
                action = SystemAction.OpenCamera,
                spokenResponse = if (isHindi) "कैमरा खोल रही हूँ।" else "Opening Camera.",
                isDirectAction = true
            )
        }

        // 3. Web Browser
        if (matchesPattern(lower, listOf("open browser", "open chrome", "chrome kholo", "क्रोम खोलो", "ब्राउज़र खोलो", "chrome open karo"))) {
            val isHindi = lower.contains("खोलो") || lower.contains("kholo") || lower.contains("क्रोम")
            val chromeApp = appManager.findApp("chrome") ?: appManager.findApp("क्रोम")
            val action = if (chromeApp != null) {
                SystemAction.LaunchApp(chromeApp.packageName, chromeApp.label)
            } else {
                SystemAction.OpenBrowser("https://www.google.com")
            }
            return ParsedCommandResult(
                action = action,
                spokenResponse = if (isHindi) "क्रोम ब्राउज़र खोल रही हूँ।" else "Opening Chrome browser.",
                isDirectAction = true
            )
        }

        // 4. Phone Call detection: "call 9876543210" or "9876543210 ko call karo"
        val callRegex = Regex("""(?:call|कॉल करें|कॉल करो|फ़ोन लगाओ)\s+([0-9+\s\-]{6,15})""", RegexOption.IGNORE_CASE)
        val callMatch = callRegex.find(text)
        if (callMatch != null) {
            val number = callMatch.groupValues[1].replace("\\s".toRegex(), "")
            return ParsedCommandResult(
                action = SystemAction.RequestCallConfirmation(number),
                spokenResponse = "Do you want me to call $number?",
                isDirectAction = true
            )
        }

        val hindiCallRegex = Regex("""([0-9+\s\-]{6,15})\s+(?:को कॉल करो|को फोन करो|ko call karo)""", RegexOption.IGNORE_CASE)
        val hindiCallMatch = hindiCallRegex.find(text)
        if (hindiCallMatch != null) {
            val number = hindiCallMatch.groupValues[1].replace("\\s".toRegex(), "")
            return ParsedCommandResult(
                action = SystemAction.RequestCallConfirmation(number),
                spokenResponse = "क्या आप $number पर कॉल करना चाहते हैं?",
                isDirectAction = true
            )
        }

        // 5. SMS detection: "send sms to 9876543210 hello"
        val smsRegex = Regex("""(?:send sms to|message to|मैसेज भेजो)\s+([0-9+\s\-]{6,15})\s*(.*)""", RegexOption.IGNORE_CASE)
        val smsMatch = smsRegex.find(text)
        if (smsMatch != null) {
            val number = smsMatch.groupValues[1].replace("\\s".toRegex(), "")
            val message = smsMatch.groupValues.getOrNull(2) ?: ""
            return ParsedCommandResult(
                action = SystemAction.RequestSmsConfirmation(number, message),
                spokenResponse = "Please confirm sending SMS to $number.",
                isDirectAction = true
            )
        }

        // 6. Generic App Launch Pattern
        // Patterns:
        // "open [App]"
        // "launch [App]"
        // "[App] खोलो"
        // "[App] open karo"
        // "[App] chalao"
        val appName = extractAppCandidate(text)
        if (!appName.isNullOrBlank()) {
            val foundApp = appManager.findApp(appName)
            val isHindi = text.contains("खोलो") || text.contains("kholo") || text.contains("चलाओ")
            return if (foundApp != null) {
                ParsedCommandResult(
                    action = SystemAction.LaunchApp(foundApp.packageName, foundApp.label),
                    spokenResponse = if (isHindi) "${foundApp.label} खोल रही हूँ।" else "Opening ${foundApp.label}.",
                    isDirectAction = true
                )
            } else {
                ParsedCommandResult(
                    action = SystemAction.None,
                    spokenResponse = if (isHindi) "माफ़ कीजिए, $appName ऐप आपके फ़ोन में इन्स्टॉल नहीं है।" else "I couldn't find $appName installed on your device.",
                    isDirectAction = true
                )
            }
        }

        // 7. Web search: "search for [query]", "[query] search karo"
        if (lower.startsWith("search ") || lower.startsWith("google ")) {
            val query = text.substringAfter(" ").trim()
            if (query.isNotEmpty()) {
                return ParsedCommandResult(
                    action = SystemAction.WebSearch(query),
                    spokenResponse = "Searching for $query.",
                    isDirectAction = true
                )
            }
        }

        return null
    }

    private fun matchesPattern(text: String, patterns: List<String>): Boolean {
        return patterns.any { text == it || text.startsWith("$it ") || text.endsWith(" $it") }
    }

    private fun extractAppCandidate(text: String): String? {
        val lower = text.lowercase().trim()

        // "open youtube" -> "youtube"
        if (lower.startsWith("open ") && lower.length > 5) {
            return text.substring(5).trim()
        }
        // "launch youtube" -> "youtube"
        if (lower.startsWith("launch ") && lower.length > 7) {
            return text.substring(7).trim()
        }
        // "start youtube" -> "youtube"
        if (lower.startsWith("start ") && lower.length > 6) {
            return text.substring(6).trim()
        }

        // Hindi: "यूट्यूब खोलो" / "youtube kholo"
        if (lower.endsWith(" खोलो") && text.length > 5) {
            return text.substring(0, text.length - 5).trim()
        }
        if (lower.endsWith(" kholo") && lower.length > 6) {
            return text.substring(0, text.length - 6).trim()
        }
        if (lower.endsWith(" open karo") && lower.length > 10) {
            return text.substring(0, text.length - 10).trim()
        }
        if (lower.endsWith(" चलाओ") && text.length > 5) {
            return text.substring(0, text.length - 5).trim()
        }

        return null
    }
}
