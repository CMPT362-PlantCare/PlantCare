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
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityDashboardBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
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

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gridItemAdapter: GridItemAdapter
    private lateinit var plantEntryList: ArrayList<Plant>
    private lateinit var gridView: GridView
    private lateinit var navigationView: BottomNavigationView

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.dashboard_toolbar))

        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database

        userRef = firebaseDatabase.reference.child(getString(R.string.firebase_users_key)).child(firebaseAuth.currentUser?.uid!!)

        val userEmail = firebaseAuth.currentUser!!.email
        val userName = userEmail!!.substringBefore('@')

        binding.greetingTextView.text = getString(R.string.greeting_message, userName)

        gridView = binding.gridView
        navigationView = binding.bottomNavigation


        /* User's added Plants in gridView*/
        setUpGridItemAdapter()


        loadPlants()

        /* Bottom Navigator */
        bottomNavigation()

        /* Daily watering notification*/
        wateringNotification()

    }

    override fun onResume() {
        super.onResume()
        navigationView.menu.getItem(0).isChecked = true;
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
            userRef.child(getString(R.string.plants_firebase_key)).addValueEventListener(object : ValueEventListener {
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
                    Log.w(getString(R.string.tag), getString(R.string.failed_to_read_value), error.toException())
                }
            })
        }
    }

    private fun wateringNotification(){
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val myIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_MUTABLE)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            Calendar.getInstance().timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

    }

    private fun bottomNavigation(){
        navigationView.selectedItemId = R.id.dashboard_home
        navigationView.setOnNavigationItemSelectedListener{ item ->
                when (item.itemId) {
                    R.id.add_plant -> {
                        val intent = Intent(this, AddPlantActivity::class.java)
                        intent.putExtra(getString(R.string.plant_page_type), AddPlantActivity.PLANT_ADD)
                        startActivity(intent)

                        return@setOnNavigationItemSelectedListener true
                    }
                    R.id.calender -> {
                        val intent = Intent(this, ScheduleActivity::class.java)
                        startActivity(intent)

                        return@setOnNavigationItemSelectedListener true
                    }
                    R.id.reminder -> {
                        val intent = Intent(this, CalenderActivity::class.java)
                        startActivity(intent)

                        return@setOnNavigationItemSelectedListener true
                    }
                    else -> false
                }
        }
    }
}