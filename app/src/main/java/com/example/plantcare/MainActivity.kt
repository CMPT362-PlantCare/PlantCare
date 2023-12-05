package com.example.plantcare

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivitySignupBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    //Variables
    private lateinit var rellay1: RelativeLayout
    private lateinit var dlg2: AlertDialog
    private lateinit var topAnim: Animation
    private lateinit var bottomAnim: Animation
    private lateinit var txtview: TextView
    private lateinit var logoImgview: ImageView
    private lateinit var btnSignup: MaterialButton
    private lateinit var btnLogin: MaterialButton
    private var handler = Handler()
    private var runnable = Runnable { rellay1!!.visibility = View.VISIBLE }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) //to hide status bar
        setContentView(R.layout.activity_main)
        rellay1 = findViewById(R.id.rellay1)

        //Animations
        topAnim = AnimationUtils.loadAnimation(
            this,
            R.anim.top_animation
        ) //we are using top_animation in this context
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        //hooks
        logoImgview = findViewById(R.id.logoImgview)
        txtview = findViewById(R.id.txtview)
        btnSignup = findViewById(R.id.btnSignup)
        btnLogin = findViewById(R.id.btnLogin)
        logoImgview.setAnimation(topAnim)
        txtview.setAnimation(bottomAnim)
        handler.postDelayed(runnable,1000)
        val radioLsnr = View.OnClickListener { v ->
            val slktdbtn = findViewById<View>(v.id) as MaterialButton
            if (slktdbtn.text == "SignUp") {
                val intent = Intent(this@MainActivity, SignupActivity::class.java)
                startActivity(intent)
            }
            if (slktdbtn.text == "Login") {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            }

        btnSignup.setOnClickListener(radioLsnr)
        btnLogin.setOnClickListener(radioLsnr)

    }

}
