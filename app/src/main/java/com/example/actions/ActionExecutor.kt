package com.example.actions

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.apps.AppLaunchResult
import com.example.apps.AppManager
import com.example.data.PendingAction

sealed class ExecutionResult {
    data class Success(val message: String, val badge: String) : ExecutionResult()
    data class ConfirmationRequired(val pendingAction: PendingAction, val message: String) : ExecutionResult()
    data class Failure(val error: String) : ExecutionResult()
}

class ActionExecutor(
    private val context: Context,
    private val appManager: AppManager
) {

    fun executeAction(action: SystemAction): ExecutionResult {
        return try {
            when (action) {
                is SystemAction.LaunchApp -> {
                    when (val result = appManager.launchApp(action.packageName)) {
                        is AppLaunchResult.Success -> {
                            ExecutionResult.Success(
                                message = "${result.appName} opened successfully.",
                                badge = "${result.appName} Launched"
                            )
                        }
                        is AppLaunchResult.NotFound -> {
                            ExecutionResult.Failure(
                                error = "Application '${result.requestedName}' is not installed on this device."
                            )
                        }
                        is AppLaunchResult.Error -> {
                            ExecutionResult.Failure(
                                error = "Could not launch ${result.appName}: ${result.message}"
                            )
                        }
                    }
                }

                is SystemAction.OpenSettings -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ExecutionResult.Success(
                        message = "Settings opened.",
                        badge = "Settings"
                    )
                }

                is SystemAction.OpenWifiSettings -> {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ExecutionResult.Success(
                        message = "Wi-Fi settings opened.",
                        badge = "Wi-Fi Settings"
                    )
                }

                is SystemAction.OpenBluetoothSettings -> {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ExecutionResult.Success(
                        message = "Bluetooth settings opened.",
                        badge = "Bluetooth Settings"
                    )
                }

                is SystemAction.OpenCamera -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        ExecutionResult.Success(
                            message = "Camera opened.",
                            badge = "Camera"
                        )
                    } else {
                        // Fallback: search for camera app package
                        val cameraApp = appManager.findApp("camera") ?: appManager.findApp("कैमरा")
                        if (cameraApp != null) {
                            appManager.launchApp(cameraApp.packageName)
                            ExecutionResult.Success(
                                message = "Camera opened.",
                                badge = "Camera"
                            )
                        } else {
                            ExecutionResult.Failure("No camera application found.")
                        }
                    }
                }

                is SystemAction.OpenBrowser -> {
                    var safeUrl = action.url
                    if (!safeUrl.startsWith("http://") && !safeUrl.startsWith("https://")) {
                        safeUrl = "https://$safeUrl"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ExecutionResult.Success(
                        message = "Browser opened.",
                        badge = "Web Browser"
                    )
                }

                is SystemAction.WebSearch -> {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, action.query)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        val searchUrl = "https://www.google.com/search?q=" + Uri.encode(action.query)
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(browserIntent)
                    }
                    ExecutionResult.Success(
                        message = "Searching web for '${action.query}'.",
                        badge = "Web Search"
                    )
                }

                is SystemAction.RequestCallConfirmation -> {
                    ExecutionResult.ConfirmationRequired(
                        pendingAction = PendingAction.CallPhone(
                            phoneNumber = action.phoneNumber,
                            recipientName = action.name
                        ),
                        message = "Would you like me to call ${action.name ?: action.phoneNumber}?"
                    )
                }

                is SystemAction.RequestSmsConfirmation -> {
                    ExecutionResult.ConfirmationRequired(
                        pendingAction = PendingAction.SendSms(
                            phoneNumber = action.phoneNumber,
                            messageText = action.messageText
                        ),
                        message = "Please confirm sending SMS to ${action.phoneNumber}."
                    )
                }

                is SystemAction.None -> {
                    ExecutionResult.Success("", "")
                }
            }
        } catch (e: Exception) {
            ExecutionResult.Failure(e.localizedMessage ?: "Failed to execute action")
        }
    }

    fun executeConfirmedAction(pendingAction: PendingAction): ExecutionResult {
        return try {
            when (pendingAction) {
                is PendingAction.CallPhone -> {
                    val uri = Uri.parse("tel:${pendingAction.phoneNumber.trim()}")
                    val hasCallPermission = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED

                    val intent = if (hasCallPermission) {
                        Intent(Intent.ACTION_CALL, uri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    } else {
                        // Safe fallback without requiring dangerous permission immediately
                        Intent(Intent.ACTION_DIAL, uri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    }
                    context.startActivity(intent)
                    ExecutionResult.Success(
                        message = "Calling ${pendingAction.recipientName ?: pendingAction.phoneNumber}...",
                        badge = "Call Initiated"
                    )
                }

                is PendingAction.SendSms -> {
                    val uri = Uri.parse("smsto:${pendingAction.phoneNumber.trim()}")
                    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                        putExtra("sms_body", pendingAction.messageText)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ExecutionResult.Success(
                        message = "SMS composer opened for ${pendingAction.phoneNumber}.",
                        badge = "SMS Composer"
                    )
                }
            }
        } catch (e: Exception) {
            ExecutionResult.Failure("Could not complete action: ${e.localizedMessage}")
        }
    }
}
