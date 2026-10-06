package com.example.domain

import com.example.data.local.ExplainerVideoEntity
import com.example.data.model.Storyboard

sealed class GenerationState {
    object Idle : GenerationState()

    data class Generating(
        val stepIndex: Int,
        val totalSteps: Int = 6,
        val stepTitle: String,
        val progress: Float
    ) : GenerationState()

    data class Success(
        val video: ExplainerVideoEntity,
        val storyboard: Storyboard
    ) : GenerationState()

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : GenerationState()
}
