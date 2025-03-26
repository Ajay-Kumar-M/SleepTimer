package com.example.sleeptimer.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class SleepTimerWorker(appContext: Context, params: WorkerParameters) : Worker(appContext,params) {

    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val focusRequest: AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        ).build()

    override fun doWork(): Result {
        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Proceed with media playback
            Log.d("MainTimerView","Media Paused after :  minutes")
            return Result.success(Data.Builder().putString("Status","Media Paused").build())
        } else {
            // Handle the case where audio focus was not granted
            Log.d("MainTimerView","Failed to Pause Media after :  minutes")
            return  Result.failure(Data.Builder().putString("Status","Media Failed to Pause").build())
        }
    }

}