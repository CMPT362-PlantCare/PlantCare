package com.example.plantcare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val NOTIFY_ID = 100
private const val CHANNEL_ID = "water reminder notification channel"
class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        sendNotification(context)
    }

    private fun sendNotification(context: Context) {
        val notificationIntent = Intent(context, CalenderActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
        notificationBuilder.setContentTitle("PlantCare")
        notificationBuilder.setContentText("It's time to water your plants! Don't forget to give them some care today.")
        notificationBuilder.setSmallIcon(R.drawable.flower_icon_white)
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setAutoCancel(true)
        val notification = notificationBuilder.build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(NOTIFY_ID, notification)
    }
}


