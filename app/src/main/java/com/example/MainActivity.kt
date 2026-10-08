package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.permissions.PermissionHelper
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberBlack
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    var pendingPermissionToRequest by remember { mutableStateOf<String?>(null) }
                    var showRationaleDialog by remember { mutableStateOf(false) }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        pendingPermissionToRequest?.let { perm ->
                            viewModel.onPermissionResult(perm, isGranted)
                        }
                        pendingPermissionToRequest = null
                        showRationaleDialog = false
                    }

                    // Handle permission request events from ViewModel
                    if (uiState.requestPermissionEvent != null && !showRationaleDialog) {
                        pendingPermissionToRequest = uiState.requestPermissionEvent
                        showRationaleDialog = true
                    }

                    // Permission Rationale Dialog
                    if (showRationaleDialog && pendingPermissionToRequest != null) {
                        val perm = pendingPermissionToRequest!!
                        AlertDialog(
                            onDismissRequest = {
                                showRationaleDialog = false
                                viewModel.onPermissionResult(perm, false)
                            },
                            title = {
                                Text(
                                    text = if (perm == PermissionHelper.AUDIO_PERMISSION) "Microphone Access Required" else "Permission Required",
                                    color = JarvisCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            },
                            text = {
                                Text(
                                    text = PermissionHelper.getRationale(perm),
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showRationaleDialog = false
                                        permissionLauncher.launch(perm)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = CyberBlack)
                                ) {
                                    Text("Allow Access", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = {
                                        showRationaleDialog = false
                                        viewModel.onPermissionResult(perm, false)
                                    },
                                    border = BorderStroke(1.dp, BorderCyan),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCoral)
                                ) {
                                    Text("Not Now")
                                }
                            },
                            containerColor = CyberNavySurface,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    // Screen routing
                    when (uiState.currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                onMicClick = { viewModel.onMicClicked() },
                                onStopSpeakingClick = { viewModel.onStopSpeakingClicked() },
                                onSendMessage = { viewModel.sendTextMessage(it) },
                                onConfirmAction = { viewModel.confirmPendingAction() },
                                onDismissAction = { viewModel.dismissPendingAction() },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                settings = uiState.settings,
                                onSaveSettings = { viewModel.updateSettings(it) },
                                onTestVoice = { viewModel.testVoice() },
                                onClearHistory = { viewModel.clearHistory() },
                                onBack = { viewModel.navigateTo(AppScreen.HOME) }
                            )
                        }

                        AppScreen.APPS_DRAWER -> {
                            AppDrawerScreen(
                                installedApps = uiState.installedApps,
                                onLaunchApp = { viewModel.launchInstalledApp(it) },
                                onRefreshApps = { viewModel.refreshApps() },
                                onBack = { viewModel.navigateTo(AppScreen.HOME) }
                            )
                        }

                        AppScreen.PERMISSIONS -> {
                            PermissionsScreen(
                                onRequestPermission = { perm ->
                                    pendingPermissionToRequest = perm
                                    permissionLauncher.launch(perm)
                                },
                                onBack = { viewModel.navigateTo(AppScreen.HOME) }
                            )
                        }
                    }
                }
            }
        }
    }
}
