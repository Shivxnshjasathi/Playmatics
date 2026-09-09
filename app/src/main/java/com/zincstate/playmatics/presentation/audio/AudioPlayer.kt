package com.zincstate.playmatics.presentation.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.zincstate.playmatics.R

class AudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null

    private var sfxClickId: Int = 0
    private var sfxErrorId: Int = 0
    private var sfxWinId: Int = 0

    private var isMusicEnabled = true
    private var isSfxEnabled = true

    private var currentPlaylist = emptyList<Int>()
    private var currentSongIndex = 0

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

    private fun playNextSong() {
        mediaPlayer?.release()
        
        if (currentSongIndex >= currentPlaylist.size || currentPlaylist.isEmpty()) {
            currentPlaylist = listOf(
                R.raw.notes_on_a_rainy_desk,
                R.raw.solving_for_peace,
                R.raw.the_final_row
            ).shuffled()
            currentSongIndex = 0
        }
        
        try {
            mediaPlayer = MediaPlayer.create(context, currentPlaylist[currentSongIndex]).apply {
                setVolume(0.3f, 0.3f)
                setOnCompletionListener {
                    if (isMusicEnabled) playNextSong()
                }
                if (isMusicEnabled) start()
            }
            currentSongIndex++
        } catch (e: Exception) {
            e.printStackTrace()
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
                if (mediaPlayer == null) {
                    playNextSong()
                } else if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        soundPool?.release()
        soundPool = null
    }
}
