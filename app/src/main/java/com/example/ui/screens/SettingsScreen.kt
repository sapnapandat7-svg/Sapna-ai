package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.AppSettings
import com.example.data.LanguageMode
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberNavySurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    onTestVoice: () -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedLang by remember { mutableStateOf(settings.languageMode) }
    var speechRate by remember { mutableFloatStateOf(settings.speechRate) }
    var speechPitch by remember { mutableFloatStateOf(settings.pitch) }
    var autoSpeak by remember { mutableStateOf(settings.autoSpeak) }
    var customApiKey by remember { mutableStateOf(settings.customApiKey) }
    var customEndpoint by remember { mutableStateOf(settings.customEndpoint) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // TOP APP BAR
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = JarvisCyan
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "JARVIS CONFIGURATION",
                color = JarvisCyan,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // SECTION 1: LANGUAGE SELECTION
            SettingsSectionHeader(title = "VOICE INPUT & LANGUAGE")

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                border = BorderStroke(1.dp, BorderCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Preferred Recognition Language",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Controls Android SpeechRecognizer locale",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LanguageMode.values().forEach { mode ->
                        val isSelected = selectedLang == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CyberNavySurfaceVariant else CyberNavyDark,
                            border = BorderStroke(1.dp, if (isSelected) JarvisCyan else BorderCyan.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedLang = mode
                                    onSaveSettings(
                                        settings.copy(
                                            languageMode = mode,
                                            speechRate = speechRate,
                                            pitch = speechPitch,
                                            autoSpeak = autoSpeak,
                                            customApiKey = customApiKey,
                                            customEndpoint = customEndpoint
                                        )
                                    )
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSelected) JarvisCyan else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: VOICE RESPONSE & TTS
            SettingsSectionHeader(title = "VOICE RESPONSE (TEXT-TO-SPEECH)")

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                border = BorderStroke(1.dp, BorderCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Auto-Speak Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Speak Responses Aloud",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Assistant reads answers aloud using Android TTS",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = {
                                autoSpeak = it
                                onSaveSettings(
                                    settings.copy(
                                        languageMode = selectedLang,
                                        speechRate = speechRate,
                                        pitch = speechPitch,
                                        autoSpeak = it,
                                        customApiKey = customApiKey,
                                        customEndpoint = customEndpoint
                                    )
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberBlack,
                                checkedTrackColor = JarvisCyan,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = CyberNavyDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speech Rate Slider
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Speech Rate", color = TextPrimary, fontSize = 13.sp)
                        Text(String.format("%.1fx", speechRate), color = JarvisCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { speechRate = it },
                        onValueChangeFinished = {
                            onSaveSettings(
                                settings.copy(
                                    languageMode = selectedLang,
                                    speechRate = speechRate,
                                    pitch = speechPitch,
                                    autoSpeak = autoSpeak,
                                    customApiKey = customApiKey,
                                    customEndpoint = customEndpoint
                                )
                            )
                        },
                        valueRange = 0.6f..1.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = CyberNavyDark
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pitch Slider
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Voice Pitch", color = TextPrimary, fontSize = 13.sp)
                        Text(String.format("%.1fx", speechPitch), color = JarvisCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = { speechPitch = it },
                        onValueChangeFinished = {
                            onSaveSettings(
                                settings.copy(
                                    languageMode = selectedLang,
                                    speechRate = speechRate,
                                    pitch = speechPitch,
                                    autoSpeak = autoSpeak,
                                    customApiKey = customApiKey,
                                    customEndpoint = customEndpoint
                                )
                            )
                        },
                        valueRange = 0.6f..1.6f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = CyberNavyDark
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onTestVoice,
                        border = BorderStroke(1.dp, JarvisCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_voice_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Voice Synthesis", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 3: AI BRAIN CONFIGURATION
            SettingsSectionHeader(title = "AI BRAIN (GEMINI API / PROXY)")

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                border = BorderStroke(1.dp, BorderCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // System Key Status
                    val hasInjectedKey = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberNavyDark, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (hasInjectedKey || customApiKey.isNotBlank()) NeonMint else NeonCoral)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (hasInjectedKey) "Injected API Key Active (Gemini 3.5 Flash)"
                                else if (customApiKey.isNotBlank()) "Custom API Key Active"
                                else "No API Key Configured",
                                color = if (hasInjectedKey || customApiKey.isNotBlank()) NeonMint else NeonCoral,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasInjectedKey) "Injected securely from Secrets panel"
                                else "Add GEMINI_API_KEY in Secrets or enter below",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Custom Gemini API Key Override",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = {
                            customApiKey = it
                            onSaveSettings(
                                settings.copy(
                                    languageMode = selectedLang,
                                    speechRate = speechRate,
                                    pitch = speechPitch,
                                    autoSpeak = autoSpeak,
                                    customApiKey = it,
                                    customEndpoint = customEndpoint
                                )
                            )
                        },
                        placeholder = { Text("AIzaSy...", color = TextTertiary, fontSize = 12.sp) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = BorderCyan,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CyberNavyDark,
                            unfocusedContainerColor = CyberNavyDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_api_key_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Custom Proxy / Backend Endpoint (Optional)",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = customEndpoint,
                        onValueChange = {
                            customEndpoint = it
                            onSaveSettings(
                                settings.copy(
                                    languageMode = selectedLang,
                                    speechRate = speechRate,
                                    pitch = speechPitch,
                                    autoSpeak = autoSpeak,
                                    customApiKey = customApiKey,
                                    customEndpoint = it
                                )
                            )
                        },
                        placeholder = { Text("https://my-backend-proxy.run.app/api", color = TextTertiary, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = BorderCyan,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CyberNavyDark,
                            unfocusedContainerColor = CyberNavyDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_endpoint_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Production security: API keys are never hardcoded.",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 4: MAINTENANCE
            SettingsSectionHeader(title = "DATA & RESET")

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                border = BorderStroke(1.dp, BorderCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Button(
                        onClick = onClearHistory,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurfaceVariant, contentColor = NeonCoral),
                        border = BorderStroke(1.dp, NeonCoral.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_history_button")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Conversation History", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = ElectricBlue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
    )
}
