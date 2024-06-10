package com.example.reservas_hipica.retrofit.request

data class RegisterRequest (
    val name: String,
    val email: String,
    val password: String,
    val confirm_password: String
)