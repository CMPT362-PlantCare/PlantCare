package com.example.plantcare

import LogoutDialogFragment
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.plantcare.databinding.ActivityDashboardBinding
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
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlantInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantInfoBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gridItemAdapter: GridItemAdapter
    private lateinit var plantEntryList: ArrayList<Plant>
    private lateinit var userEmail: String
    private lateinit var gridView: GridView
    private lateinit var addButton: Button
    private lateinit var scheduleButton: Button

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
        userRef = firebaseDatabase.reference.child("Users").child(firebaseAuth.currentUser?.uid!!)

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
                            if (plantEntry.imageUri != null && plantEntry.imageUri != ""){
                                binding.plantImageView.setImageURI(plantEntry.imageUri?.toUri())
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
        }
    }


}