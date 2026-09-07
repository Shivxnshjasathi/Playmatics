package com.zincstate.playmatics.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zincstate.playmatics.domain.repository.PlayerStats
import com.zincstate.playmatics.domain.repository.PuzzleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    puzzleRepository: PuzzleRepository
) : ViewModel() {

    val stats: StateFlow<PlayerStats> = puzzleRepository.observeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerStats())

    companion object {
        fun formatTime(ms: Long): String {
            if (ms <= 0) return "—"
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "${minutes}m ${seconds}s"
        }
    }
}
