package com.example.ai

import com.example.actions.SystemAction
import com.example.apps.AppManager
import com.example.data.AppSettings

data class AssistantResponse(
    val spokenText: String,
    val action: SystemAction = SystemAction.None,
    val isDirectAction: Boolean = false,
    val isError: Boolean = false,
    val isKeyMissing: Boolean = false
)

class AiService(
    private val appManager: AppManager,
    private val commandParser: CommandParser = CommandParser(appManager),
    private val geminiClient: GeminiClient = GeminiClient()
) {

    suspend fun processQuery(
        query: String,
        conversationHistory: List<Pair<String, String>>,
        settings: AppSettings
    ): AssistantResponse {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return AssistantResponse(
                spokenText = "I didn't catch that. Please speak again.",
                isError = true
            )
        }

        // 1. Check for immediate device actions & app launches (zero latency)
        val directResult = commandParser.parse(trimmed)
        if (directResult != null) {
            return AssistantResponse(
                spokenText = directResult.spokenResponse,
                action = directResult.action,
                isDirectAction = directResult.isDirectAction
            )
        }

        // 2. Query Gemini AI brain
        val aiResult = geminiClient.generateReply(
            userPrompt = trimmed,
            conversationHistory = conversationHistory,
            customApiKey = settings.customApiKey,
            customEndpoint = settings.customEndpoint
        )

        return when (aiResult) {
            is AiResult.Success -> {
                AssistantResponse(
                    spokenText = aiResult.text,
                    action = SystemAction.None
                )
            }
            is AiResult.Error -> {
                AssistantResponse(
                    spokenText = aiResult.message,
                    isError = true,
                    isKeyMissing = aiResult.isKeyMissing
                )
            }
        }
    }
}
