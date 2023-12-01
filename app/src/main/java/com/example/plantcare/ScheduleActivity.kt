package com.example.plantcare

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityScheduleBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private lateinit var date : String
    private lateinit var calendar : Calendar
    private lateinit var eventListAdapter: ArrayAdapter<WateringEventEntry>
    private lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        navigationView = binding.bottomNavigation

        calendar = Calendar.getInstance()
        getInitialDate()

        eventListAdapter = ArrayAdapter(this, R.layout.watering_event, mutableListOf())
//        binding.eventList.adapter = eventListAdapter

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            date = (month+1).toString() + "/" + dayOfMonth.toString() + "/" + year.toString()
            Toast.makeText(this, date, Toast.LENGTH_SHORT).show()
            calendarClicked()
        }

        /* Bottom Navigator */
        bottomNavigation()

    }

    private fun calendarClicked() {

    }

    private fun getInitialDate() {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        date = (month+1).toString() + "/" + day.toString() + "/" + year.toString()
        Toast.makeText(this, date, Toast.LENGTH_SHORT).show()
    }

    private fun bottomNavigation(){
        navigationView.selectedItemId = R.id.calender
        navigationView.setOnNavigationItemSelectedListener{ item ->
            when (item.itemId) {
                R.id.dashboard_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.add_plant -> {
                    val intent = Intent(this, AddPlantActivity::class.java)
                    intent.putExtra(getString(R.string.plant_page_type), AddPlantActivity.PLANT_ADD)
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
