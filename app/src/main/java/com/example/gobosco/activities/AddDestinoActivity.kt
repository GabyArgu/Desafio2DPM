package com.example.gobosco.activities

import android.net.Uri
import android.os.Bundle
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

        // Configurar Spinner de Países
        val adapter = ArrayAdapter.createFromResource(this, R.array.paises_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnPais.adapter = adapter

        // Verificar si es Edición
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

                // Cargar imagen previa
                Glide.with(this).load(destino.imageUrl).into(ivPreview)

                // Seleccionar país en Spinner
                val adapter = spnPais.adapter as ArrayAdapter<String>
                val position = adapter.getPosition(destino.pais)
                spnPais.setSelection(position)
            }
        }
    }

    private fun validarYGuardar() {
        val nombre = etNombre.text.toString()
        val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val desc = etDesc.text.toString()
        val pais = spnPais.selectedItem.toString()

        if (nombre.isEmpty() || desc.isEmpty() || (imageUri == null && urlImagenExistente == null) || pais == "Seleccione un país") {
            Toast.makeText(this, "Por favor rellene todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            // Si eligió una foto nueva, subirla
            subirImagenYDatos(nombre, pais, precio, desc)
        } else {
            // Si es edición y no cambió la foto, solo actualizar datos
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
        }
    }

    private fun guardarEnFirestore(nombre: String, pais: String, precio: Double, desc: String, url: String) {
        val destino = Destino(destinoId, nombre, pais, precio, desc, url)

        if (destinoId != null) {
            // ACTUALIZAR
            db.collection("destinos").document(destinoId!!).set(destino)
                .addOnSuccessListener {
                    Toast.makeText(this, "Destino Actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
        } else {
            // CREAR NUEVO
            db.collection("destinos").add(destino)
                .addOnSuccessListener {
                    Toast.makeText(this, "Destino Guardado", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}