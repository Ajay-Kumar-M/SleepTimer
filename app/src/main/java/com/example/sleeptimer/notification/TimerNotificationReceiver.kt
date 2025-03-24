package com.example.sleeptimer.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sleeptimer.viewModel.HomeScreenViewModel

class TimerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val service = TimerNotificationService(context)
        when(intent.getStringExtra("operation_type")) {
            "STOP" -> Log.d("MainTimerView","STOP notification called : ${intent.getStringExtra("operation_type")} minutes")
            "INCREMENT" -> Log.d("MainTimerView","increment timer called : ${intent.getStringExtra("operation_type")} minutes")
        }
        service.showNotification(10)
    }
}