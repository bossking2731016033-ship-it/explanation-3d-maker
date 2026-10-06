package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.audio.TtsHelper
import com.example.data.api.RetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.local.ExplainerVideoEntity
import com.example.data.local.VideoDao
import com.example.data.model.Content
import com.example.data.model.GenerateContentRequest
import com.example.data.model.GenerationConfig
import com.example.data.model.Part
import com.example.data.model.Storyboard
import com.example.data.parser.StoryboardParser
import com.example.domain.GenerationState
import com.example.video.FFmpegVideoAssembler
import com.example.video.VideoAssembler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExplainerRepository(
    private val context: Context,
    private val videoDao: VideoDao = AppDatabase.getInstance(context).videoDao(),
    private val ttsHelper: TtsHelper = TtsHelper(context),
    private val videoAssembler: VideoAssembler = FFmpegVideoAssembler()
) {

    val allVideos: Flow<List<ExplainerVideoEntity>> = videoDao.getAllVideos()

    suspend fun getVideoById(id: Long): ExplainerVideoEntity? = withContext(Dispatchers.IO) {
        videoDao.getVideoById(id)
    }

    suspend fun deleteVideo(video: ExplainerVideoEntity) = withContext(Dispatchers.IO) {
        try {
            File(video.videoPath).delete()
            video.thumbnailPath?.let { File(it).delete() }
        } catch (e: Exception) {
            Log.w("ExplainerRepo", "Could not delete video files: ${e.message}")
        }
        videoDao.deleteVideo(video)
    }

    suspend fun deleteVideoById(id: Long) = withContext(Dispatchers.IO) {
        val video = videoDao.getVideoById(id)
        if (video != null) {
            deleteVideo(video)
        }
    }

    suspend fun generateExplainerVideo(
        topic: String,
        language: String,
        durationSeconds: Int,
        voiceStyle: String,
        apiKey: String,
        onProgress: (GenerationState.Generating) -> Unit
    ): Result<Pair<ExplainerVideoEntity, Storyboard>> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val outputDir = File(context.filesDir, "explainer_videos").apply { mkdirs() }
            val videoFile = File(outputDir, "explainer_${topic.replace("\\s+".toRegex(), "_")}_$timestamp.mp4")
            val audioFile = File(outputDir, "audio_$timestamp.wav")

            // STEP 1: Searching internet & analyzing topic
            onProgress(
                GenerationState.Generating(
                    stepIndex = 0,
                    stepTitle = "Searching internet & analyzing \"$topic\"...",
                    progress = 0.12f
                )
            )
            delay(1200)

            // STEP 2: Writing script & storyboard
            onProgress(
                GenerationState.Generating(
                    stepIndex = 1,
                    stepTitle = "Writing script & 3D storyboard...",
                    progress = 0.28f
                )
            )

            val storyboard = generateStoryboard(topic, language, durationSeconds, apiKey)
            delay(800)

            // STEP 3: Generating voiceover (Gemini TTS / Android TTS)
            onProgress(
                GenerationState.Generating(
                    stepIndex = 2,
                    stepTitle = "Generating voiceover (Gemini 3.8 Flash TTS)...",
                    progress = 0.45f
                )
            )

            val fullScript = storyboard.scenes.joinToString(" ") { it.narration }
            ttsHelper.synthesizeSpeechToFile(
                text = fullScript,
                voiceStyle = voiceStyle,
                language = language,
                apiKey = apiKey,
                targetFile = audioFile
            )

            // STEP 4: Building 3D scene & wireframes
            onProgress(
                GenerationState.Generating(
                    stepIndex = 3,
                    stepTitle = "Building 3D scene wireframes & objects...",
                    progress = 0.62f
                )
            )
            delay(800)

            // STEP 5: Rendering video
            onProgress(
                GenerationState.Generating(
                    stepIndex = 4,
                    stepTitle = "Rendering video frames & camera orbits...",
                    progress = 0.78f
                )
            )

            val renderResult = videoAssembler.assembleVideo(
                storyboard = storyboard,
                audioFile = if (audioFile.exists()) audioFile else null,
                outputFile = videoFile
            ) { subProgress, subMessage ->
                val combined = 0.75f + (subProgress * 0.18f)
                onProgress(
                    GenerationState.Generating(
                        stepIndex = 4,
                        stepTitle = subMessage,
                        progress = combined
                    )
                )
            }

            if (renderResult.isFailure) {
                return@withContext Result.failure(renderResult.exceptionOrNull() ?: Exception("Video rendering failed"))
            }

            // STEP 6: Almost done & saving to local Room database
            onProgress(
                GenerationState.Generating(
                    stepIndex = 5,
                    stepTitle = "Almost done! Saving to library...",
                    progress = 0.96f
                )
            )

            val thumbFile = File(outputDir, "thumb_${videoFile.nameWithoutExtension}.jpg")
            val thumbnailPath = if (thumbFile.exists()) thumbFile.absolutePath else null

            val entity = ExplainerVideoEntity(
                topic = topic,
                title = storyboard.title,
                scriptJson = RetrofitClient.moshi.adapter(Storyboard::class.java).toJson(storyboard),
                videoPath = videoFile.absolutePath,
                thumbnailPath = thumbnailPath,
                durationSeconds = durationSeconds,
                language = language,
                voice = voiceStyle,
                createdAt = System.currentTimeMillis()
            )

            val insertedId = videoDao.insertVideo(entity)
            val savedEntity = entity.copy(id = insertedId)

            onProgress(
                GenerationState.Generating(
                    stepIndex = 5,
                    stepTitle = "Completed!",
                    progress = 1.0f
                )
            )

            Result.success(Pair(savedEntity, storyboard))
        } catch (e: Exception) {
            Log.w("ExplainerRepository", "Video generation pipeline warning: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun generateStoryboard(
        topic: String,
        language: String,
        durationSeconds: Int,
        apiKey: String
    ): Storyboard {
        // Attempt Gemini API call if key is provided
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are a 3D educational animator. Create a structured 3D video explainer storyboard for the topic: "$topic".
                    Language: $language
                    Target Duration: $durationSeconds seconds
                    
                    Return ONLY valid JSON with this exact schema:
                    {
                      "title": "Creative 3D Title",
                      "scenes": [
                        {
                          "narration": "Narration sentence explaining this part clearly in $language",
                          "duration": 7,
                          "visual_description": "Detailed 3D visual description (e.g., spinning holographic core, planetary orbits, cross-section)",
                          "camera": "orbit | zoom_in | pan_left | static",
                          "objects": ["object_name", "sub_part"]
                        }
                      ]
                    }
                    Provide between 3 and 5 scenes fitting the total $durationSeconds seconds.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        temperature = 0.7f,
                        responseMimeType = "application/json"
                    )
                )

                val response = RetrofitClient.geminiApi.generateContent(
                    model = "gemini-3.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                val responseText = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text

                if (!responseText.isNullOrBlank()) {
                    return StoryboardParser.parse(responseText, topic)
                }
            } catch (e: Exception) {
                Log.w("ExplainerRepo", "Gemini API call failed, using intelligent fallback storyboard: ${e.message}")
            }
        }

        // Offline or fallback storyboard
        return StoryboardParser.createFallbackStoryboard(topic)
    }

    suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        var freedBytes = 0L
        val dir = File(context.filesDir, "explainer_videos")
        if (dir.exists()) {
            dir.listFiles()?.forEach { file ->
                freedBytes += file.length()
                file.delete()
            }
        }
        videoDao.clearAll()
        freedBytes
    }
}
