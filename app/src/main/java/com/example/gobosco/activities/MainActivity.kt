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

        rvDestinos.layoutManager = LinearLayoutManager(this)

        // Configuramos el Adapter con las dos funciones: Editar y Eliminar
        adapter = DestinoAdapter(listaDestinos,
            { destino -> // CLICK NORMAL: Editar
                val intent = Intent(this, AddDestinoActivity::class.java)
                intent.putExtra("DESTINO_ID", destino.id)
                startActivity(intent)
            },
            { destino -> // CLICK LARGO: Eliminar
                mostrarDialogoEliminar(destino)
            }
        )
        rvDestinos.adapter = adapter

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
        val view = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvMensaje = view.findViewById<TextView>(R.id.tvMensajeDialogo)
        val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarConfirmar)

        tvMensaje.text = "¿Estás seguro de que deseas eliminar ${destino.nombre} de tus destinos?"

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnEliminar.setOnClickListener {
            destino.id?.let { id ->
                db.collection("destinos").document(id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Destino eliminado con éxito", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
            }
        }
        dialog.show()
    }
}