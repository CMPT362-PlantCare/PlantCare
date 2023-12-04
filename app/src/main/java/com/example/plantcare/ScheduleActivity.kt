package com.example.plantcare

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.plantcare.databinding.ActivityScheduleBinding
import com.example.plantcare.databinding.CalendarDayLayoutBinding
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
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private lateinit var navigationView: BottomNavigationView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private var plantEntryList = mutableListOf<Plant>()
    private val wateringSchedule = mutableMapOf<LocalDate, MutableList<Plant>>()
    private lateinit var wateringEventsListAdapter: WateringEventsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        // for bottom navigation
        navigationView = binding.bottomNavigation

        wateringEventsListAdapter = WateringEventsListAdapter(this, mutableListOf(), today) {
            CoroutineScope(Dispatchers.IO).launch {
                updatePlantWateringHistory(it)
            }
        }
        binding.eventListView.adapter = wateringEventsListAdapter

        /* Bottom Navigator */
        bottomNavigation()

        setUpCalendar()

        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database
        userRef = firebaseDatabase.reference.child("Users").child(firebaseAuth.currentUser?.uid!!)
        loadPlants()

        if (savedInstanceState == null) {
            // Show today's events initially.
            binding.calendarView.post { selectDate(today) }
        }
    }

    private fun updatePlantWateringHistory(updatedPlant: Plant) {
        val firebaseKey = updatedPlant.firebaseKey ?: return
        val plantRef = userRef.child("plants").child(firebaseKey!!)
        plantRef.child("wateringHistory").setValue(updatedPlant.wateringHistory)
            .addOnSuccessListener {
                Log.d("Update", "Plant watering history updated successfully")
            }.addOnFailureListener {
                Log.d("Update", "Failed to update plant watering history")
            }
        val index = plantEntryList.indexOfFirst { it.firebaseKey == updatedPlant.firebaseKey }
        if (index != -1) {
            plantEntryList[index] = updatedPlant
            val plantsToWater = wateringSchedule[selectedDate]
            if (plantsToWater != null) {
                Log.d("WateringEventsListAdapter", "Plants to water: ${plantsToWater?.map { it.plantName }}")
            }
            Log.d("Update", "Selected date: $selectedDate")
            if (plantsToWater != null) {
                wateringEventsListAdapter.replace(plantsToWater, selectedDate!!)
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { binding.calendarView.notifyDateChanged(it) }
            binding.calendarView.notifyDateChanged(date)
        }
    }

    private fun loadPlants() {
        CoroutineScope(Dispatchers.IO).launch {
            userRef.child("plants").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("WateringEventsListAdapter", "loadPlants:onDataChange")
                    val newPlantEntryList = mutableListOf<Plant>()
                    snapshot.children.forEach { plantSnapshot ->
                        val plant = plantSnapshot.getValue(Plant::class.java)
                        plant?.firebaseKey = plantSnapshot.key
                        plant?.let { newPlantEntryList.add(it) }
                    }
                    plantEntryList.clear() // Clear the existing list
                    plantEntryList.addAll(newPlantEntryList)
                    updateWateringSchedule(plantEntryList)
                    if(wateringSchedule.containsKey(today)) {
                        selectDate(today)
                        val plantsToWater = wateringSchedule[today]
                        Log.d("WateringEventsListAdapter", "Plants to water: ${plantsToWater?.map { it.plantName }}")
                        plantsToWater?.let {
                            updateEventList(it, today)
                        }
                    }
                    runOnUiThread {
                        // Update the calendar view
                        binding.calendarView.notifyCalendarChanged()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
        }
    }

    private fun updateEventList(it: MutableList<Plant>, today: LocalDate?) {
        wateringEventsListAdapter.replace(it, today!!)
        wateringEventsListAdapter.notifyDataSetChanged()
    }

    private fun updateWateringSchedule(plantList: List<Plant>) {
        wateringSchedule.clear()
        for (plant in plantList) {
            val dates = getWateringDates(plant)
            for (date in dates) {
                wateringSchedule.getOrPut(date) { mutableListOf() }.add(plant)
            }
        }
    }

    fun getWateringDates(plant: Plant, monthsAhead: Long = 5): List<LocalDate> {
        val wateringDates = mutableListOf<LocalDate>()
        // Convert the timestamp to LocalDate
        val start = Instant.ofEpochMilli(plant.adoptionDate ?: return emptyList())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val end = start.plusMonths(monthsAhead)

        var nextWateringDate = start
        while (nextWateringDate.isBefore(end) and (plant.wateringFreq != 0)) {
            wateringDates.add(nextWateringDate)
            nextWateringDate = nextWateringDate.plusDays(plant.wateringFreq.toLong())
        }

        return wateringDates
    }

    private fun setUpCalendar() {

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(1)
        val endMonth = currentMonth.plusMonths(3)
        val daysOfWeek = daysOfWeek()
        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
            val dotView: View = CalendarDayLayoutBinding.bind(view).greenDot
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    if(day.position == DayPosition.MonthDate && wateringSchedule.containsKey(day.date)) {
                        selectDate(day.date)
                        val plantsToWater = wateringSchedule[day.date]
                        Log.d("WateringEventsListAdapter", "Plants to water: ${plantsToWater?.map { it.plantName }}")
                        plantsToWater?.let {
                            updateEventList(it, day.date)
                        }
                    }
                    else if (day.position == DayPosition.MonthDate) {
                        selectDate(day.date)
                        updateEventList(mutableListOf(), day.date)

                    }
                }
            }
            private fun updateEventList(it: MutableList<Plant>, date: LocalDate) {
                wateringEventsListAdapter.replace(it, date)
                wateringEventsListAdapter.notifyDataSetChanged()
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val titlesContainer = view as ViewGroup
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()
                container.dotView.visibility = if (wateringSchedule.containsKey(data.date) and (data.position == DayPosition.MonthDate)) View.VISIBLE else View.GONE

                if (data.position == DayPosition.MonthDate) {
                    when (data.date) {
                        today -> {
                            // Highlight today's date with a specific background
                            container.textView.setBackgroundResource(R.drawable.background_today)
                            container.textView.setTextColor(Color.WHITE)
                        }
                        selectedDate -> {
                            // Highlight selected date with a different background
                            container.textView.setBackgroundResource(R.drawable.background_selected_date)
                            container.textView.setTextColor(Color.BLACK)
                        }
                        else -> {
                            // Default styling for other dates
                            container.textView.setBackgroundColor(Color.TRANSPARENT)
                            container.textView.setTextColor(Color.BLACK)
                        }
                    }
                } else {
                    container.textView.setTextColor(Color.GRAY)
                }
            }
        }

        binding.calendarView.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.yearMonth
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            textView.text = title
                        }
                }
            }
        }

        binding.calendarView.monthScrollListener = { month ->
            supportActionBar?.title = month.yearMonth.month.name.toLowerCase().capitalize() + " " + month.yearMonth.year
        }
    }

    override fun onResume() {
        super.onResume()
        navigationView.menu.getItem(2).isChecked = true;
        loadPlants()
        binding.calendarView.notifyCalendarChanged()
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
