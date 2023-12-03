package com.example.plantcare

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import com.example.plantcare.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = Firebase.auth
        firebaseDatabase = Firebase.database

        handleBackgroundImageChanges()
        handleUserAlreadyLoggedIn()
        handleSignup()
        handleSignupPageRoute()
    }

    private fun handleBackgroundImageChanges() {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.signupLayout.setBackgroundResource(R.drawable.signup_bg_landscape)
        } else {
            binding.signupLayout.setBackgroundResource(R.drawable.signup_bg)
        }
    }

    private fun handleUserAlreadyLoggedIn() {
        if (firebaseAuth.currentUser != null) {
            firebaseAuth.currentUser.let { user ->
                if (user != null) {
                    val dashboardActivityIntent = Intent(this, DashboardActivity::class.java)
                    startActivity(dashboardActivityIntent)
                    finish()
                }
            }
        }
    }

    private fun handleSignup() {
        binding.signUpButton.setOnClickListener {
            val userEmail = binding.emailEditText.text.toString()
            val userPassword = binding.enterPasswordEditText.text.toString()
            val userPasswordConfirm = binding.confirmPasswordEditText.text.toString()

            if (userEmail.isNotEmpty() && userPassword.isNotEmpty() && userPasswordConfirm.isNotEmpty()) {
                if (userPassword == userPasswordConfirm) {
                    firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Get the UID of the newly created user
                                val user = firebaseAuth.currentUser
                                val uid = user?.uid
                                val userEmail = user?.email

                                // Create a Users object with UID and any additional user data
                                val userObject = User()
                                userObject.uid = uid
                                userObject.email = userEmail

                                // Save the Users object to the "Users" node in the database
                                val usersReference = firebaseDatabase.reference.child("Users")
                                if (uid != null) {
                                    usersReference.child(uid).setValue(userObject)
                                }

                                val loginActivityIntent = Intent(this, LoginActivity::class.java)
                                startActivity(loginActivityIntent)
                                finish()
                            } else {
                                when (task.exception) {
                                    is FirebaseAuthInvalidCredentialsException -> {
                                        // Invalid Email Format
                                        Toast.makeText(
                                            this,
                                            getString(R.string.please_enter_a_valid_email),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else -> {
                                        // Other exceptions
                                        Toast.makeText(
                                            this,
                                            getString(R.string.signup_failed_please_try_again_later),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this,
                        getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignupPageRoute() {
        binding.loginLink.setOnClickListener {
            val loginActivityIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginActivityIntent)
            finish()
        }
    }
}