package com.example.reservas_hipica.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import com.bumptech.glide.Glide
import com.example.reservas_hipica.databinding.ActivityMainBinding
import com.example.reservas_hipica.retrofit.ApiService
import com.example.reservas_hipica.retrofit.request.LoginRequest
import com.example.reservas_hipica.model.Usuario
import com.example.reservas_hipica.retrofit.RetrofitClient
import com.example.reservas_hipica.retrofit.response.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), OnClickListener {

    lateinit var binding: ActivityMainBinding

    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .load("https://manuelflo.com/images/reservas-hipica/logo-caballo.jpg")
            .into(binding.ivLogo)

        binding.btnLogin.setOnClickListener(this)
        binding.btnRegistrar.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == binding.btnLogin) {
            login()

        } else if (v == binding.btnRegistrar) {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login() {
        var formularioValido = true

        val email = binding.etEmailLogin.text.toString()
        val password = binding.etPasswordLogin.text.toString()

        if (email.isEmpty()) {
            formularioValido = false
            binding.etEmailLogin.setError("Introduce un email")
        }
        if (password.isEmpty()) {
            formularioValido = false
            binding.etPasswordLogin.setError("Introduce la contraseña")
        }

        if (formularioValido) {
            val loginRequest = LoginRequest(email, password)
            loginAPI(loginRequest)

        }
    }

    private fun loginAPI(loginRequest: LoginRequest){
        apiService = RetrofitClient.instance.create(ApiService::class.java)
        //val loginRequest = LoginRequest(email = "prueba@email.com", password = "123456789")

        apiService.login(loginRequest).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if(response.isSuccessful) {
                    val userResponse = response.body()

                    if(userResponse?.code == 200) {
                        Log.d("API_RESPONSE", userResponse?.mensaje.toString())
                        Usuario.id = userResponse?.id!!
                        Usuario.nombre = userResponse?.name!!
                        Usuario.email = userResponse?.email!!

                        val intent = Intent(applicationContext, MisReservas::class.java)
                        startActivity(intent)

                    } else if (userResponse?.code == 402) {
                        binding.etEmailLogin.setError(userResponse.mensaje)
                        binding.etPasswordLogin.setError(userResponse.mensaje)
                        Log.d("API_RESPONSE", "Error: ${userResponse?.code}, ${userResponse?.mensaje}")

                    }

                } else {
                    Log.e("API_ERROR", "Error: ${response.code()}, ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error during POST request: ${t.message}")
            }

        })
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }


}