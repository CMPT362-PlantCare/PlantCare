package com.example.plantcare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver: BroadcastReceiver(){

    private val NOTIFY_ID = 100
    private val CHANNEL_ID = "water reminder notification channel"

    override fun onReceive(context: Context, intent: Intent) {
        // to do: check the database for the days left to water == 0
        sendNotification(context)

    }

    private fun sendNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "channel name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
        notificationBuilder.setContentTitle("PlantCare")
        notificationBuilder.setContentText("It's time to water your plants! Don't forget to give them some care today.")
        notificationBuilder.setSmallIcon(R.drawable.flower_icon_white)
        notificationBuilder.setAutoCancel(true)
        val notification = notificationBuilder.build()

        notificationManager.notify(NOTIFY_ID, notification)
    }

}


