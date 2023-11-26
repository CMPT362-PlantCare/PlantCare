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
import androidx.lifecycle.ViewModelProvider
import com.example.plantcare.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gridItemAdapter: GridItemAdapter
    private lateinit var plantEntryList: ArrayList<PlantEntry>
    private lateinit var userEmail: String
    private lateinit var gridView: GridView
    private lateinit var addButton: Button
    private lateinit var reminderButton: Button

    private lateinit var plantEntryDatabase: PlantEntryDatabase
    private lateinit var plantEntryDatabaseDao: PlantEntryDatabaseDao
    private lateinit var plantEntryRepository: PlantEntryRepository
    private lateinit var plantEntryViewModelFactory: PlantEntryViewModelFactory
    private lateinit var plantEntryViewModel: PlantEntryViewModel

    private val DAILY_WATER_REMINDER_HOUR = 13

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.dashboard_toolbar))

        firebaseAuth = Firebase.auth

        userEmail = intent.getStringExtra(getString(R.string.user_email_intent_tag))!!.substringBefore('@')

        binding.greetingTextView.text = getString(R.string.greeting_message, userEmail)

        setUpPlantEntryDatabase()

        gridView =  binding.gridView
        addButton = binding.addButton
        reminderButton = binding.reminderButton


        /* User's added Plants in gridView*/
        setUpGridItemAdapter()


        loadPlantEntryData()

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
        wateringNotification()

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

    private fun setUpPlantEntryDatabase() {
        plantEntryDatabase = PlantEntryDatabase.getInstance(this)
        plantEntryDatabaseDao = plantEntryDatabase.plantEntryDatabaseDao
        plantEntryRepository = PlantEntryRepository(plantEntryDatabaseDao)
        plantEntryViewModelFactory = PlantEntryViewModelFactory(plantEntryRepository)
        plantEntryViewModel = ViewModelProvider(this, plantEntryViewModelFactory)[PlantEntryViewModel::class.java]
    }

    private fun setUpGridItemAdapter(){
        plantEntryList = ArrayList()

        gridItemAdapter = GridItemAdapter(this, plantEntryList)
        gridView.adapter = gridItemAdapter
    }

    private fun loadPlantEntryData() {
        plantEntryViewModel.allPlantEntriesLiveData.observe(this) { updatedList ->
            gridItemAdapter.replace(updatedList)
            gridItemAdapter.notifyDataSetChanged()
        }
    }

    private fun wateringNotification(){
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
}