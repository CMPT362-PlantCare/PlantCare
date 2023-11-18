package com.example.plantcare

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        binding.loginButton.setOnClickListener {
            val userEmail = binding.emailEditText.text.toString()
            val userPassword = binding.enterPasswordEditText.text.toString()

            if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val dashboardActivityIntent =
                                Intent(this, DashboardActivity::class.java)
                            dashboardActivityIntent.putExtra(
                                getString(R.string.user_email_intent_tag),
                                userEmail
                            )
                            startActivity(dashboardActivityIntent)
                            finish()

                        } else {
                            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please Fill All Fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupLink.setOnClickListener {
            val signupActivityIntent = Intent(this, SignupActivity::class.java)
            startActivity(signupActivityIntent)
            finish()
        }
    }
}