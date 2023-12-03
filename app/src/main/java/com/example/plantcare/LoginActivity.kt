package com.example.plantcare

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.plantcare.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
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

        handleLogin()
        handleSignupPageRoute()
    }

    private fun handleLogin() {
        binding.loginButton.setOnClickListener {
            val userEmail = binding.emailEditText.text.toString()
            val userPassword = binding.enterPasswordEditText.text.toString()

            if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val dashboardActivityIntent =
                                Intent(this, DashboardActivity::class.java)
                            startActivity(dashboardActivityIntent)
                            finish()
                        } else {
                            val errorMessage = task.exception?.message
                            when {
                                errorMessage?.contains(getString(R.string.email_address_is_badly_formatted)) == true -> {
                                    // Invalid Email Format
                                    Toast.makeText(
                                        this,
                                        getString(R.string.invalid_email_format_please_enter_a_valid_email_address),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                errorMessage?.contains(getString(R.string.auth_credential_is_incorrect)) == true -> {
                                    // Invalid Password
                                    Toast.makeText(
                                        this,
                                        getString(R.string.invalid_password_please_try_again),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                errorMessage?.contains(getString(R.string.unusual_activity)) == true -> {
                                    // Limit Exceeded
                                    Toast.makeText(
                                        this,
                                        getString(R.string.login_attempt_limit_exceeded_please_try_again_later),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    // Other exceptions
                                    Toast.makeText(
                                        this,
                                        getString(R.string.login_failed_please_try_again_later),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
            } else {
                Toast.makeText(this, getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignupPageRoute() {
        binding.signupLink.setOnClickListener {
            val signupActivityIntent = Intent(this, SignupActivity::class.java)
            startActivity(signupActivityIntent)
            finish()
        }
    }
}