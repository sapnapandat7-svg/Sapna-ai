package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Message
import com.example.data.MessageSender
import com.example.data.PendingAction
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyContainer
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.CyberNavySurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageCard(
    message: Message,
    onConfirmAction: () -> Unit = {},
    onDismissAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    when (message.sender) {
        MessageSender.USER -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberNavyContainer),
                    border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .testTag("user_message_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = message.text,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formattedTime,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        MessageSender.ASSISTANT -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                    border = BorderStroke(
                        1.dp,
                        if (message.isError) NeonCoral.copy(alpha = 0.6f) else BorderCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .testTag("assistant_message_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (message.isError) NeonCoral else JarvisCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (message.isError) Icons.Default.ErrorOutline else Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = CyberBlack,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (message.isError) "SAPNA AI ALERT" else "SAPNA AI",
                                    color = if (message.isError) NeonCoral else JarvisCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = formattedTime,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Spoken Text
                        Text(
                            text = message.text,
                            color = if (message.isError) NeonCoral.copy(alpha = 0.9f) else TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 21.sp
                        )

                        // Action Badge
                        if (!message.actionBadge.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyberNavySurfaceVariant,
                                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = NeonMint,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = message.actionBadge,
                                        color = JarvisCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Pending Action Confirmation Card
                        if (message.pendingAction != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            ActionConfirmationCard(
                                pendingAction = message.pendingAction,
                                onConfirm = onConfirmAction,
                                onCancel = onDismissAction
                            )
                        }
                    }
                }
            }
        }

        MessageSender.SYSTEM -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberNavySurfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, BorderCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = message.text,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionConfirmationCard(
    pendingAction: PendingAction,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CyberBlack),
        border = BorderStroke(1.dp, JarvisCyan),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (pendingAction) {
                    is PendingAction.CallPhone -> Icons.Default.Call
                    is PendingAction.SendSms -> Icons.AutoMirrored.Filled.Message
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (pendingAction) {
                        is PendingAction.CallPhone -> "Confirm Phone Call"
                        is PendingAction.SendSms -> "Confirm SMS"
                    },
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (pendingAction) {
                    is PendingAction.CallPhone ->
                        "Target: ${pendingAction.recipientName ?: pendingAction.phoneNumber}\nAre you sure you want to dial this number?"
                    is PendingAction.SendSms ->
                        "Target: ${pendingAction.phoneNumber}\nMessage: \"${pendingAction.messageText}\""
                },
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCoral),
                    border = BorderStroke(1.dp, NeonCoral.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("action_cancel_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = CyberBlack),
                    modifier = Modifier.testTag("action_confirm_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Confirm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
