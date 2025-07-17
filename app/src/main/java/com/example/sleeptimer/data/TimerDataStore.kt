package com.example.sleeptimer.data
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

val Context.timerDataStore by preferencesDataStore(name = "timer_prefs")

object TimerPrefs {
    val END_TIMESTAMP = longPreferencesKey("end_timestamp")
    val IS_RUNNING = booleanPreferencesKey("is_running")
    val IS_MEDIA_VOLUME_CHECKED = booleanPreferencesKey("is_media_volume_checked")
    val REMAINING_MILLIS = longPreferencesKey("remaining_millis")
}
