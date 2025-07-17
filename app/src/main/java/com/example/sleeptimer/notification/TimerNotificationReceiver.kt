package com.example.sleeptimer.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sleeptimer.model.TimerSingleton

class TimerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.getStringExtra("operation_type")) {
            "STOP" -> {
                Log.d("MainTimerView","STOP notification called : ${intent.getStringExtra("operation_type")} minutes")
                TimerSingleton.toggleTimer()
                TimerSingleton.stopTimer()
                TimerSingleton.saveState()
            }
            "INCREMENT" -> {
                Log.d("MainTimerView","increment timer called : ${intent.getStringExtra("operation_type")} minutes")
                TimerSingleton.extendSleepTimer()
                TimerSingleton.saveState()
            }
        }

    }
}