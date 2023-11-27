package com.example.plantcare

import LogoutDialogFragment
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.GridView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityDashboardBinding
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

private const val PLANT_ADD = 0
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gridItemAdapter: GridItemAdapter
    private lateinit var plantEntryList: ArrayList<Plant>
    private lateinit var userEmail: String
    private lateinit var gridView: GridView
    private lateinit var addButton: Button
    private lateinit var reminderButton: Button

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.dashboard_toolbar))

        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database
        userRef = firebaseDatabase.reference.child("Users").child(firebaseAuth.currentUser?.uid!!)

        userEmail = intent.getStringExtra(getString(R.string.user_email_intent_tag))!!.substringBefore('@')

        binding.greetingTextView.text = getString(R.string.greeting_message, userEmail)

        gridView =  binding.gridView
        addButton = binding.addButton
        reminderButton = binding.reminderButton

        setUpGridItemAdapter()

        loadPlants()

        addButton.setOnClickListener(){
            val intent = Intent(this, AddPlantActivity::class.java)
            intent.putExtra(getString(R.string.plant_page_type), PLANT_ADD )
            startActivity(intent)
        }

        reminderButton.setOnClickListener(){
            val reminderActivityIntent =
                Intent(this, CalenderActivity::class.java)
            startActivity(reminderActivityIntent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                val logoutDialogFragment = LogoutDialogFragment()
                logoutDialogFragment.show(supportFragmentManager, "LogoutDialogFragment")
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpGridItemAdapter(){
        plantEntryList = ArrayList()

        gridItemAdapter = GridItemAdapter(this, plantEntryList)
        gridView.adapter = gridItemAdapter
    }

    private fun loadPlants() {
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

                    gridItemAdapter.replace(plantEntryList)
                    gridItemAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
        }
    }

//    private fun showLogoutConfirmationDialog() {
//        val alertDialogBuilder = AlertDialog.Builder(this)
//        alertDialogBuilder.setTitle(getString(R.string.logout_confirmation_title))
//        alertDialogBuilder.setMessage(getString(R.string.logout_confirmation_message))
//
//        alertDialogBuilder.setPositiveButton(getString(R.string.yes)) { _, _ ->
//            firebaseAuth.signOut()
//            val loginActivityIntent = Intent(this, LoginActivity::class.java)
//            startActivity(loginActivityIntent)
//            finish()
//        }
//
//        alertDialogBuilder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
//            dialog.dismiss()
//        }
//
//        val alertDialog = alertDialogBuilder.create()
//        alertDialog.show()
//    }
}