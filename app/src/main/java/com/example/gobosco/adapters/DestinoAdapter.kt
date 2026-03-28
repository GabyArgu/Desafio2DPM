package com.example.gobosco.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gobosco.R
import com.example.gobosco.models.Destino

class DestinoAdapter(private var lista: List<Destino>) :
    RecyclerView.Adapter<DestinoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombre)
        val precio: TextView = view.findViewById(R.id.tvPrecio)
        val descripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val imagen: ImageView = view.findViewById(R.id.imgDestino)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destino, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val destino = lista[position]
        holder.nombre.text = destino.nombre
        holder.precio.text = holder.itemView.context.getString(R.string.formato_precio, destino.precio)
        holder.descripcion.text = destino.descripcion

        Glide.with(holder.itemView.context)
            .load(destino.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.imagen)
    }

    override fun getItemCount() = lista.size

    fun updateData(newList: List<Destino>) {
        lista = newList
        notifyDataSetChanged()
    }
}