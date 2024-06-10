package com.example.reservas_hipica.retrofit.request

data class EditRequest (
    val id_reserva_actual: Int,
    val alumno: Int,
    val fecha: String,
    val hora: String,
    val caballo: String,
    val comentarios: String
)