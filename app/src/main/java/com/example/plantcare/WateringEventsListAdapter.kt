package com.example.plantcare

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import java.time.LocalDate

class WateringEventsListAdapter(private val context: Context,
                                private var plantEntryList: List<Plant>,
                                private var selectedDate: LocalDate,
                                private val onWateringEventUpdated: (Plant) -> Unit) : BaseAdapter() {
    override fun getCount(): Int {
        return plantEntryList.size
    }

    override fun getItem(position: Int): Any {
        return plantEntryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.watering_event, parent, false)

        val plant = getItem(position) as Plant
        val imageView = view.findViewById<ImageView>(R.id.ivPlantPicSchedule)
        val nameTextView = view.findViewById<TextView>(R.id.tvPlantNameSchedule)
        val statusButton = view.findViewById<Button>(R.id.btnWateringSchedule)
        nameTextView.text = plant.plantName
        plant.imageUri?.let {
            if (it.isNotEmpty()) {
                val imageUri = Uri.parse(it)
                imageView.setImageURI(imageUri)
            } else {
                imageView.setImageResource(R.drawable.default_plant_profile_pic) // Set default image
            }
        } ?: imageView.setImageResource(R.drawable.default_plant_profile_pic) // Set default image if URI is null

        val wasWateredOnSelectedDate = selectedDate.toString() in plant.wateringHistory
        if (wasWateredOnSelectedDate) {
            statusButton.text = "Done"
            statusButton.isEnabled = false
            statusButton.setBackgroundColor(getColor(context, R.color.grey)) // Set grey background
        } else {
            statusButton.text = "Complete"
            statusButton.isEnabled = true
            statusButton.setBackgroundColor(getColor(context, R.color.button_green)) // Set green background
        }

        statusButton.setOnClickListener {
            // Add the selected date to the plant's watering history if not already present
            if (selectedDate.toString() !in plant.wateringHistory) {
                plant.wateringHistory = plant.wateringHistory + selectedDate.toString()
                onWateringEventUpdated(plant)
            }
            // Update button text
            statusButton.text = "Done"
            statusButton.isEnabled = false
            statusButton.setBackgroundColor(getColor(context, R.color.grey))
        }

        return view
    }

    fun replace(newPlantList: List<Plant>, date: LocalDate) {
        plantEntryList = newPlantList
        selectedDate = date
    }
}
