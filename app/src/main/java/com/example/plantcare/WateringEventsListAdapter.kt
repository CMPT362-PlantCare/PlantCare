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
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.FileProvider
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import com.google.firebase.storage.ktx.storage

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

        setImage(plantEntryList[position].imageName!!, imageView)

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

    private fun setImage(imageName: String, view: ImageView) {
        val firebaseStorageRef = Firebase.storage.reference.child(imageName!!)
        val externalFilesDir = context.getExternalFilesDir(null)
        if (externalFilesDir != null) {
            var tempImgFile = File(externalFilesDir, imageName)
            // Check if the file exists
            if (!tempImgFile.exists()) {
                // If the file doesn't exist, proceed with the download
                firebaseStorageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                    // Successfully downloaded the byte array
                    try {
                        val stream = FileOutputStream(tempImgFile)
                        stream.write(bytes)
                        stream.flush()
                        stream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    var tempImgUri = FileProvider.getUriForFile(
                        context,
                        context.getString(R.string.com_example_plantcare),
                        tempImgFile
                    )
                    view!!.setImageURI(tempImgUri)
                }.addOnFailureListener { exception ->
                    // Errors that occurred during the download
                    Log.e(javaClass.simpleName,
                        context.getString(R.string.error_downloading_image, exception.message), exception)
                }
            } else {
                // If the file already exists, use it directly
                var tempImgUri = FileProvider.getUriForFile(
                    context,
                    context.getString(R.string.com_example_plantcare),
                    tempImgFile
                )
                view!!.setImageURI(tempImgUri)
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.oops_missing_external_file_directory), Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun replace(newPlantList: List<Plant>, date: LocalDate) {
        plantEntryList = newPlantList
        selectedDate = date
    }
}
