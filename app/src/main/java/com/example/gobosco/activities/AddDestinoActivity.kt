package com.example.gobosco.activities

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gobosco.R
import com.example.gobosco.models.Destino
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddDestinoActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_destino)

        ivPreview = findViewById(R.id.ivPreview)
        val etNombre = findViewById<EditText>(R.id.etNombreDestino)
        val etPrecio = findViewById<EditText>(R.id.etPrecio)
        val etDesc = findViewById<EditText>(R.id.etDescripcion)
        val spnPais = findViewById<Spinner>(R.id.spnPais)
        val btnFoto = findViewById<Button>(R.id.btnSeleccionarImg)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        // Configurar Spinner
        val adapter = ArrayAdapter.createFromResource(this, R.array.paises_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnPais.adapter = adapter

        // Lanzador de Galería
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                ivPreview.setImageURI(uri)
            }
        }

        btnFoto.setOnClickListener { pickImage.launch("image/*") }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val desc = etDesc.text.toString()
            val pais = spnPais.selectedItem.toString()

            // Validaciones Obligatorias [cite: 40, 41, 42, 43, 56]
            if (nombre.isEmpty() || desc.isEmpty() || imageUri == null || pais == "Seleccione un país") {
                Toast.makeText(this, getString(R.string.error_campos_vacios), Toast.LENGTH_SHORT).show()
            } else if (precio <= 0) {
                Toast.makeText(this, getString(R.string.error_precio), Toast.LENGTH_SHORT).show()
            } else if (desc.length < 20) {
                Toast.makeText(this, getString(R.string.error_descripcion), Toast.LENGTH_SHORT).show()
            } else {
                subirImagenYDatos(nombre, pais, precio, desc)
            }
        }
    }

    private fun subirImagenYDatos(nombre: String, pais: String, precio: Double, desc: String) {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.child("destinos/$fileName")

        ref.putFile(imageUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                val destino = Destino(null, nombre, pais, precio, desc, url.toString())
                db.collection("destinos").add(destino).addOnSuccessListener {
                    Toast.makeText(this, "Destino Guardado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
        }
    }
}