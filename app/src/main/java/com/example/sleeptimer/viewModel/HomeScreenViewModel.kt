package com.example.sleeptimer.viewModel

import android.app.Notification
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.example.sleeptimer.notification.TimerNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Timer
import java.util.TimerTask
import kotlin.time.Duration.Companion.seconds


class HomeScreenViewModel : ViewModel() {

    private var _processedAngle : MutableState<Int> = mutableIntStateOf(0)
    private var _previousAngle : MutableState<Int> = mutableIntStateOf(0)
    private var totalHours : MutableState<UInt> = mutableStateOf(0U)
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean>
        get() = _isTimerRunning.asStateFlow()
    private val timer = Timer()
    private var runningTask: TimerTask = object : TimerTask() {
        override fun run() {
            // Task implementation will be defined later
        }
    }
    private var hasNotificationPermission: Boolean = false
    private val _delayInMillis : MutableState<Int> = mutableIntStateOf(0)
    val delayInMillis: Int
        get() = _delayInMillis.value

    val processedMins : Int
        get() = _processedAngle.value.div(6).plus(totalHours.value.toInt() * 60)

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

    fun pauseMedia(context: Context, withNotification: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            ).build()

        _delayInMillis.value = processedMins
        if (withNotification) { toggleTimerNotification(context) }

        runningTask = object : TimerTask() {
            override fun run() {
                // Code to execute after 1 minute
                _delayInMillis.value -= 1
                if (withNotification) { toggleTimerNotification(context) }
                Log.d("MainTimerView","run executed : ${processedMins} minutes $delayInMillis")
                if (delayInMillis == 0) {
                    val result = audioManager.requestAudioFocus(focusRequest)
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        // Proceed with media playback
                        Log.d("MainTimerView","Media Paused after : ${processedMins} minutes")
                        toggleTimer()
                        stopTimer(context,withNotification)
                    } else {
                        // Handle the case where audio focus was not granted
                        Log.d("MainTimerView","Failed to Pause Media after : ${processedMins} minutes")
                    }
                }
            }
        }
        timer.schedule(runningTask, 60000,60000)
        Log.d("MainTimerView","Timer scheduled : ${processedMins} minutes")
    }

    fun stopTimer(context: Context,withNotification: Boolean) {
        runningTask.cancel()
        if (withNotification) { toggleTimerNotification(context) }
        //timer.cancel()
    }

    fun toggleTimer() {
            _isTimerRunning.value = !_isTimerRunning.value
    }

    fun changeNotificationPermissionState(permissionState: Boolean) {
        hasNotificationPermission = permissionState
    }

    fun extendSleepTimer() {
        _delayInMillis.value += 5
    }

    private fun toggleTimerNotification(context: Context) {
        val timerNotificationService = TimerNotificationService(context)
        if (isTimerRunning.value)
            timerNotificationService.showNotification(_delayInMillis.value)
        else
            timerNotificationService.stopNotification()
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