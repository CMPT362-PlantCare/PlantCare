package com.example.plantcare


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.plantcare.databinding.ActivityCalenderBinding
import com.google.android.material.circularreveal.cardview.CircularRevealCardView
import com.google.android.material.textview.MaterialTextView
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

    private lateinit var gridView: GridView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalenderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.calender_toolbar))

        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database
        userRef = firebaseDatabase.reference.child("Users").child(firebaseAuth.currentUser?.uid!!)
        gridView = binding.gridView


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
