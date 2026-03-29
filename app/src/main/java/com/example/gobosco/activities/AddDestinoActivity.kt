package com.example.gobosco.activities

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.gobosco.R
import com.example.gobosco.models.Destino
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddDestinoActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var etNombre: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etDesc: EditText
    private lateinit var spnPais: Spinner
    private lateinit var btnGuardar: Button

    private var imageUri: Uri? = null
    private var destinoId: String? = null
    private var urlImagenExistente: String? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_destino)

        ivPreview = findViewById(R.id.ivPreview)
        etNombre = findViewById(R.id.etNombreDestino)
        etPrecio = findViewById(R.id.etPrecio)
        etDesc = findViewById(R.id.etDescripcion)
        spnPais = findViewById(R.id.spnPais)
        btnGuardar = findViewById(R.id.btnGuardar)
        val btnFoto = findViewById<Button>(R.id.btnSeleccionarImg)

        val adapter = ArrayAdapter.createFromResource(this, R.array.paises_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnPais.adapter = adapter

        destinoId = intent.getStringExtra("DESTINO_ID")
        if (destinoId != null) {
            btnGuardar.text = "Actualizar Destino"
            cargarDatosDestino(destinoId!!)
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                ivPreview.setImageURI(uri)
            }
        }

        btnFoto.setOnClickListener { pickImage.launch("image/*") }

        btnGuardar.setOnClickListener {
            validarYGuardar()
        }
    }

    private fun cargarDatosDestino(id: String) {
        db.collection("destinos").document(id).get().addOnSuccessListener { doc ->
            val destino = doc.toObject(Destino::class.java)
            if (destino != null) {
                etNombre.setText(destino.nombre)
                etPrecio.setText(destino.precio.toString())
                etDesc.setText(destino.descripcion)
                urlImagenExistente = destino.imageUrl
                Glide.with(this).load(destino.imageUrl).into(ivPreview)
                val adapterP = spnPais.adapter as ArrayAdapter<String>
                val position = adapterP.getPosition(destino.pais)
                spnPais.setSelection(position)
            }
        }
    }

    private fun validarYGuardar() {
        val nombre = etNombre.text.toString().trim()
        val desc = etDesc.text.toString().trim()
        val precioStr = etPrecio.text.toString()
        val precio = precioStr.toDoubleOrNull() ?: 0.0
        val pais = spnPais.selectedItem.toString()

        if (nombre.isEmpty() || desc.isEmpty() || pais == "Seleccione un país") {
            showCustomToast("Por favor, rellene todos los campos", true)
            return
        }

        if (imageUri == null && urlImagenExistente == null) {
            showCustomToast("Debe seleccionar una imagen para el destino", true)
            return
        }

        if (precio <= 0) {
            showCustomToast("El precio debe ser mayor a 0", true)
            return
        }

        if (desc.length < 20) {
            showCustomToast("La descripción debe tener al menos 20 caracteres", true)
            return
        }

        if (imageUri != null) {
            subirImagenYDatos(nombre, pais, precio, desc)
        } else {
            guardarEnFirestore(nombre, pais, precio, desc, urlImagenExistente!!)
        }
    }

    private fun subirImagenYDatos(nombre: String, pais: String, precio: Double, desc: String) {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.child("destinos/$fileName")

        ref.putFile(imageUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                guardarEnFirestore(nombre, pais, precio, desc, url.toString())
            }
        }.addOnFailureListener {
            showCustomToast("Error al subir imagen", true)
        }
    }

    private fun guardarEnFirestore(nombre: String, pais: String, precio: Double, desc: String, url: String) {
        val destino = Destino(destinoId, nombre, pais, precio, desc, url)

        if (destinoId != null) {
            db.collection("destinos").document(destinoId!!).set(destino)
                .addOnSuccessListener {
                    showCustomToast("Destino actualizado con éxito", false)
                    finish()
                }
        } else {
            db.collection("destinos").add(destino)
                .addOnSuccessListener {
                    showCustomToast("Destino guardado con éxito", false)
                    finish()
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
        toast.setGravity(Gravity.TOP, 0, 100)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}