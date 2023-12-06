package com.example.plantcare

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val ANIMATION_DELAY = 1000L
class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var rellay1: RelativeLayout
    private lateinit var topAnim: Animation
    private lateinit var bottomAnim: Animation
    private lateinit var txtview: TextView
    private lateinit var logoImgview: ImageView
    private lateinit var btnSignup: MaterialButton
    private lateinit var btnLogin: MaterialButton
    private var handler = Handler()
    private var runnable = Runnable { rellay1.visibility = View.VISIBLE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = Firebase.auth

        handleUserAlreadyLoggedIn()
        initializeViews()
        setupAnimations()
        setupButtonClickListeners()
    }

    private fun initializeViews() {
        rellay1 = findViewById(R.id.rellay1)
        logoImgview = findViewById(R.id.logoImgview)
        txtview = findViewById(R.id.txtview)
        btnSignup = findViewById(R.id.btnSignup)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun setupAnimations() {
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
        logoImgview.animation = topAnim
        txtview.animation = bottomAnim
        handler.postDelayed(runnable, ANIMATION_DELAY)
    }

    private fun setupButtonClickListeners() {
        btnSignup.setOnClickListener{
            startActivity(SignupActivity::class.java)
        }
        btnLogin.setOnClickListener{
            startActivity(LoginActivity::class.java)
        }
    }

    private fun handleUserAlreadyLoggedIn() {
        if (firebaseAuth.currentUser != null) {
            startActivity(DashboardActivity::class.java)
            finish()
        }
    }

    private fun <T : Any> startActivity(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }
}