package com.example.plantcare

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityScheduleBinding

class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private lateinit var date : String
    private lateinit var calendar : Calendar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        calendar = Calendar.getInstance()
        getInitialDate()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            date = (month+1).toString() + "/" + dayOfMonth.toString() + "/" + year.toString()
            Toast.makeText(this, date, Toast.LENGTH_SHORT).show()
            calendarClicked()
        }

        binding.remindersBtn.setOnClickListener {
            val intent = Intent(this, CalenderActivity::class.java)
            startActivity(intent)
        }
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

}
