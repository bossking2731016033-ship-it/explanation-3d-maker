package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Storyboard(
    @Json(name = "title") val title: String = "3D Explainer",
    @Json(name = "scenes") val scenes: List<Scene> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Scene(
    @Json(name = "narration") val narration: String = "",
    @Json(name = "duration") val duration: Int = 6,
    @Json(name = "visual_description") val visualDescription: String = "",
    @Json(name = "camera") val camera: String = "orbit", // zoom_in, pan_left, orbit, static
    @Json(name = "objects") val objects: List<String> = emptyList()
)

enum class CameraMotion {
    ZOOM_IN,
    PAN_LEFT,
    ORBIT,
    STATIC;

    companion object {
        fun fromString(value: String): CameraMotion {
            return when (value.lowercase().trim()) {
                "zoom_in", "zoom" -> ZOOM_IN
                "pan_left", "pan", "pan_right" -> PAN_LEFT
                "orbit", "rotate", "3d_orbit" -> ORBIT
                else -> STATIC
            }
        }
    }
}
