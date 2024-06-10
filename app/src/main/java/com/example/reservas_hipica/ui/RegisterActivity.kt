package com.example.reservas_hipica.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import com.example.reservas_hipica.R
import com.example.reservas_hipica.databinding.ActivityRegisterBinding
import com.example.reservas_hipica.model.Usuario
import com.example.reservas_hipica.retrofit.ApiService
import com.example.reservas_hipica.retrofit.RetrofitClient
import com.example.reservas_hipica.retrofit.request.RegisterRequest
import com.example.reservas_hipica.retrofit.response.StoreResponse
import com.example.reservas_hipica.retrofit.response.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGuardarRegistro.setOnClickListener(this)
        binding.btnCancelarRegistro.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == binding.btnGuardarRegistro) {
            register()
        } else if (v == binding.btnCancelarRegistro) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun register() {

        var formularioValido = true
        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

        val nombre = binding.etNombreRegistro.text.toString()
        val email = binding.etEmailRegistro.text.toString()
        val password = binding.etPasswordRegistro.text.toString()
        val confirm_password = binding.etPasswordRepetidaRegistro.text.toString()

        if (!emailPattern.matcher(email).matches()) {
            formularioValido = false
            binding.etEmailRegistro.setError("Email inválido")
        }

        if (!password.equals(confirm_password)) {
            formularioValido = false
            binding.etPasswordRegistro.setError("Las contraseñas no coinciden")
            binding.etPasswordRepetidaRegistro.setError("Las contraseñas no coinciden")
        }

        if (formularioValido) {
            val registerRequest = RegisterRequest(nombre, email, password, confirm_password)
            Log.d("RegisterRequest", registerRequest.toString())
            registerApi(registerRequest)
        }
    }
    private fun registerApi(registerRequest: RegisterRequest) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.register(registerRequest).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if(response.isSuccessful) {
                    val userResponse = response.body()

                    if (userResponse?.code == 200) {
                        Log.d("API_RESPONSE", "Mensaje: ${userResponse?.mensaje}")
                        Usuario.id = userResponse?.id!!
                        Usuario.nombre = userResponse?.name!!
                        Usuario.email = userResponse?.email!!
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)

                    } else if (userResponse?.code == 402) {
                        Log.e("API_RESPONSE", "Error: ${userResponse?.code}, ${userResponse?.mensaje}")
                        binding.etEmailRegistro.setError("Email ya registrado")
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

}