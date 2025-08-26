package com.example.expensetracker.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.expensetracker.databinding.ActivityLoginBinding
import com.example.expensetracker.ui.MainActivity
import com.example.expensetracker.util.SharedPrefManager
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefManager = SharedPrefManager(this)
        executor = ContextCompat.getMainExecutor(this)

        setupTitle()
        setupBiometrics()
        setupClickListeners()
    }

    private fun setupTitle() {
        if (sharedPrefManager.isPasswordSet()) {
            binding.textViewTitle.text = "Welcome Back"
            binding.buttonBiometrics.visibility = View.VISIBLE
        } else {
            binding.textViewTitle.text = "Create a Password"
            binding.buttonBiometrics.visibility = View.GONE
        }
    }

    private fun setupBiometrics() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    navigateToMain()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for Expense Tracker")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            handleLoginOrRegister()
        }

        binding.buttonBiometrics.setOnClickListener {
            if (BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
                biometricPrompt.authenticate(promptInfo)
            } else {
                Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLoginOrRegister() {
        val password = binding.editTextPassword.text.toString()
        if (password.isEmpty()) {
            binding.layoutPassword.error = "Password cannot be empty"
            return
        }

        if (sharedPrefManager.isPasswordSet()) {
            // Login
            if (sharedPrefManager.isPasswordCorrect(password)) {
                navigateToMain()
            } else {
                binding.layoutPassword.error = "Incorrect password"
            }
        } else {
            // Register
            sharedPrefManager.setPassword(password)
            Toast.makeText(this, "Password created successfully", Toast.LENGTH_SHORT).show()
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
