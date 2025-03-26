package com.example.sleeptimer.model

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import com.example.sleeptimer.notification.TimerNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Timer
import java.util.TimerTask

object TimerSingleton {

    private val timer = Timer()
    private var runningTask: TimerTask = object : TimerTask() {
        override fun run() {
            // Task implementation will be defined later
        }
    }
    private val _isNotificationRunning = MutableStateFlow(false)
    val isNotificationRunning: StateFlow<Boolean>
        get() = _isNotificationRunning.asStateFlow()
    private val _liveTimer : MutableState<Int> = mutableIntStateOf(0)
    val liveTimer: Int
        get() = _liveTimer.value
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean>
        get() = _isTimerRunning.asStateFlow()

    fun startTimer(processedMins: Int, timerNotificationService: TimerNotificationService, withNotification: Boolean) {

        _liveTimer.value = processedMins
        if (withNotification) { toggleTimerNotification(timerNotificationService) }
        runningTask = object : TimerTask() {
            override fun run() {
                // Code to execute after 1 minute
                _liveTimer.value -= 1
                if (withNotification) { updateTimerNotification(timerNotificationService) }
                Log.d("Timer_Singleton","run executed : ${processedMins} minutes $liveTimer")
                if (liveTimer == 0) {
                    toggleTimer()
                }
            }
        }
        timer.schedule(runningTask, 60000,60000)
        Log.d("Timer_Singleton","Timer scheduled : ${processedMins} minutes")
    }

    private fun updateTimerNotification(timerNotificationService: TimerNotificationService) {
        timerNotificationService.showNotification(liveTimer)
    }

    private fun toggleTimerNotification(timerNotificationService: TimerNotificationService) {
        _isNotificationRunning.value = !_isNotificationRunning.value
        if (isNotificationRunning.value)
            updateTimerNotification(timerNotificationService)
        else
            timerNotificationService.stopNotification()
    }

    fun extendSleepTimer(timerNotificationService: TimerNotificationService) {
        _liveTimer.value += 5
        if (isNotificationRunning.value) {
            timerNotificationService.showNotification(liveTimer)
        }
    }

    fun stopTimer(timerNotificationService: TimerNotificationService) {
        Log.d("Timer_Singleton","StopTimer called : ${liveTimer} minutes")
        runningTask.cancel()
        if (isNotificationRunning.value) {
            toggleTimerNotification(timerNotificationService)
        }
    }

    fun toggleTimer() {
        _isTimerRunning.value = !_isTimerRunning.value
    }
}