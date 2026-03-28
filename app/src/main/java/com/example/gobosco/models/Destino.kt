package com.example.gobosco.models

data class Destino(
    var id: String? = null,
    val nombre: String = "",
    val pais: String = "",
    val precio: Double = 0.0,
    val descripcion: String = "",
    val imageUrl: String = ""
)