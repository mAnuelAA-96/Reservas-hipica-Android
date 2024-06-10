package com.example.reservas_hipica.ui

import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.reservas_hipica.databinding.ActivityEditReservaBinding
import com.example.reservas_hipica.model.Reserva
import com.example.reservas_hipica.model.Usuario
import com.example.reservas_hipica.retrofit.ApiService
import com.example.reservas_hipica.retrofit.RetrofitClient
import com.example.reservas_hipica.retrofit.request.EditRequest
import com.example.reservas_hipica.retrofit.response.StoreResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class EditReserva : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityEditReservaBinding
    private lateinit var currentReserva: Reserva

    private var fechaSeleccionada = "Fecha inválida"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditReservaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        establecerCurrentReserva()

        binding.btnEditReserva.setOnClickListener(this)
        binding.btnCancelarReservaEdit.setOnClickListener(this)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, AddReserva.listaCaballos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCaballoEdit.adapter = adapter

        //Inicialización del calendarView a la fecha de la reserva
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        Log.e("current fecha", currentReserva.fecha)
        val fecha = formato.parse(currentReserva.fecha)
        val fechaMillis = fecha?.time ?: System.currentTimeMillis()
        fechaSeleccionada = currentReserva.fecha
        binding.calendarViewEdit.setDate(fechaMillis, false, true)
        binding.calendarViewEdit.setOnDateChangeListener{view, year, month, dayOfMonth ->
            fechaSeleccionada = fechaSeleccionada(year, month, dayOfMonth)
        }

        binding.etEditTelefono.setText(Usuario.telefono)

        //Establecemos los parámetros del formulario de acuerdo a la reserva a editar
        binding.spinnerCaballoEdit.setSelection(AddReserva.listaCaballos.indexOf(currentReserva.caballo))
        binding.etEditComentario.setText(currentReserva.comentarios)
        if(currentReserva.hora.equals("10:00:00")){
            binding.rbtnEdit10.isChecked = true
        } else if (currentReserva.hora.equals("11:00:00")){
            binding.rbtnEdit11.isChecked = true
        } else if (currentReserva.hora.equals("12:00:00")){
            binding.rbtnEdit12.isChecked = true
        } else if (currentReserva.hora.equals("13:00:00")){
            binding.rbtnEdit13.isChecked = true
        }
    }

    private fun establecerCurrentReserva(){
        val id = intent.getIntExtra("id", -1)
        val id_alumno = intent.getIntExtra("id_alumno", -1)
        val id_caballo = intent.getIntExtra("id_caballo", -1)
        val nombreCaballo = intent.getStringExtra("nombre_caballo")!!
        val fecha = intent.getStringExtra("fecha")!!
        val hora = intent.getStringExtra("hora")!!
        val comentarios = intent.getStringExtra("comentarios")
        currentReserva = Reserva(id, id_alumno, id_caballo, fecha, hora, nombreCaballo, comentarios)

    }

    override fun onClick(v: View?) {
        if (v == binding.btnEditReserva) {
            editReserva()

        } else if (v == binding.btnCancelarReservaEdit) {
            val intent = Intent(this, MisReservas::class.java)
            startActivity(intent)
        }
    }

    private fun editReserva() {

        var formularioValido = true
        val telefonoRegex = Regex("^[6-7]\\d{8}$")

        var nombreCaballo = binding.spinnerCaballoEdit.selectedItem.toString()
        var hora = horaSeleccionada()
        var comentarios = binding.etEditComentario.text.toString()
        val telefono = binding.etEditTelefono.text.toString()


        if(fechaSeleccionada.equals("Fecha inválida")) {
            formularioValido = false
            binding.tvEditErrorFecha.visibility = View.VISIBLE
        } else binding.tvEditErrorFecha.visibility = View.GONE

        if(hora.equals("Hora no seleccionada")){
            formularioValido = false
            binding.tvAddErrorHora.visibility = View.VISIBLE
        } else binding.tvAddErrorHora.visibility = View.GONE

        if(!telefonoRegex.matches(telefono)) {
            formularioValido = false
            binding.etEditTelefono.setError("Introduce un teléfono válido")
        } else Usuario.telefono = telefono

        if (formularioValido) {

            val editRequest = EditRequest( currentReserva.id, Usuario.id, fechaSeleccionada, hora, nombreCaballo, comentarios)
            Log.d("editRequest", editRequest.toString())
            editAPI(editRequest)

        } else {
            Toast.makeText(this, "Error al guardar la reserva", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editAPI(editRequest: EditRequest) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.editReserva(editRequest).enqueue(object : Callback<StoreResponse> {
            override fun onResponse(call: Call<StoreResponse>, response: Response<StoreResponse>) {
                if(response.isSuccessful) {
                    val storeResponse = response.body()

                    if (storeResponse?.code == 200) {
                        Log.d("API_RESPONSE", "${storeResponse?.mensaje}")
                        val intent = Intent(applicationContext, MisReservas::class.java)
                        startActivity(intent)

                    } else if (storeResponse?.code == 402) {
                        Log.d("API_RESPONSE", "Reserva no guardada: ${storeResponse?.mensaje}")
                        binding.tvErrorAlEditar.visibility = View.VISIBLE
                        binding.tvErrorAlEditar.text = "*${storeResponse?.mensaje}"
                    }
                } else {
                    Log.e("API_ERROR", "Error: ${response.code()}, ${response.message()}")
                }
            }

            override fun onFailure(call: Call<StoreResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error during POST request")
            }

        })
    }

    private fun fechaSeleccionada(year: Int, month: Int, dayOfMonth: Int): String {

        var fechaFormateada = "Fecha inválida"

        fechaFormateada = "$year-${String.format("%02d", month + 1)}-${String.format("%02d", dayOfMonth)}"

        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = formato.parse(fechaFormateada)

        val calendario = Calendar.getInstance()
        calendario.time = fecha ?: Date()
        val numeroDiaSemana = calendario.get(Calendar.DAY_OF_WEEK)

        if(numeroDiaSemana != 1 && numeroDiaSemana != 7){
            fechaFormateada = "Fecha inválida"
        }

        val fechaActual = LocalDate.now()
        if(fechaActual.isAfter(LocalDate.parse(fechaFormateada))){
            fechaFormateada = "Fecha inválida"
        }

        return fechaFormateada
    }

    private fun horaSeleccionada(): String {
        var hora = "Hora no seleccionada"

        if(binding.rbtnEdit10.isChecked){
            hora = "10:00:00"
        } else if (binding.rbtnEdit11.isChecked){
            hora = "11:00:00"
        } else if (binding.rbtnEdit12.isChecked){
            hora = "12:00:00"
        } else if (binding.rbtnEdit13.isChecked){
            hora = "13:00:00"
        }
        return hora
    }
}