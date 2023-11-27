package com.example.plantcare


import android.app.AlarmManager
import android.app.PendingIntent
import LogoutDialogFragment
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityDashboardBinding
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

private const val PLANT_ADD = 0
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gridItemAdapter: GridItemAdapter
    private lateinit var plantEntryList: ArrayList<Plant>
    private lateinit var userEmail: String
    private lateinit var gridView: GridView
    private lateinit var addButton: Button
    private lateinit var reminderButton: Button

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    private val DAILY_WATER_REMINDER_HOUR = 13

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.dashboard_toolbar))

        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database
        userRef = firebaseDatabase.reference.child("Users").child(firebaseAuth.currentUser?.uid!!)

        userEmail =
            intent.getStringExtra(getString(R.string.user_email_intent_tag))!!.substringBefore('@')

        binding.greetingTextView.text = getString(R.string.greeting_message, userEmail)

        gridView = binding.gridView
        addButton = binding.addButton
        reminderButton = binding.reminderButton


        /* User's added Plants in gridView*/
        setUpGridItemAdapter()


        loadPlants()

        /* Buttons on page */
        addButton.setOnClickListener() {
            val intent = Intent(this, AddPlantActivity::class.java)
            intent.putExtra(getString(R.string.plant_page_type), PLANT_ADD)
            startActivity(intent)

        }

        reminderButton.setOnClickListener() {
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
                val logoutDialogFragment = LogoutDialogFragment()
                logoutDialogFragment.show(supportFragmentManager,
                    getString(R.string.logout_dialog_fragment_tag))
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpGridItemAdapter() {
        plantEntryList = ArrayList()

        gridItemAdapter = GridItemAdapter(this, plantEntryList)
        gridView.adapter = gridItemAdapter
    }

    private fun loadPlants() {
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

                    gridItemAdapter.replace(plantEntryList)
                    gridItemAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
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

        /* for testing Purpose - Will delete at the end*/
        /*alarmManager.setExact(AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )*/
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

    }
}