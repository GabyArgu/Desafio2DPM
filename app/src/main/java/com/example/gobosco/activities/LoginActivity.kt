package com.example.gobosco.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
                        showCustomToast("Bienvenido a GoBosco", false)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        showCustomToast("Error: ${it.exception?.message}", true)
                    }
                }
            } else {
                showCustomToast(getString(R.string.error_campos_vacios), true)
            }
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                if (pass.length < 6) {
                    showCustomToast("La contraseña debe tener al menos 6 caracteres", true)
                    return@setOnClickListener
                }

                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showCustomToast("Usuario creado con éxito", false)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        showCustomToast("Error: ${it.exception?.message}", true)
                    }
                }
            } else {
                showCustomToast(getString(R.string.error_campos_vacios), true)
            }
        }
    }

    private fun showCustomToast(mensaje: String, esError: Boolean) {
        val layout = layoutInflater.inflate(R.layout.custom_toast, findViewById(R.id.toast_layout_root))
        val fondo = layout.findViewById<View>(R.id.toast_layout_root)
        val icono = layout.findViewById<ImageView>(R.id.toast_icon)
        val texto = layout.findViewById<TextView>(R.id.toast_text)

        texto.text = mensaje

        if (esError) {
            fondo.backgroundTintList = ColorStateList.valueOf(getColor(R.color.accent))
            icono.setImageResource(android.R.drawable.ic_dialog_alert)
            icono.imageTintList = ColorStateList.valueOf(getColor(R.color.white))
            texto.setTextColor(getColor(R.color.white))
        } else {
            fondo.backgroundTintList = ColorStateList.valueOf(getColor(R.color.teal_custom))
            icono.setImageResource(android.R.drawable.ic_dialog_info)
            icono.imageTintList = ColorStateList.valueOf(getColor(R.color.primary))
            texto.setTextColor(getColor(R.color.primary))
        }

        val toast = Toast(applicationContext)
        toast.setGravity(android.view.Gravity.TOP, 0, 100)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}