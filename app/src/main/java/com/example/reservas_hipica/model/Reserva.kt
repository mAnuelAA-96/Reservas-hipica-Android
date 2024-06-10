package com.example.reservas_hipica.model

data class Reserva (
    val id: Int,
    val id_alumno: Int,
    val id_caballo: Int,
    val fecha: String,
    val hora: String,
    val caballo: String,
    val comentarios: String?
)