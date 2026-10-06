package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Base64
import android.util.Log
import com.example.data.api.RetrofitClient
import com.example.data.model.Content
import com.example.data.model.GenerateContentRequest
import com.example.data.model.GenerationConfig
import com.example.data.model.Part
import com.example.data.model.PrebuiltVoiceConfig
import com.example.data.model.SpeechConfig
import com.example.data.model.VoiceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

class TtsHelper(private val context: Context) {

    private var androidTts: TextToSpeech? = null
    private var isTtsInitialized = false

    private fun ensureAndroidTts(onReady: (Boolean) -> Unit) {
        if (androidTts != null && isTtsInitialized) {
            onReady(true)
            return
        }
        try {
            androidTts = TextToSpeech(context.applicationContext) { status ->
                isTtsInitialized = (status == TextToSpeech.SUCCESS)
                if (isTtsInitialized) {
                    try {
                        androidTts?.language = Locale.US
                    } catch (e: Exception) {
                        Log.w("TtsHelper", "Could not set TTS default language: ${e.message}")
                    }
                }
                onReady(isTtsInitialized)
            }
        } catch (e: Exception) {
            Log.w("TtsHelper", "Failed to initialize Android TextToSpeech: ${e.message}")
            onReady(false)
        }
    }

    /**
     * Synthesizes audio to a file.
     * First attempts Gemini TTS (gemini-3.8-flash-tts).
     * If that fails or API key is unavailable, gracefully falls back to Android's built-in TextToSpeech engine.
     */
    suspend fun synthesizeSpeechToFile(
        text: String,
        voiceStyle: String,
        language: String,
        apiKey: String,
        targetFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        if (text.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Text cannot be empty"))
        }

        // 1. Try Gemini TTS if API key is provided
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiVoice = when (voiceStyle.lowercase()) {
                    "female" -> "Kore"
                    "kid" -> "Puck"
                    else -> "Fenrir" // Male
                }

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = "Please narrate the following clearly in $language: $text")))
                    ),
                    generationConfig = GenerationConfig(
                        responseModalities = listOf("AUDIO"),
                        speechConfig = SpeechConfig(
                            voiceConfig = VoiceConfig(
                                prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = geminiVoice)
                            )
                        )
                    )
                )

                // Call gemini-3.8-flash-tts
                val response = try {
                    RetrofitClient.geminiApi.generateContent(
                        model = "gemini-3.8-flash-tts",
                        apiKey = apiKey,
                        request = request
                    )
                } catch (e: Exception) {
                    Log.w("TtsHelper", "gemini-3.8-flash-tts fallback to gemini-2.5-flash-preview-tts", e)
                    RetrofitClient.geminiApi.generateContent(
                        model = "gemini-2.5-flash-preview-tts",
                        apiKey = apiKey,
                        request = request
                    )
                }

                val inlineData = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull { it.inlineData != null }
                    ?.inlineData

                if (inlineData != null && inlineData.data.isNotBlank()) {
                    val audioBytes = Base64.decode(inlineData.data, Base64.DEFAULT)
                    FileOutputStream(targetFile).use { it.write(audioBytes) }
                    Log.d("TtsHelper", "Successfully generated Gemini TTS audio: ${targetFile.length()} bytes")
                    return@withContext Result.success(targetFile)
                }
            } catch (e: Exception) {
                Log.w("TtsHelper", "Gemini TTS failed, falling back to Android system TTS: ${e.message}")
            }
        }

        // 2. Fallback to Android System TextToSpeech engine
        synthesizeWithAndroidTts(text, voiceStyle, language, targetFile)
    }

    private suspend fun synthesizeWithAndroidTts(
        text: String,
        voiceStyle: String,
        language: String,
        targetFile: File
    ): Result<File> = suspendCancellableCoroutine { continuation ->
        ensureAndroidTts { ready ->
            val tts = androidTts
            if (!ready || tts == null) {
                continuation.resume(Result.failure(IllegalStateException("TTS not available")))
                return@ensureAndroidTts
            }

            try {
                val locale = when (language.lowercase()) {
                    "hindi" -> Locale("hi", "IN")
                    "spanish" -> Locale("es", "ES")
                    "french" -> Locale.FRENCH
                    "german" -> Locale.GERMAN
                    "japanese" -> Locale.JAPANESE
                    else -> Locale.US
                }

                tts.language = locale

                when (voiceStyle.lowercase()) {
                    "female" -> {
                        tts.setPitch(1.2f)
                        tts.setSpeechRate(1.0f)
                    }
                    "kid" -> {
                        tts.setPitch(1.5f)
                        tts.setSpeechRate(1.1f)
                    }
                    else -> { // Male
                        tts.setPitch(0.85f)
                        tts.setSpeechRate(0.95f)
                    }
                }

                val utteranceId = UUID.randomUUID().toString()
                val params = Bundle()

                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onDone(id: String?) {
                        if (id == utteranceId) {
                            continuation.resume(Result.success(targetFile))
                        }
                    }
                    override fun onError(id: String?) {
                        if (id == utteranceId) {
                            continuation.resume(Result.failure(RuntimeException("System TTS synthesis failed")))
                        }
                    }
                })

                val result = tts.synthesizeToFile(text, params, targetFile, utteranceId)
                if (result != TextToSpeech.SUCCESS) {
                    continuation.resume(Result.failure(RuntimeException("Failed to schedule TTS file synthesis")))
                }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    fun shutdown() {
        androidTts?.stop()
        androidTts?.shutdown()
        androidTts = null
    }
}
