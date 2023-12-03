package com.example.plantcare

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils.split
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.plantcare.databinding.ActivityPlantInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantInfoBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database
        userRef = firebaseDatabase.reference.child(getString(R.string.firebase_users_key)).child(firebaseAuth.currentUser?.uid!!)

        position = intent.getIntExtra(getString(R.string.position_key), 0)
        getPlantInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.plantinfo_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                val intent = Intent(this, AddPlantActivity::class.java)
                intent.putExtra(this.getString(R.string.position_key), position)
                intent.putExtra(this.getString(R.string.plant_page_type), AddPlantActivity.PLANT_VIEW )
                this.startActivity(intent)
                finish()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getPlantInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            userRef.child("plants").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (position != -1 && position >= 0 && position < snapshot.children.toList().size) {
                        val plantEntry = snapshot.children.toList()[position].getValue(Plant::class.java)
                        if(plantEntry != null){
                            supportActionBar?.title = plantEntry.plantName;
                            val specId = plantEntry.plantSpeciesId
                            var infoText = "Plant species: ${split(
                            plantEntry.plantSpecies,
                            " "
                            ).joinToString(" ") { it ->
                                String
                                it.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.ROOT
                                    ) else it.toString()
                                }
                            }
                        }"
                            if(plantEntry.adoptionDate != null){
                                val calendarTimeMillis = plantEntry.adoptionDate!!
                                val dateFormat = SimpleDateFormat(getString(R.string.dd_mmm_yyyy), Locale.getDefault())
                                val formattedDate = dateFormat.format(Date(calendarTimeMillis))

                                infoText += "\nDate of birth: $formattedDate"
                            }
                            binding.infoTextView.text = infoText
                            if (specId != null && specId != "") {
                                lifecycleScope.launch {
                                    var defaultImg = Helpers.getDefaultImg(specId)
                                    if (defaultImg != "")
                                        Picasso.get().load(defaultImg).into(binding.plantImageView);
                                    val sections = Helpers.getCareGuide(specId)
                                    for (t in 0 until sections.length()) {
                                        var section = sections.getJSONObject(t)
                                        var type = section.getString("type")
                                        when (type) {
                                            "watering" -> binding.wateringTextView.text = "Watering:\n" + section.getString("description") + "\n\nBased on pot size, we recommend watering ${plantEntry.plantName} every ${plantEntry.wateringFreq} days."
                                            "sunlight" -> binding.sunlightTextView.text = "Sunlight:\n" + section.getString("description")
                                            "pruning" -> binding.pruningTextView.text = "Pruning:\n" + section.getString("description")
                                        }
                                    }
                                }
                            }
                            if(plantEntry.imageName != null){
                                setImage(plantEntry.imageName!!)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w(getString(R.string.tag), getString(R.string.failed_to_read_value), error.toException())
                }
            })
        }
    }

    private fun setImage(imageName: String) {
        val firebaseStorageRef = Firebase.storage.reference.child(imageName!!)
        val externalFilesDir = getExternalFilesDir(null)
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
                        this,
                        getString(R.string.com_example_plantcare),
                        tempImgFile
                    )
                    binding.plantImageView!!.setImageURI(tempImgUri)
                }.addOnFailureListener { exception ->
                    // Errors that occurred during the download
                    Log.e(javaClass.simpleName,
                        getString(R.string.error_downloading_image, exception.message), exception)
                }
            } else {
                // If the file already exists, use it directly
                var tempImgUri = FileProvider.getUriForFile(
                    this,
                    getString(R.string.com_example_plantcare),
                    tempImgFile
                )
                binding.plantImageView!!.setImageURI(tempImgUri)
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.oops_missing_external_file_directory), Toast.LENGTH_SHORT
            ).show()
        }
    }
}