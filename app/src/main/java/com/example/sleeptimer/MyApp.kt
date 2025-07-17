package com.example.sleeptimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.WorkManager
import com.example.sleeptimer.model.TimerSingleton
import com.example.sleeptimer.notification.TimerNotificationService

class MyApp : Application() {

    lateinit var timerNotificationService: TimerNotificationService
        private set
    lateinit var applicationWorkManager: WorkManager
        private set

    override fun onCreate() {
        super.onCreate()
        TimerSingleton.initialize(this.applicationContext)
        createNotificationChannel()
        timerNotificationService = TimerNotificationService(this.applicationContext)
        applicationWorkManager = WorkManager.getInstance(this.applicationContext)
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel (
                TimerNotificationService.TIMER_CHANNEL_ID,
                "Timer Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Used to control on-going Sleep Timer"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}