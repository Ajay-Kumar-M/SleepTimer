package com.example.sleeptimer.model

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sleeptimer.MyApp
import com.example.sleeptimer.components.MediaVolumeWorker
import com.example.sleeptimer.components.SleepTimerWorker
import com.example.sleeptimer.data.TimerPrefs
import com.example.sleeptimer.data.timerDataStore
import com.example.sleeptimer.notification.TimerNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

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
    private lateinit var appContext: Context
    private lateinit var application: MyApp
    private lateinit var dataStore: DataStore<Preferences>

    fun initialize(context: Context) {
        appContext = context.applicationContext
        application = appContext.applicationContext as MyApp
        dataStore = application.timerDataStore
    }

    fun startTimer(processedMins: Int, withNotification: Boolean, isMediaVolumeChecked: Boolean) {

        var sleepTimerWorkRequest:OneTimeWorkRequest
        var mediaVolumeWorkRequest:PeriodicWorkRequest
        _liveTimer.value = processedMins
        if (withNotification) { toggleTimerNotification(application.timerNotificationService) }
        runningTask = object : TimerTask() {
            override fun run() {
                // Code to execute after 1 minute
                _liveTimer.value -= 1
                if (withNotification) {
                    Log.d("Timer_Singleton","notification executed : ${withNotification} minutes $liveTimer")
                    updateTimerNotification(application.timerNotificationService)
                }
                Log.d("Timer_Singleton","run executed : ${processedMins} minutes $liveTimer")
                if (liveTimer == 0) {
                    toggleTimer()
                }
            }
        }
        timer.schedule(runningTask, 60000,60000)

        sleepTimerWorkRequest = OneTimeWorkRequestBuilder<SleepTimerWorker>()
            .setInitialDelay(Duration.ofMinutes(TimerSingleton.liveTimer.toLong()))
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = Duration.ofSeconds(10)
            )
            .addTag("sleep_timer_work")
            .build()
        application.applicationWorkManager.enqueueUniqueWork("sleep_timer_work",ExistingWorkPolicy.REPLACE,sleepTimerWorkRequest)
        if (isMediaVolumeChecked) {
            mediaVolumeWorkRequest = PeriodicWorkRequestBuilder<MediaVolumeWorker>(15, TimeUnit.MINUTES)
                .setInitialDelay(15,TimeUnit.MINUTES)
                .addTag("media_volume_work")
                .build()
            application.applicationWorkManager.enqueueUniquePeriodicWork("media_volume_work",ExistingPeriodicWorkPolicy.UPDATE,mediaVolumeWorkRequest)
        }
        Log.d("Timer_Singleton","Timer scheduled : ${processedMins} minutes")
    }

    private fun updateTimerNotification(timerNotificationService: TimerNotificationService) {
        Log.d("Timer_Singleton","update called minutes $liveTimer")
        timerNotificationService.showNotification(liveTimer)
    }

    private fun toggleTimerNotification(timerNotificationService: TimerNotificationService) {
        _isNotificationRunning.value = !_isNotificationRunning.value
        if (isNotificationRunning.value)
            updateTimerNotification(timerNotificationService)
        else
            timerNotificationService.stopNotification()
    }

    fun extendSleepTimer() {
        _liveTimer.value += 5
        if (isNotificationRunning.value) {
            application.timerNotificationService.showNotification(liveTimer)
        }
        application.applicationWorkManager.cancelAllWorkByTag("sleep_timer_work")
        val updateWorkRequest = OneTimeWorkRequestBuilder<SleepTimerWorker>()
            .setInitialDelay(Duration.ofMinutes(TimerSingleton.liveTimer.toLong()))
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = Duration.ofSeconds(10)
            )
            .addTag("sleep_timer_work")
            .build()
        application.applicationWorkManager.enqueueUniqueWork("sleep_timer_work",ExistingWorkPolicy.REPLACE,updateWorkRequest)
    }

    fun stopTimer() {
        Log.d("Timer_Singleton","StopTimer called : ${liveTimer} minutes")
        runningTask.cancel()
        application.applicationWorkManager.cancelAllWorkByTag("sleep_timer_work")
        application.applicationWorkManager.cancelAllWorkByTag("media_volume_work")
        if (isNotificationRunning.value) {
            toggleTimerNotification(application.timerNotificationService)
        }
    }

    internal fun saveState() {
        val targetTimestamp = System.currentTimeMillis() + (liveTimer*60000)
        AppCoroutineScope.launch {
            Log.d("HomeScreenViewModel","timersingleton savestate called ${isTimerRunning.value} ${liveTimer}")
            dataStore.edit { prefs ->
                prefs[TimerPrefs.END_TIMESTAMP] = targetTimestamp
                prefs[TimerPrefs.REMAINING_MILLIS] = liveTimer.toLong()
                prefs[TimerPrefs.IS_RUNNING] = isTimerRunning.value
            }
        }
    }

    fun toggleTimer() {
        _isTimerRunning.value = !_isTimerRunning.value
    }

    fun setTimerState(value: Boolean) {
        _isTimerRunning.value = value
    }

    fun setLiveTimer(valueMins: Int) {
        _liveTimer.value = valueMins
    }
}

object AppCoroutineScope : CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun cancelAll() = job.cancel()
}
