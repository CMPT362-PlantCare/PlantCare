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

class AlarmReceiver: BroadcastReceiver() {

    private val NOTIFY_ID = 100
    private val CHANNEL_ID = "water reminder notification channel"
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userRef: DatabaseReference


    override fun onReceive(context: Context, intent: Intent) {

        /*firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database

        userRef = firebaseDatabase.reference.child("Users").child(firebaseAuth.currentUser?.uid!!)

        CoroutineScope(Dispatchers.IO).launch {
            userRef.child("plants").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val plantEntryList = mutableListOf<Plant>()

                    for (plantSnapshot in snapshot.children) {
                        val plantEntry = plantSnapshot.getValue(Plant::class.java)
                        if (plantEntry != null) {
                            plantEntryList.add(plantEntry)
                        }
                    }

                    for (plant in plantEntryList) {
                        if (daysBetweenDates(plant.lastWateredDate!!) >= plant.wateringFreq) {
                            sendNotification(context)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
        }*/
        sendNotification(context)

    }

    private fun sendNotification(context: Context) {
        val notificationIntent = Intent(context, ScheduleActivity::class.java)
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

    private fun daysBetweenDates(lastWatered: Long): Int {
        val currentTime = System.currentTimeMillis()
        var days = 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val differenceInMillis = currentTime - lastWatered
            days = TimeUnit.MILLISECONDS.toDays(differenceInMillis).toInt()
        }
        return days
    }
}


