package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.ExplainerApp
import com.example.data.local.ExplainerVideoEntity
import com.example.data.local.UserSettings
import com.example.data.model.Storyboard
import com.example.data.parser.StoryboardParser
import com.example.domain.GenerationState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExplainerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExplainerApp
    private val repository = app.explainerRepository
    private val dataStore = app.appSettingsDataStore

    // History Flow from Room
    val historyList: StateFlow<List<ExplainerVideoEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings Flow from DataStore
    val userSettings: StateFlow<UserSettings> = dataStore.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    // UI Input States
    private val _topicInput = MutableStateFlow("")
    val topicInput: StateFlow<String> = _topicInput.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedDuration = MutableStateFlow(30)
    val selectedDuration: StateFlow<Int> = _selectedDuration.asStateFlow()

    private val _selectedVoice = MutableStateFlow("Male")
    val selectedVoice: StateFlow<String> = _selectedVoice.asStateFlow()

    // Generation State
    private val _generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    // Current Active Video for Result Screen
    private val _currentVideo = MutableStateFlow<ExplainerVideoEntity?>(null)
    val currentVideo: StateFlow<ExplainerVideoEntity?> = _currentVideo.asStateFlow()

    private val _currentStoryboard = MutableStateFlow<Storyboard?>(null)
    val currentStoryboard: StateFlow<Storyboard?> = _currentStoryboard.asStateFlow()

    private var generationJob: Job? = null

    init {
        // Initialize defaults from saved preferences
        viewModelScope.launch {
            userSettings.collect { settings ->
                if (_selectedLanguage.value == "English" && settings.defaultLanguage != "English") {
                    _selectedLanguage.value = settings.defaultLanguage
                }
                if (_selectedDuration.value == 30 && settings.defaultDuration != 30) {
                    _selectedDuration.value = settings.defaultDuration
                }
                if (_selectedVoice.value == "Male" && settings.defaultVoice != "Male") {
                    _selectedVoice.value = settings.defaultVoice
                }
            }
        }
    }

    fun onTopicChange(topic: String) {
        _topicInput.value = topic
    }

    fun onSuggestionSelect(suggestion: String) {
        _topicInput.value = suggestion
    }

    fun onLanguageSelect(lang: String) {
        _selectedLanguage.value = lang
    }

    fun onDurationSelect(duration: Int) {
        _selectedDuration.value = duration
    }

    fun onVoiceSelect(voice: String) {
        _selectedVoice.value = voice
    }

    fun startGeneration(onNavigateToLoading: () -> Unit) {
        val topic = _topicInput.value.trim()
        if (topic.isBlank()) return

        onNavigateToLoading()

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            val keyFromSettings = userSettings.value.apiKey
            val effectiveApiKey = if (keyFromSettings.isNotBlank()) keyFromSettings else BuildConfig.GEMINI_API_KEY

            val result = repository.generateExplainerVideo(
                topic = topic,
                language = _selectedLanguage.value,
                durationSeconds = _selectedDuration.value,
                voiceStyle = _selectedVoice.value,
                apiKey = effectiveApiKey
            ) { progressState ->
                _generationState.value = progressState
            }

            result.onSuccess { (entity, storyboard) ->
                _currentVideo.value = entity
                _currentStoryboard.value = storyboard
                _generationState.value = GenerationState.Success(entity, storyboard)
            }.onFailure { error ->
                _generationState.value = GenerationState.Error(
                    message = error.message ?: "Failed to generate video. Please verify your connection.",
                    canRetry = true
                )
            }
        }
    }

    fun cancelGeneration(onNavBack: () -> Unit) {
        generationJob?.cancel()
        _generationState.value = GenerationState.Idle
        onNavBack()
    }

    fun selectVideo(video: ExplainerVideoEntity, onNavigateToResult: () -> Unit) {
        _currentVideo.value = video
        _currentStoryboard.value = StoryboardParser.parse(video.scriptJson, video.title)
        onNavigateToResult()
    }

    fun deleteVideo(video: ExplainerVideoEntity) {
        viewModelScope.launch {
            repository.deleteVideo(video)
            if (_currentVideo.value?.id == video.id) {
                _currentVideo.value = null
                _currentStoryboard.value = null
            }
        }
    }

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            dataStore.saveApiKey(apiKey)
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            dataStore.setDarkMode(isDark)
        }
    }

    fun setDefaultLanguage(language: String) {
        viewModelScope.launch {
            dataStore.saveLanguage(language)
            _selectedLanguage.value = language
        }
    }

    fun setDefaultDuration(duration: Int) {
        viewModelScope.launch {
            dataStore.saveDuration(duration)
            _selectedDuration.value = duration
        }
    }

    fun setDefaultVoice(voice: String) {
        viewModelScope.launch {
            dataStore.saveVoice(voice)
            _selectedVoice.value = voice
        }
    }

    fun clearCache(onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val freed = repository.clearCache()
            onComplete(freed)
        }
    }
}
