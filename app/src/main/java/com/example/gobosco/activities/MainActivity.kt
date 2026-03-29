package com.example.gobosco.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gobosco.R
import com.example.gobosco.adapters.DestinoAdapter
import com.example.gobosco.models.Destino
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var rvDestinos: RecyclerView
    private lateinit var adapter: DestinoAdapter
    private val db = FirebaseFirestore.getInstance()
    private var listaDestinos = mutableListOf<Destino>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvDestinos = findViewById(R.id.rvDestinos)
        val fabAdd = findViewById<ExtendedFloatingActionButton>(R.id.fabAdd)

        // Configurar RecyclerView
        rvDestinos.layoutManager = LinearLayoutManager(this)

        // Inicializamos el adapter con la lista y la lógica de click largo
        adapter = DestinoAdapter(listaDestinos) { destino ->
            mostrarDialogoEliminar(destino)
        }
        rvDestinos.adapter = adapter

        // Botón para ir a Agregar Destino
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddDestinoActivity::class.java))
        }

        obtenerDestinos()
    }

    private fun obtenerDestinos() {
        db.collection("destinos").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener

            listaDestinos.clear()
            for (doc in snapshot.documents) {
                val destino = doc.toObject(Destino::class.java)
                if (destino != null) {
                    destino.id = doc.id
                    listaDestinos.add(destino)
                }
            }
            adapter.updateData(listaDestinos)
        }
    }

    private fun mostrarDialogoEliminar(destino: Destino) {
        // 1. Inflamos el diseño especializado que creamos
        val view = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()

        // TRUCO DE ESPECIALISTA: Fondo transparente para que se vean los bordes redondeados del XML
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 2. Referenciamos los textos y botones del layout personalizado
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensajeDialogo)
        val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarConfirmar)

        // Personalizamos el mensaje con el nombre del destino
        tvMensaje.text = "¿Estás seguro de que deseas eliminar ${destino.nombre} de tus destinos?"

        // 3. Lógica de los botones
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnEliminar.setOnClickListener {
            destino.id?.let { id ->
                db.collection("destinos").document(id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Destino eliminado con éxito", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
            }
        }

        dialog.show()
    }
}