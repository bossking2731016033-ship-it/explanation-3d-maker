package com.example.data.parser

import android.util.Log
import com.example.data.api.RetrofitClient
import com.example.data.model.Scene
import com.example.data.model.Storyboard

object StoryboardParser {

    /**
     * Parses a storyboard from raw Gemini response text.
     * Robust against markdown code blocks (```json ... ```) or conversational preambles.
     */
    fun parse(rawJson: String, defaultTitle: String = "3D Explainer"): Storyboard {
        try {
            // Clean markdown blocks
            var cleaned = rawJson.trim()
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.removePrefix("```json")
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.removePrefix("```")
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.removeSuffix("```")
            }
            cleaned = cleaned.trim()

            // Find JSON object bounds if there is surrounding text
            val firstBrace = cleaned.indexOf('{')
            val lastBrace = cleaned.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                cleaned = cleaned.substring(firstBrace, lastBrace + 1)
            }

            val adapter = RetrofitClient.moshi.adapter(Storyboard::class.java)
            val parsed = adapter.fromJson(cleaned)
            if (parsed != null && parsed.scenes.isNotEmpty()) {
                return parsed
            }
        } catch (e: Exception) {
            Log.w("StoryboardParser", "Failed to parse JSON directly, falling back to regex: ${e.message}")
        }

        return createFallbackStoryboard(defaultTitle)
    }

    /**
     * Generates a richly structured fallback storyboard for predefined or custom topics
     * in case of network unavailability or offline usage.
     */
    fun createFallbackStoryboard(topic: String): Storyboard {
        val normalized = topic.lowercase().trim()
        return when {
            normalized.contains("solar") || normalized.contains("space") -> Storyboard(
                title = "The Solar System",
                scenes = listOf(
                    Scene(
                        narration = "At the center of our cosmic neighborhood blazes the Sun, a massive star providing light and gravity.",
                        duration = 7,
                        visualDescription = "Blazing golden Sun sphere with solar flares and coronal loops",
                        camera = "orbit",
                        objects = listOf("sun", "solar_flares", "core")
                    ),
                    Scene(
                        narration = "Eight distinct planets revolve around the Sun along elliptical paths, shaped by gravitational forces.",
                        duration = 8,
                        visualDescription = "Planetary orbital rings stretching outward with terrestrial and gas giant spheres",
                        camera = "zoom_in",
                        objects = listOf("planets", "elliptical_orbits", "gravity_wells")
                    ),
                    Scene(
                        narration = "Earth rests comfortably within the habitable Goldilocks zone, sustaining liquid oceans and vibrant life.",
                        duration = 8,
                        visualDescription = "Vibrant blue Earth sphere with swirling white atmospheric clouds and moon orbit",
                        camera = "pan_left",
                        objects = listOf("earth", "atmosphere", "moon")
                    ),
                    Scene(
                        narration = "Beyond lies the mysterious Kuiper belt and vast interstellar depths, waiting to be explored.",
                        duration = 7,
                        visualDescription = "Distant icy comets and glittering starry horizon in 3D perspective",
                        camera = "orbit",
                        objects = listOf("kuiper_belt", "deep_space", "starfield")
                    )
                )
            )
            normalized.contains("heart") || normalized.contains("blood") -> Storyboard(
                title = "The Human Heart",
                scenes = listOf(
                    Scene(
                        narration = "The human heart is an astonishing muscular pump beating over one hundred thousand times every single day.",
                        duration = 7,
                        visualDescription = "Anatomical 3D heart with glowing chambers and rhythmic muscular contractions",
                        camera = "orbit",
                        objects = listOf("heart", "ventricles", "muscle_fibers")
                    ),
                    Scene(
                        narration = "Four valves regulate one-way blood flow through two atria and two muscular ventricles.",
                        duration = 8,
                        visualDescription = "Cutaway view revealing tricuspid and mitral valves opening and closing",
                        camera = "zoom_in",
                        objects = listOf("valves", "atria", "ventricles")
                    ),
                    Scene(
                        narration = "Oxygen-rich blood is propelled across thousands of miles of arteries to nourish every living cell.",
                        duration = 8,
                        visualDescription = "Pulsing red arterial network carrying oxygenated corpuscles through glowing vessels",
                        camera = "pan_left",
                        objects = listOf("aorta", "arteries", "oxygen_cells")
                    )
                )
            )
            normalized.contains("water") || normalized.contains("cycle") -> Storyboard(
                title = "The Water Cycle",
                scenes = listOf(
                    Scene(
                        narration = "Solar heat warms oceans and lakes, causing liquid water to evaporate and ascend as invisible vapor.",
                        duration = 7,
                        visualDescription = "Sun rays striking glistening ocean surface with ascending steam vapor particles",
                        camera = "pan_left",
                        objects = listOf("ocean", "evaporation", "heat_rays")
                    ),
                    Scene(
                        narration = "Cooling high in the atmosphere, vapor condenses around microscopic dust particles to form dense clouds.",
                        duration = 8,
                        visualDescription = "Atmospheric vapor particles coalescing into dense volumetric 3D cumulus clouds",
                        camera = "zoom_in",
                        objects = listOf("condensation", "cloud_particles", "cooling_air")
                    ),
                    Scene(
                        narration = "When water droplets become too heavy, precipitation falls as rain and snow, replenishing rivers and oceans.",
                        duration = 8,
                        visualDescription = "Rain streams falling over mountainous terrain returning water to winding river channels",
                        camera = "orbit",
                        objects = listOf("precipitation", "mountains", "river_runoff")
                    )
                )
            )
            normalized.contains("black hole") -> Storyboard(
                title = "Black Holes",
                scenes = listOf(
                    Scene(
                        narration = "When massive stars exhaust their nuclear fuel, they undergo gravitational collapse into an infinite point.",
                        duration = 7,
                        visualDescription = "Supernova explosion collapsing inwards into a spacetime funnel",
                        camera = "zoom_in",
                        objects = listOf("collapsing_star", "spacetime", "singularity")
                    ),
                    Scene(
                        narration = "Surrounding the singularity is the event horizon, the cosmic boundary from which not even light can escape.",
                        duration = 8,
                        visualDescription = "Shadow disk with glowing relativistic photon sphere and gravitationally warped light",
                        camera = "orbit",
                        objects = listOf("event_horizon", "photon_sphere", "gravitational_lensing")
                    ),
                    Scene(
                        narration = "Superheated matter whirls inside the accretion disk, emitting energetic X-rays before vanishing forever.",
                        duration = 8,
                        visualDescription = "Fiery orange accretion disk swirling at relativistic speeds around the black void",
                        camera = "pan_left",
                        objects = listOf("accretion_disk", "plasma_jets", "doppler_beaming")
                    )
                )
            )
            normalized.contains("internet") || normalized.contains("web") -> Storyboard(
                title = "How The Internet Works",
                scenes = listOf(
                    Scene(
                        narration = "The internet is a vast global network connecting billions of computers through undersea cables and satellites.",
                        duration = 7,
                        visualDescription = "Digital wireframe globe with glowing fiber-optic cables crisscrossing oceans",
                        camera = "orbit",
                        objects = listOf("global_mesh", "fiber_optics", "satellites")
                    ),
                    Scene(
                        narration = "Data is chopped into tiny digital packets, stamped with IP addresses, and routed through high-speed switches.",
                        duration = 8,
                        visualDescription = "Glowing data packets streaming along digital highways through routing nodes",
                        camera = "zoom_in",
                        objects = listOf("data_packets", "routers", "ip_addresses")
                    ),
                    Scene(
                        narration = "Servers instantly reassemble the packets into webpages, videos, and messages on your screen.",
                        duration = 8,
                        visualDescription = "Data packets uniting seamlessly into a rich interactive 3D digital interface",
                        camera = "pan_left",
                        objects = listOf("servers", "reconstruction", "user_device")
                    )
                )
            )
            else -> Storyboard(
                title = topic.replaceFirstChar { it.uppercase() },
                scenes = listOf(
                    Scene(
                        narration = "Let us explore the core fundamental principles behind $topic.",
                        duration = 7,
                        visualDescription = "Dynamic 3D model representing $topic with glowing dimensional components",
                        camera = "orbit",
                        objects = listOf("core_structure", "energy_field")
                    ),
                    Scene(
                        narration = "Each interconnected element plays a crucial role in shaping how $topic behaves and functions.",
                        duration = 8,
                        visualDescription = "Interactive exploded view of components revealing internal operational dynamics",
                        camera = "zoom_in",
                        objects = listOf("subsystems", "internal_mechanisms")
                    ),
                    Scene(
                        narration = "Understanding these mechanisms allows us to innovate and apply $topic to real-world technology.",
                        duration = 8,
                        visualDescription = "3D hologram illuminating global applications and futuristic technological connections",
                        camera = "pan_left",
                        objects = listOf("global_application", "future_innovation")
                    )
                )
            )
        }
    }
}
