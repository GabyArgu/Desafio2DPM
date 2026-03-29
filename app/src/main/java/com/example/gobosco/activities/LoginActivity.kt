package com.example.gobosco.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.example.gobosco.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnVerPassword = findViewById<ImageView>(R.id.btnVerPassword)

        btnVerPassword.setOnClickListener {
            if (isPasswordVisible) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnVerPassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnVerPassword.setImageResource(R.drawable.ic_eye_closed)
            }
            isPasswordVisible = !isPasswordVisible
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showCustomToast("¡Bienvenido de nuevo!", false)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        val msg = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "Esta cuenta no está registrada"
                            is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
                            else -> "Error de conexión. Inténtalo de nuevo"
                        }
                        showCustomToast(msg, true)
                    }
                }
            } else {
                showCustomToast("Por favor, completa todos los campos", true)
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

                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showCustomToast("¡Cuenta creada con éxito!", false)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        val msg = when (task.exception) {
                            is FirebaseAuthUserCollisionException -> "Este correo ya está registrado"
                            is FirebaseAuthInvalidCredentialsException -> "El formato del correo es inválido"
                            else -> "No se pudo crear la cuenta"
                        }
                        showCustomToast(msg, true)
                    }
                }
            } else {
                showCustomToast("Por favor, completa todos los campos", true)
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
        toast.setGravity(Gravity.TOP, 0, 150)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}