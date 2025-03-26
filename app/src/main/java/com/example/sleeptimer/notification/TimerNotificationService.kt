package com.example.sleeptimer.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sleeptimer.MainActivity
import com.example.sleeptimer.R

class TimerNotificationService(private  val context: Context) {

    companion object {
        const val TIMER_CHANNEL_ID = "Timer_Channel_ID"
        const val TIMER_NOTIFICATION_ID = 1
    }
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(timer: Int) {
        val activityIntent = Intent(context,MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE } else 0
        )
        val incrementTimer = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, TimerNotificationReceiver::class.java).apply {
                putExtra("operation_type","INCREMENT")
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val stopTimer = PendingIntent.getBroadcast(
            context,
            3,
            Intent(context, TimerNotificationReceiver::class.java).apply {
                putExtra("operation_type","STOP")
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setContentTitle("Media Sleep Timer")
            .setContentText("$timer minutes left until end.")
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setContentIntent(activityPendingIntent)
            .addAction(R.drawable.baseline_timer_24,"Extend",incrementTimer)
            .addAction(R.drawable.baseline_timer_24,"Stop",stopTimer)
            .setSilent(true)
            .build()

        notificationManager.notify(TIMER_NOTIFICATION_ID,notification)
    }

    fun stopNotification() {
        notificationManager.cancel(TIMER_NOTIFICATION_ID)
    }
}

/*
//            .apply {
//            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//        }
//        val stackBuilder = TaskStackBuilder.create(context).apply {
//            addParentStack(MainActivity::class.java)
//            addNextIntent(activityIntent)
//        }
//        val activityPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
 */