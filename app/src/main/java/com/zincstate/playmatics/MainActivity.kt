package com.zincstate.playmatics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.zincstate.playmatics.data.remote.ConnectivityObserver
import com.zincstate.playmatics.domain.repository.SettingsRepository
import com.zincstate.playmatics.presentation.navigation.NavGraph
import com.zincstate.playmatics.ui.theme.PlaymaticsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var connectivityObserver: ConnectivityObserver
    @Inject lateinit var audioPlayer: com.zincstate.playmatics.presentation.audio.AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsRepository.observeThemeMode()
                .collectAsState(initial = null)

            val musicEnabled = false
            val sfxEnabled by settingsRepository.observeSfxEnabled()
                .collectAsState(initial = true)
                
            androidx.compose.runtime.LaunchedEffect(musicEnabled) {
                audioPlayer.setMusicEnabled(musicEnabled)
            }
            androidx.compose.runtime.LaunchedEffect(sfxEnabled) {
                audioPlayer.setSfxEnabled(sfxEnabled)
            }

            PlaymaticsTheme(
                darkTheme = when (themeMode) {
                    true -> true
                    false -> false
                    null -> androidx.compose.foundation.isSystemInDarkTheme()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        connectivityObserver = connectivityObserver
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        audioPlayer.resumeMusic()
    }

    override fun onStop() {
        super.onStop()
        audioPlayer.pauseMusic()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}