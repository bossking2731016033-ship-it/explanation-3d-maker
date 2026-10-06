package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "explainer_videos")
data class ExplainerVideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topic: String,
    val title: String,
    val scriptJson: String,
    val videoPath: String,
    val thumbnailPath: String? = null,
    val durationSeconds: Int,
    val language: String,
    val voice: String,
    val createdAt: Long = System.currentTimeMillis()
)
