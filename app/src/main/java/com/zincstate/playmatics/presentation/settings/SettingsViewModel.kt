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

    val hapticsEnabled: StateFlow<Boolean> = settingsRepository.observeHapticsEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val highlightMistakes: StateFlow<Boolean> = settingsRepository.observeHighlightMistakes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(isDark: Boolean?) {
        viewModelScope.launch { settingsRepository.setThemeMode(isDark) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun setHighlightMistakes(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHighlightMistakes(enabled) }
    }
}
