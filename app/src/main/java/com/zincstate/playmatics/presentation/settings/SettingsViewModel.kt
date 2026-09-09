package com.zincstate.playmatics.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zincstate.playmatics.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<Boolean?> = settingsRepository.observeThemeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themePalette: StateFlow<String> = settingsRepository.observeThemePalette()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Sunset Minimalist")

    val hapticsEnabled: StateFlow<Boolean> = settingsRepository.observeHapticsEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val highlightMistakes: StateFlow<Boolean> = settingsRepository.observeHighlightMistakes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val musicEnabled: StateFlow<Boolean> = settingsRepository.observeMusicEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val sfxEnabled: StateFlow<Boolean> = settingsRepository.observeSfxEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val mistakeLimitEnabled: StateFlow<Boolean> = settingsRepository.observeMistakeLimitEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(isDark: Boolean?) {
        viewModelScope.launch { settingsRepository.setThemeMode(isDark) }
    }

    fun setThemePalette(palette: String) {
        viewModelScope.launch { settingsRepository.setThemePalette(palette) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun setHighlightMistakes(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHighlightMistakes(enabled) }
    }

    fun setMistakeLimitEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMistakeLimitEnabled(enabled) }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMusicEnabled(enabled) }
    }

    fun setSfxEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSfxEnabled(enabled) }
    }
}
