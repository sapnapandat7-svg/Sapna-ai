package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.AssistantState
import com.example.ui.UiState
import com.example.ui.components.JarvisCoreVisualizer
import com.example.ui.components.MessageCard
import com.example.ui.components.MicrophoneOrbButton
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberNavySurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun HomeScreen(
    uiState: UiState,
    onMicClick: () -> Unit,
    onStopSpeakingClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onConfirmAction: () -> Unit,
    onDismissAction: () -> Unit,
    onNavigate: (AppScreen) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll on new messages
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    val quickCommands = listOf(
        "YouTube खोलो",
        "Camera खोलो",
        "Wi-Fi Settings",
        "Bluetooth Settings",
        "Settings खोलो",
        "WhatsApp खोलो",
        "Who created you?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // TOP BAR
        TopJarvisBar(
            assistantState = uiState.assistantState,
            onNavigate = onNavigate
        )

        // MAIN CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Core Visualizer & Status banner (compact)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    JarvisCoreVisualizer(
                        assistantState = uiState.assistantState,
                        soundLevel = uiState.soundLevel,
                        size = 120.dp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val statusText = when (uiState.assistantState) {
                        AssistantState.LISTENING -> {
                            if (uiState.recognizedPartialText.isNotEmpty()) {
                                "\"${uiState.recognizedPartialText}\""
                            } else {
                                "Listening in Hindi & English..."
                            }
                        }
                        AssistantState.THINKING -> "Analyzing command with AI..."
                        AssistantState.SPEAKING -> "Speaking response aloud..."
                        AssistantState.ERROR -> uiState.speechError ?: "Error encountered"
                        AssistantState.IDLE -> "Sapna AI Core Online"
                    }

                    Text(
                        text = statusText,
                        color = when (uiState.assistantState) {
                            AssistantState.LISTENING -> NeonMint
                            AssistantState.THINKING -> ElectricBlue
                            AssistantState.SPEAKING -> JarvisCyanBright
                            AssistantState.ERROR -> NeonCoral
                            AssistantState.IDLE -> TextSecondary
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick suggestion chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickCommands) { command ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CyberNavySurfaceVariant,
                        border = BorderStroke(1.dp, BorderCyan),
                        modifier = Modifier
                            .clickable { onSendMessage(command) }
                            .testTag("quick_command_chip")
                    ) {
                        Text(
                            text = command,
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Conversation history list
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(uiState.messages) { message ->
                    MessageCard(
                        message = message,
                        onConfirmAction = onConfirmAction,
                        onDismissAction = onDismissAction
                    )
                }
            }
        }

        // BOTTOM CONTROLS
        Card(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberNavyDark),
            border = BorderStroke(1.dp, BorderCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Typed text input bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text("Ask Sapna AI or type command...", color = TextTertiary, fontSize = 13.sp)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                textInput = ""
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = BorderCyan,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = JarvisCyan,
                            focusedContainerColor = CyberNavySurface,
                            unfocusedContainerColor = CyberNavySurface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("command_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (textInput.isNotBlank()) JarvisCyan else CyberNavySurfaceVariant)
                            .testTag("send_command_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank()) CyberBlack else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Microphone Orb with Stop Speaking button when speaking
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.assistantState == AssistantState.SPEAKING) {
                        IconButton(
                            onClick = onStopSpeakingClick,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyberNavySurfaceVariant)
                                .testTag("stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Mute Voice",
                                tint = NeonCoral,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    MicrophoneOrbButton(
                        assistantState = uiState.assistantState,
                        soundLevel = uiState.soundLevel,
                        onClick = onMicClick
                    )
                }
            }
        }
    }
}

@Composable
fun TopJarvisBar(
    assistantState: AssistantState,
    onNavigate: (AppScreen) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        when (assistantState) {
                            AssistantState.LISTENING -> NeonMint
                            AssistantState.THINKING -> ElectricBlue
                            AssistantState.SPEAKING -> JarvisCyanBright
                            AssistantState.ERROR -> NeonCoral
                            AssistantState.IDLE -> JarvisCyan
                        }
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "SAPNA AI",
                    color = JarvisCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "JARVIS VOICE ASSISTANT",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onNavigate(AppScreen.APPS_DRAWER) },
                modifier = Modifier.testTag("nav_apps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "Installed Apps",
                    tint = JarvisCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = { onNavigate(AppScreen.PERMISSIONS) },
                modifier = Modifier.testTag("nav_permissions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Permissions",
                    tint = JarvisCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = { onNavigate(AppScreen.SETTINGS) },
                modifier = Modifier.testTag("nav_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = JarvisCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
