package com.example.sleeptimer.viewModel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleeptimer.data.TimerPrefs
import com.example.sleeptimer.data.timerDataStore
import com.example.sleeptimer.model.TimerSingleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HomeScreenViewModel(app: Application) : AndroidViewModel(app) {

    private val dataStore = app.timerDataStore
    private var _processedAngle : MutableState<Int> = mutableIntStateOf(0)
    private var _previousAngle : MutableState<Int> = mutableIntStateOf(0)
    private var totalHours : MutableState<UInt> = mutableStateOf(0U)
    private var hasNotificationPermission: Boolean = (
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                app.applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    )
    val processedMins : Int
        get() = _processedAngle.value.div(6).plus(totalHours.value.toInt() * 60)
    private val _isMediaVolumeChecked = MutableStateFlow(false)
    val isMediaVolumeChecked: StateFlow<Boolean>
        get() = _isMediaVolumeChecked.asStateFlow()

    init {
        viewModelScope.launch {
            Log.d("HomeScreenViewModel","init called")
            dataStore.data.firstOrNull()?.let { prefs ->
                TimerSingleton.setTimerState(prefs[TimerPrefs.IS_RUNNING] ?: false)
                Log.d("HomeScreenViewModel","timer state ${prefs[TimerPrefs.IS_RUNNING]} - ${prefs[TimerPrefs.END_TIMESTAMP]}")
//                val pausedMillis = prefs[TimerPrefs.REMAINING_MILLIS] ?: 0L
                val targetTimestamp = prefs[TimerPrefs.END_TIMESTAMP] ?: 0L
                if (TimerSingleton.isTimerRunning.value) {
                    val remainingMilliSecs = (targetTimestamp - System.currentTimeMillis()).coerceAtLeast(0)
                    val remainingMins = (remainingMilliSecs/60000).toInt()
                    TimerSingleton.setLiveTimer(remainingMins)
                    Log.d("HomeScreenViewModel","timer value ${remainingMins}")
                    _isMediaVolumeChecked.value = prefs[TimerPrefs.IS_MEDIA_VOLUME_CHECKED] ?: false
                    TimerSingleton.startTimer(remainingMins, hasNotificationPermission, isMediaVolumeChecked.value)
                    saveState()
                } else {
//                    _remainingMillis.value = pausedMillis
                }
            }
        }
    }

    fun onAngleChanged(currentAngle: Double){
        val tempCurrentAngle = currentAngle.toInt()
        if((_previousAngle.value>350)&&(tempCurrentAngle<10)){
            totalHours.value  = totalHours.value.plus(1u)
        } else if((_previousAngle.value<10)&&(tempCurrentAngle>350)){
            totalHours.value = totalHours.value.minus(1u)
        }
        _processedAngle.value = tempCurrentAngle
        _previousAngle.value = tempCurrentAngle
    }

    fun changeNotificationPermissionState(permissionState: Boolean) {
        hasNotificationPermission = permissionState
    }

    fun toggleMediaVolumeCheckbox() {
        _isMediaVolumeChecked.value = !_isMediaVolumeChecked.value
    }

    internal fun saveState() {
        val targetTimestamp = System.currentTimeMillis() + (processedMins*60000)
        viewModelScope.launch {
            Log.d("HomeScreenViewModel","savestate called ${TimerSingleton.isTimerRunning.value} ${TimerSingleton.liveTimer.toLong()}")
            dataStore.edit { prefs ->
                prefs[TimerPrefs.END_TIMESTAMP] = targetTimestamp
                prefs[TimerPrefs.REMAINING_MILLIS] = TimerSingleton.liveTimer.toLong()
                prefs[TimerPrefs.IS_RUNNING] = TimerSingleton.isTimerRunning.value
                prefs[TimerPrefs.IS_MEDIA_VOLUME_CHECKED] = isMediaVolumeChecked.value
            }
        }
    }
}

/*

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
//            .setOnAudioFocusChangeListener { focusChange ->
//                when (focusChange) {
//                    AudioManager.AUDIOFOCUS_LOSS -> {
//                        // Pause your media playback
//                    }
//                    AudioManager.AUDIOFOCUS_GAIN -> {
//                        // Resume your media playback
//                    }
//                    // Handle other focus changes as needed
//                }
//            }
            .build()
 */