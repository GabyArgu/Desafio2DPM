package com.example.gobosco.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gobosco.R
import com.example.gobosco.adapters.DestinoAdapter
import com.example.gobosco.models.Destino
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        // Configurar RecyclerView
        rvDestinos.layoutManager = LinearLayoutManager(this)
        adapter = DestinoAdapter(listaDestinos)
        rvDestinos.adapter = adapter

        // Botón para ir a Agregar Destino
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddDestinoActivity::class.java))
        }

        obtenerDestinos()
    }

    private fun obtenerDestinos() {
        // Escuchar cambios en tiempo real en Firestore
        db.collection("destinos").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener

            listaDestinos.clear()
            for (doc in snapshot.documents) {
                val destino = doc.toObject(Destino::class.java)
                if (destino != null) {
                    destino.id = doc.id // Guardamos el ID para editar/eliminar luego
                    listaDestinos.add(destino)
                }
            }
            adapter.updateData(listaDestinos)
        }
    }
}