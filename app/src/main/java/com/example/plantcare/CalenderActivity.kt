package com.example.plantcare


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityCalenderBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.button.MaterialButtonToggleGroup
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


class CalenderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalenderBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var calenderAdapter: CalenderAdapter
    private lateinit var plantEntryList: ArrayList<Plant>
    private lateinit var btnBack: MaterialButtonToggleGroup
    private lateinit var gridView: GridView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var navigationView: BottomNavigationView
    private lateinit var plantRef:DatabaseReference

    private lateinit var Agroup: RadioGroup
    private lateinit var todoo: RadioButton
    private lateinit var done:RadioButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var status = 0;
        binding = ActivityCalenderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.calender_toolbar))

        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database
        userRef = firebaseDatabase.reference.child("Users").child(firebaseAuth.currentUser?.uid!!)
        gridView = binding.gridView

        navigationView = binding.bottomNavigation
        btnBack = binding.btnBack

        btnBack.setOnClickListener {
            finish()
        }
        /*  Agroup = findViewById(R.id.Agroup)
          todoo = findViewById<RadioButton>(R.id.radTodoo)
          done = findViewById<RadioButton>(R.id.radDone)
          val radioLsnr = View.OnClickListener { v ->
              val slktd = findViewById<View>(v.id) as RadioButton
              if (slktd.text == "Todo") {
                  status = 0
                  fetchPlants()
              }
              if (slktd.text == "Done") {
                  status = 1
                  fetchPlants()
              }
          }
          todoo.setOnClickListener(radioLsnr)
          done.setOnClickListener(radioLsnr)*/

        setUpCalenderAdapter()
        fetchPlants()

        /* Bottom Navigator */
        bottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        navigationView.menu.getItem(3).isChecked = true;
    }

    private fun setUpCalenderAdapter() {
        plantEntryList = ArrayList()

        calenderAdapter = CalenderAdapter(this, plantEntryList)
        gridView.adapter = calenderAdapter
    }

    private fun fetchPlants() {
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

                    calenderAdapter.replace(plantEntryList)
                    calenderAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
        }

    }

    private fun bottomNavigation(){
        navigationView.selectedItemId = R.id.reminder
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
                R.id.calender -> {
                    val intent = Intent(this, ScheduleActivity::class.java)
                    startActivity(intent)

                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }

}
