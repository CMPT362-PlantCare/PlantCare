package com.example.plantcare

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        if (firebaseAuth.currentUser != null) {
            firebaseAuth.currentUser.let { user ->
                if (user != null) {
                    val userEmail = user.email.toString()
                    val dashboardActivityIntent = Intent(this, DashboardActivity::class.java)
                    dashboardActivityIntent.putExtra(
                        getString(R.string.user_email_intent_tag),
                        userEmail
                    )
                    startActivity(dashboardActivityIntent)
                    finish()
                }
            }
        }

        binding.signUpButton.setOnClickListener {
            val userEmail = binding.emailEditText.text.toString()
            val userPassword = binding.enterPasswordEditText.text.toString()
            val userPasswordConfirm = binding.confirmPasswordEditText.text.toString()

            if (userEmail.isNotEmpty() && userPassword.isNotEmpty() && userPasswordConfirm.isNotEmpty()) {
                if (userPassword == userPasswordConfirm) {
                    firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val loginActivityIntent = Intent(this, LoginActivity::class.java)
                                startActivity(loginActivityIntent)
                                finish()
                            } else {
                                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords Do Not Match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please Out Fill All Fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginLink.setOnClickListener {
            val loginActivityIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginActivityIntent)
            finish()
        }
    }
}