package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM explainer_videos ORDER BY createdAt DESC")
    fun getAllVideos(): Flow<List<ExplainerVideoEntity>>

    @Query("SELECT * FROM explainer_videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): ExplainerVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: ExplainerVideoEntity): Long

    @Delete
    suspend fun deleteVideo(video: ExplainerVideoEntity)

    @Query("DELETE FROM explainer_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("DELETE FROM explainer_videos")
    suspend fun clearAll()
}
