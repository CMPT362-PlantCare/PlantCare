package com.example.plantcare

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userEmail: String
    private lateinit var gridView: GridView
    private lateinit var addButton: Button
    private lateinit var reminderButton: Button

    private val DAILY_WATER_REMINDER_HOUR = 13

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.dashboard_toolbar))

        firebaseAuth = Firebase.auth

        userEmail = intent.getStringExtra(getString(R.string.user_email_intent_tag))!!.substringBefore('@')

        binding.greetingTextView.text = getString(R.string.greeting_message, userEmail)

        gridView =  binding.gridView
        addButton = binding.addButton
        reminderButton = binding.reminderButton

        /* User's added Plants in gridView*/

        /* SAMPLE ARRAYS */
        val imageSet = arrayOf(R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green,
            R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green)
        val textSet = arrayOf("flower #1", "flower #2", "flower #3", "flower #4",
            "flower #5", "flower #6", "flower #7", "flower #8", "flower #9")
        /* SAMPLE ARRAYS */

        var gridItemAdapter = GridItemAdapter(this, imageSet, textSet)
        gridView.adapter = gridItemAdapter
        gridView.setOnItemClickListener { adapterView, parent, position, l ->
            Toast.makeText(this, "Click on : ${textSet[position]}", Toast.LENGTH_SHORT).show()
        }

        /* Buttons on page */
        addButton.setOnClickListener(){
            val addPlantActivityIntent =
                Intent(this, AddPlantActivity::class.java)
            startActivity(addPlantActivityIntent)
        }

        reminderButton.setOnClickListener(){
            val reminderActivityIntent =
                Intent(this, CalenderActivity::class.java)
            startActivity(reminderActivityIntent)
        }

        /* Daily watering notification*/
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val myIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_MUTABLE)

        val calendar = Calendar.getInstance().apply {
            if (get(Calendar.HOUR_OF_DAY) >= DAILY_WATER_REMINDER_HOUR) {
                add(Calendar.DAY_OF_MONTH, 1)
            }

            set(Calendar.HOUR_OF_DAY, DAILY_WATER_REMINDER_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }


        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                firebaseAuth.signOut()
                val loginActivityIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginActivityIntent)
                finish()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}