package com.example.reservas_hipica.retrofit.request

data class AddRequest (
    val alumno: Int,
    val fecha: String,
    val hora: String,
    val caballo: String,
    val comentarios: String
)