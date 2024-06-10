package com.example.reservas_hipica.retrofit

import com.example.reservas_hipica.model.Caballo
import com.example.reservas_hipica.model.Reserva
import com.example.reservas_hipica.retrofit.request.AddRequest
import com.example.reservas_hipica.retrofit.request.DeleteRequest
import com.example.reservas_hipica.retrofit.request.EditRequest
import com.example.reservas_hipica.retrofit.request.LoginRequest
import com.example.reservas_hipica.retrofit.request.RegisterRequest
import com.example.reservas_hipica.retrofit.response.StoreResponse
import com.example.reservas_hipica.retrofit.response.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/login")
    fun login(@Body loginRequest: LoginRequest): Call<UserResponse>

    @POST("api/registrar")
    fun register(@Body registerRequest: RegisterRequest): Call<UserResponse>

    @GET("api/datosusuario/{email}")
    fun getDatosUsuario(@Path("email") email: String): Call<UserResponse>

    @GET("api/misreservas/{user_id}")
    fun getReservas(@Path("user_id") userId: Int): Call<List<Reserva>>

    @GET("api/caballos")
    fun getListaCaballos(): Call<List<Caballo>>

    @POST("api/addreserva")
    fun storeReserva(@Body addRequest: AddRequest): Call<StoreResponse>

    @POST("api/editreserva")
    fun editReserva(@Body editRequest: EditRequest): Call<StoreResponse>

    @POST("api/deletereserva")
    fun deleteReserva(@Body deleteRequest: DeleteRequest): Call<StoreResponse>
}