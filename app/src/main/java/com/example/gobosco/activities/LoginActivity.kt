package com.example.gobosco.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.gobosco.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Usuario Creado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
            }
        }
    }
}