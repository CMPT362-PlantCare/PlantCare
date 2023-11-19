package com.example.plantcare

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userEmail: String
    private lateinit var gridView: GridView
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.dashboard_toolbar))

        firebaseAuth = Firebase.auth

        userEmail = intent.getStringExtra(getString(R.string.user_email_intent_tag))!!.substringBefore('@')

        binding.greetingTextView.text = getString(R.string.greeting_message, userEmail)

        gridView =  binding.gridView
        addButton = binding.addButton

        /* SAMPLE ARRAYS */
        val imageSet = arrayOf(R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green,
            R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green, R.drawable.flower_icon_green)
        val textSet = arrayOf("flower #1", "flower #2", "flower #3", "flower #4",
            "flower #5", "flower #6", "flower #7", "flower #8", "flower #9")
        /* SAMPLE ARRAYS */

        var gridItemAdapter = GridItemAdapter(this, imageSet, textSet)
        gridView.adapter = gridItemAdapter
        gridView.setOnItemClickListener { adapterView, parent, position, l ->
            Toast.makeText(this, "Click on : ${textSet[position]}", Toast.LENGTH_SHORT).show()
        }

        addButton.setOnClickListener(){
            val addPlantActivityIntent =
                Intent(this, AddPlantActivity::class.java)
            startActivity(addPlantActivityIntent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                firebaseAuth.signOut()
                val loginActivityIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginActivityIntent)
                finish()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}