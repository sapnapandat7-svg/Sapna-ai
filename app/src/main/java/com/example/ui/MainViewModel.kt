package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.actions.ActionExecutor
import com.example.actions.ExecutionResult
import com.example.actions.SystemAction
import com.example.ai.AiService
import com.example.apps.AppManager
import com.example.apps.InstalledApp
import com.example.data.AppSettings
import com.example.data.Message
import com.example.data.MessageSender
import com.example.data.PendingAction
import com.example.data.PreferencesManager
import com.example.permissions.PermissionHelper
import com.example.voice.SpeechRecognitionManager
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class AppScreen {
    HOME,
    SETTINGS,
    APPS_DRAWER,
    PERMISSIONS
}

data class UiState(
    val assistantState: AssistantState = AssistantState.IDLE,
    val currentScreen: AppScreen = AppScreen.HOME,
    val soundLevel: Float = 0f,
    val recognizedPartialText: String = "",
    val messages: List<Message> = emptyList(),
    val pendingConfirmation: PendingAction? = null,
    val pendingConfirmationPrompt: String? = null,
    val settings: AppSettings = AppSettings(),
    val installedApps: List<InstalledApp> = emptyList(),
    val speechError: String? = null,
    val isTtsReady: Boolean = false,
    val requestPermissionEvent: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefsManager = PreferencesManager(context)
    val appManager = AppManager(context)
    private val actionExecutor = ActionExecutor(context, appManager)
    private val aiService = AiService(appManager)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val textToSpeechManager: TextToSpeechManager
    private val speechRecognitionManager: SpeechRecognitionManager

    init {
        val initialSettings = prefsManager.loadSettings()
        _uiState.update {
            it.copy(
                settings = initialSettings,
                installedApps = appManager.getInstalledApps(),
                messages = listOf(
                    Message(
                        sender = MessageSender.ASSISTANT,
                        text = "नमस्ते! I am Sapna AI, your personal voice assistant. Speak or type a command to get started."
                    )
                )
            )
        }

        textToSpeechManager = TextToSpeechManager(context) { ready ->
            _uiState.update { it.copy(isTtsReady = ready) }
        }.apply {
            onSpeakingStarted = {
                _uiState.update { it.copy(assistantState = AssistantState.SPEAKING) }
            }
            onSpeakingFinished = {
                _uiState.update {
                    if (it.assistantState == AssistantState.SPEAKING) {
                        it.copy(assistantState = AssistantState.IDLE)
                    } else it
                }
            }
            onSpeakingError = { errorMsg ->
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.IDLE,
                        speechError = errorMsg
                    )
                }
            }
        }

        speechRecognitionManager = SpeechRecognitionManager(
            context = context,
            onReady = {
                _uiState.update { it.copy(assistantState = AssistantState.LISTENING) }
            },
            onListeningChanged = { listening ->
                if (!listening && _uiState.value.assistantState == AssistantState.LISTENING) {
                    _uiState.update { it.copy(assistantState = AssistantState.IDLE, soundLevel = 0f) }
                }
            },
            onRmsChanged = { rms ->
                _uiState.update { it.copy(soundLevel = rms) }
            },
            onPartialResult = { partial ->
                _uiState.update { it.copy(recognizedPartialText = partial) }
            },
            onFinalResult = { text ->
                _uiState.update { it.copy(recognizedPartialText = "") }
                processUserInput(text)
            },
            onError = { errMsg ->
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.ERROR,
                        speechError = errMsg,
                        soundLevel = 0f
                    )
                }
            }
        )
    }

    fun onMicClicked() {
        if (!PermissionHelper.isAudioPermissionGranted(context)) {
            _uiState.update { it.copy(requestPermissionEvent = PermissionHelper.AUDIO_PERMISSION) }
            return
        }

        when (_uiState.value.assistantState) {
            AssistantState.LISTENING -> {
                speechRecognitionManager.stopListening()
            }
            AssistantState.SPEAKING -> {
                textToSpeechManager.stop()
                _uiState.update { it.copy(assistantState = AssistantState.IDLE) }
            }
            else -> {
                textToSpeechManager.stop()
                _uiState.update {
                    it.copy(
                        assistantState = AssistantState.LISTENING,
                        speechError = null,
                        recognizedPartialText = ""
                    )
                }
                speechRecognitionManager.startListening(_uiState.value.settings.languageMode.speechTag)
            }
        }
    }

    fun onStopSpeakingClicked() {
        textToSpeechManager.stop()
        _uiState.update { it.copy(assistantState = AssistantState.IDLE) }
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        _uiState.update { it.copy(requestPermissionEvent = null) }
        if (permission == PermissionHelper.AUDIO_PERMISSION && isGranted) {
            onMicClicked()
        }
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        processUserInput(text)
    }

    private fun processUserInput(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        textToSpeechManager.stop()

        val userMessage = Message(
            sender = MessageSender.USER,
            text = trimmed
        )
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                assistantState = AssistantState.THINKING,
                speechError = null
            )
        }

        viewModelScope.launch {
            // Build recent context
            val history = _uiState.value.messages.takeLast(6).map {
                (if (it.sender == MessageSender.USER) "user" else "assistant") to it.text
            }

            val response = aiService.processQuery(
                query = trimmed,
                conversationHistory = history,
                settings = _uiState.value.settings
            )

            // Handle actions
            var actionBadge: String? = null
            var pendingAction: PendingAction? = null
            var pendingPrompt: String? = null

            if (response.action !is SystemAction.None) {
                when (val result = actionExecutor.executeAction(response.action)) {
                    is ExecutionResult.Success -> {
                        actionBadge = result.badge.ifEmpty { null }
                    }
                    is ExecutionResult.ConfirmationRequired -> {
                        pendingAction = result.pendingAction
                        pendingPrompt = result.message
                    }
                    is ExecutionResult.Failure -> {
                        actionBadge = "Action Failed"
                    }
                }
            }

            val assistantMessage = Message(
                sender = MessageSender.ASSISTANT,
                text = response.spokenText,
                actionBadge = actionBadge,
                pendingAction = pendingAction,
                isError = response.isError
            )

            _uiState.update {
                it.copy(
                    messages = it.messages + assistantMessage,
                    pendingConfirmation = pendingAction,
                    pendingConfirmationPrompt = pendingPrompt,
                    assistantState = if (_uiState.value.settings.autoSpeak && response.spokenText.isNotBlank())
                        AssistantState.SPEAKING else AssistantState.IDLE
                )
            }

            if (_uiState.value.settings.autoSpeak && response.spokenText.isNotBlank()) {
                textToSpeechManager.speak(
                    text = response.spokenText,
                    speechRate = _uiState.value.settings.speechRate,
                    pitch = _uiState.value.settings.pitch
                )
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _uiState.value.pendingConfirmation ?: return
        viewModelScope.launch {
            val result = actionExecutor.executeConfirmedAction(pending)
            val badge = when (result) {
                is ExecutionResult.Success -> result.badge
                is ExecutionResult.Failure -> "Action Failed"
                else -> null
            }
            val confirmMessage = Message(
                sender = MessageSender.SYSTEM,
                text = when (result) {
                    is ExecutionResult.Success -> result.message
                    is ExecutionResult.Failure -> result.error
                    else -> ""
                },
                actionBadge = badge,
                actionExecuted = true
            )

            _uiState.update {
                it.copy(
                    messages = it.messages + confirmMessage,
                    pendingConfirmation = null,
                    pendingConfirmationPrompt = null,
                    assistantState = AssistantState.IDLE
                )
            }
        }
    }

    fun dismissPendingAction() {
        _uiState.update {
            it.copy(
                pendingConfirmation = null,
                pendingConfirmationPrompt = null
            )
        }
    }

    fun launchInstalledApp(app: InstalledApp) {
        val result = appManager.launchApp(app.packageName)
        val msg = when (result) {
            is com.example.apps.AppLaunchResult.Success -> "Launched ${result.appName}"
            is com.example.apps.AppLaunchResult.NotFound -> "App not found"
            is com.example.apps.AppLaunchResult.Error -> "Failed: ${result.message}"
        }
        _uiState.update {
            it.copy(
                messages = it.messages + Message(
                    sender = MessageSender.SYSTEM,
                    text = msg,
                    actionBadge = app.label,
                    actionExecuted = true
                ),
                currentScreen = AppScreen.HOME
            )
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun updateSettings(newSettings: AppSettings) {
        prefsManager.saveSettings(newSettings)
        _uiState.update { it.copy(settings = newSettings) }
    }

    fun refreshApps() {
        val apps = appManager.refreshInstalledApps()
        _uiState.update { it.copy(installedApps = apps) }
    }

    fun clearHistory() {
        _uiState.update {
            it.copy(
                messages = listOf(
                    Message(
                        sender = MessageSender.ASSISTANT,
                        text = "History cleared. How can I assist you now?"
                    )
                )
            )
        }
    }

    fun testVoice() {
        textToSpeechManager.speak(
            text = "Sapna AI voice synthesis test complete.",
            speechRate = _uiState.value.settings.speechRate,
            pitch = _uiState.value.settings.pitch
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.destroy()
        textToSpeechManager.shutdown()
    }
}
