package com.example.plantcare

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityAddplantBinding
import com.example.plantcare.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AddPlantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddplantBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userEmail: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("creating add plant activity")
        binding = ActivityAddplantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = Firebase.auth
        userEmail = intent.getStringExtra(getString(R.string.user_email_intent_tag))!!
    }

}