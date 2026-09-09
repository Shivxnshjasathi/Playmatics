package com.zincstate.playmatics.presentation.audio

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.view.animation.LinearInterpolator
import com.zincstate.playmatics.R
import kotlinx.coroutines.*

class AudioPlayer(private val context: Context) {

    private var currentPlayer: MediaPlayer? = null
    private var fadingOutPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null

    private var sfxClickId: Int = 0
    private var sfxErrorId: Int = 0
    private var sfxWinId: Int = 0

    private var isMusicEnabled = false
    private var isSfxEnabled = true

    private var currentPlaylist = emptyList<Int>()
    private var currentSongIndex = 0

    private val crossfadeDurationMs = 3000L
    private val maxVolume = 0.3f
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var crossfadeAnimator: ValueAnimator? = null
    private var isCrossfading = false

    fun init() {
        // Init SoundPool for SFX
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            soundPool?.let { pool ->
                sfxClickId = pool.load(context, R.raw.sfx_click, 1)
                sfxErrorId = pool.load(context, R.raw.sfx_error, 1)
                sfxWinId = pool.load(context, R.raw.sfx_win, 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (isMusicEnabled) {
            resumeMusic()
        } else {
            pauseMusic()
        }
    }

    private fun startCrossfadeToNext() {
        if (isCrossfading || !isMusicEnabled) return
        isCrossfading = true

        if (currentSongIndex >= currentPlaylist.size || currentPlaylist.isEmpty()) {
            currentPlaylist = listOf(
                R.raw.notes_on_a_rainy_desk,
                R.raw.solving_for_peace,
                R.raw.the_final_row
            ).shuffled()
            currentSongIndex = 0
        }

        try {
            val nextPlayer = MediaPlayer.create(context, currentPlaylist[currentSongIndex]).apply {
                setVolume(0f, 0f)
                start()
            }
            currentSongIndex++

            fadingOutPlayer = currentPlayer
            currentPlayer = nextPlayer

            crossfadeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = crossfadeDurationMs
                interpolator = LinearInterpolator()
                addUpdateListener { animation ->
                    val progress = animation.animatedValue as Float
                    if (isMusicEnabled) {
                        fadingOutPlayer?.setVolume(maxVolume * (1f - progress), maxVolume * (1f - progress))
                        currentPlayer?.setVolume(maxVolume * progress, maxVolume * progress)
                    }
                }
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        fadingOutPlayer?.release()
                        fadingOutPlayer = null
                        isCrossfading = false
                        scheduleNextCrossfade()
                    }
                })
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isCrossfading = false
        }
    }

    private fun playNextSong() {
        fadingOutPlayer?.release()
        fadingOutPlayer = null
        currentPlayer?.release()
        
        if (currentSongIndex >= currentPlaylist.size || currentPlaylist.isEmpty()) {
            currentPlaylist = listOf(
                R.raw.notes_on_a_rainy_desk,
                R.raw.solving_for_peace,
                R.raw.the_final_row
            ).shuffled()
            currentSongIndex = 0
        }
        
        try {
            currentPlayer = MediaPlayer.create(context, currentPlaylist[currentSongIndex]).apply {
                setVolume(maxVolume, maxVolume)
                if (isMusicEnabled) start()
            }
            currentSongIndex++
            scheduleNextCrossfade()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleNextCrossfade() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val player = currentPlayer
                if (player != null && player.isPlaying) {
                    val remainingTime = player.duration - player.currentPosition
                    if (remainingTime <= crossfadeDurationMs && remainingTime > 0) {
                        startCrossfadeToNext()
                        break
                    }
                }
                delay(500)
            }
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled = enabled
    }

    fun playClick() {
        if (isSfxEnabled && sfxClickId != 0) {
            soundPool?.play(sfxClickId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playError() {
        if (isSfxEnabled && sfxErrorId != 0) {
            soundPool?.play(sfxErrorId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playWin() {
        if (isSfxEnabled && sfxWinId != 0) {
            soundPool?.play(sfxWinId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun resumeMusic() {
        if (isMusicEnabled) {
            try {
                if (currentPlayer == null) {
                    playNextSong()
                } else if (currentPlayer?.isPlaying == false) {
                    currentPlayer?.start()
                    scheduleNextCrossfade()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pauseMusic() {
        try {
            progressJob?.cancel()
            crossfadeAnimator?.cancel()
            
            fadingOutPlayer?.release()
            fadingOutPlayer = null
            isCrossfading = false

            if (currentPlayer?.isPlaying == true) {
                currentPlayer?.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        scope.cancel()
        crossfadeAnimator?.cancel()
        fadingOutPlayer?.release()
        fadingOutPlayer = null
        currentPlayer?.release()
        currentPlayer = null
        soundPool?.release()
        soundPool = null
    }
}
