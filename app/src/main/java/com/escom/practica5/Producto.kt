package com.escom.practica5

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val precio: Number = 0.0,
    val descripcion: String = "",
    val categoria: String = "",
    val enStock: Boolean = true
)