package com.example.sleeptimer.components

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class MediaVolumeWorker(appContext: Context, params: WorkerParameters) : Worker(appContext,params) {

    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun doWork(): Result {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val volumeChange = -1 // Use -1 to decrease, 1 to increase
        val newVolume = (currentVolume + volumeChange).coerceIn(0, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        // Set the new volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        Log.d("MediaVolumeWorker","Media Volume Decreased to : $newVolume")
        return Result.success(Data.Builder().putString("Status","Media Volume Reduced").build())
    }

}