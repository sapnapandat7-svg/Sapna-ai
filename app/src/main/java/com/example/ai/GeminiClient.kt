package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 250
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

sealed class AiResult {
    data class Success(val text: String) : AiResult()
    data class Error(val message: String, val isKeyMissing: Boolean = false) : AiResult()
}

class GeminiClient {

    companion object {
        private const val TAG = "GeminiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    suspend fun generateReply(
        userPrompt: String,
        conversationHistory: List<Pair<String, String>>, // role to text
        customApiKey: String? = null,
        customEndpoint: String? = null
    ): AiResult = withContext(Dispatchers.IO) {
        // Determine active API key
        val effectiveKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        // If custom proxy endpoint is configured, call custom proxy directly
        if (!customEndpoint.isNullOrBlank()) {
            return@withContext callCustomProxy(customEndpoint.trim(), userPrompt, effectiveKey)
        }

        if (effectiveKey.isEmpty()) {
            return@withContext AiResult.Error(
                message = "API key not configured. Please add GEMINI_API_KEY in the Secrets panel or enter your key in Sapna AI Settings.",
                isKeyMissing = true
            )
        }

        try {
            val systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(
                        text = "You are Sapna AI, a super-intelligent, concise, JARVIS-style bilingual voice assistant (Hindi and English). " +
                                "Speak directly, politely, and warmly. Match the language used by the user (Hindi, English, or conversational Hinglish). " +
                                "CRITICAL: Keep your response very concise (1 to 2 short sentences, maximum 40 words), because your answer is synthesized into spoken audio through Text-To-Speech. " +
                                "Do NOT use markdown symbols, asterisks, bullet points, or emoji, as they sound awkward when spoken aloud."
                    )
                )
            )

            val contents = mutableListOf<GeminiContent>()
            // Include recent conversation context (last 4 turns)
            val recentTurns = conversationHistory.takeLast(4)
            for ((role, text) in recentTurns) {
                contents.add(
                    GeminiContent(
                        role = if (role == "user") "user" else "model",
                        parts = listOf(GeminiPart(text = text))
                    )
                )
            }
            // Add current query
            contents.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userPrompt))
                )
            )

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 200)
            )

            val response = api.generateContent(apiKey = effectiveKey, request = request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()

            if (!text.isNullOrEmpty()) {
                AiResult.Success(cleanForSpeech(text))
            } else {
                AiResult.Error("Sapna AI could not generate a response. Please try again.")
            }
        } catch (e: retrofit2.HttpException) {
            val code = e.code()
            val errorMsg = when (code) {
                400 -> "Invalid request or API key."
                403 -> "API key unauthorized or quota exhausted. Please check your Gemini API key in Settings."
                429 -> "Rate limit reached. Please wait a moment."
                500, 503 -> "AI service temporarily unavailable. Please retry shortly."
                else -> "AI service error (HTTP $code)."
            }
            Log.e(TAG, "HttpException $code: ${e.message()}")
            AiResult.Error(errorMsg, isKeyMissing = code == 403 || code == 400)
        } catch (e: java.net.UnknownHostException) {
            AiResult.Error("No internet connection detected. Please check your network.")
        } catch (e: java.net.SocketTimeoutException) {
            AiResult.Error("Request timed out. The network connection might be slow.")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating AI response: ${e.message}")
            AiResult.Error("AI connection error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun callCustomProxy(endpointUrl: String, prompt: String, apiKey: String): AiResult {
        return try {
            val jsonAdapter = moshi.adapter(Map::class.java)
            val payload = mapOf(
                "prompt" to prompt,
                "model" to "gemini-3.5-flash"
            )
            val body = jsonAdapter.toJson(payload).toRequestBody("application/json".toMediaType())

            val reqBuilder = Request.Builder()
                .url(endpointUrl)
                .post(body)

            if (apiKey.isNotEmpty()) {
                reqBuilder.addHeader("Authorization", "Bearer $apiKey")
            }

            val response = okHttpClient.newCall(reqBuilder.build()).execute()
            if (response.isSuccessful) {
                val respBody = response.body?.string() ?: ""
                AiResult.Success(cleanForSpeech(respBody))
            } else {
                AiResult.Error("Proxy returned error HTTP ${response.code}")
            }
        } catch (e: Exception) {
            AiResult.Error("Proxy error: ${e.localizedMessage}")
        }
    }

    private fun cleanForSpeech(text: String): String {
        return text
            .replace("**", "")
            .replace("*", "")
            .replace("`", "")
            .replace("#", "")
            .replace(Regex("[\r\n]+"), " ")
            .trim()
    }
}
