package com.example.video

import com.example.data.model.Storyboard
import java.io.File

interface VideoAssembler {
    suspend fun assembleVideo(
        storyboard: Storyboard,
        audioFile: File?,
        outputFile: File,
        onProgress: (Float, String) -> Unit
    ): Result<File>
}
