package com.example.gobosco.activities

import android.content.Intent
import android.os.Bundle
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
        AlertDialog.Builder(this)
            .setTitle("Eliminar Destino")
            .setMessage("¿Estás seguro de que deseas eliminar ${destino.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                destino.id?.let { id ->
                    db.collection("destinos").document(id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}