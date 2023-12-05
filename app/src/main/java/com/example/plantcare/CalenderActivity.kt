package com.example.plantcare

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityCalenderBinding
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
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
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

}
