package com.example.reservas_hipica.ui

import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reservas_hipica.databinding.ActivityAddReservaBinding
import com.example.reservas_hipica.model.Usuario
import com.example.reservas_hipica.retrofit.ApiService
import com.example.reservas_hipica.retrofit.RetrofitClient
import com.example.reservas_hipica.retrofit.request.AddRequest
import com.example.reservas_hipica.retrofit.response.StoreResponse
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class AddReserva : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityAddReservaBinding
    companion object {
        var listaCaballos = mutableListOf<String>()
    }
    private var fechaSeleccionada = "Fecha inválida"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReservaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddReserva.setOnClickListener(this)
        binding.btnCancelarReserva.setOnClickListener(this)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaCaballos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCaballo.adapter = adapter

        //Inicialización del calendarView a la fecha actual
        val fechaActual = System.currentTimeMillis()
        binding.calendarView.setDate(fechaActual, false, true)
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            fechaSeleccionada = fechaSeleccionada(year, month, dayOfMonth)
        }

        binding.etAddTelefono.setText(Usuario.telefono)
    }

    override fun onClick(v: View?) {
        if (v == binding.btnAddReserva) {
            saveReserva()

        } else if (v == binding.btnCancelarReserva) {
            val intent = Intent(this, MisReservas::class.java)
            startActivity(intent)
        }
    }

    private fun saveReserva() {

        var formularioValido = true
        val telefonoRegex = Regex("^[6-7]\\d{8}$")

        val nombreCaballo = binding.spinnerCaballo.selectedItem.toString()
        val hora = horaSeleccionada()
        val comentarios = binding.etAddComentario.text.toString()
        val telefono = binding.etAddTelefono.text.toString()


        if(fechaSeleccionada.equals("Fecha inválida")) {
            formularioValido = false
            binding.tvAddErrorFecha.visibility = View.VISIBLE
        } else binding.tvAddErrorFecha.visibility = View.GONE

        if(hora.equals("Hora no seleccionada")){
            formularioValido = false
            binding.tvAddErrorHora.visibility = View.VISIBLE
        } else binding.tvAddErrorHora.visibility = View.GONE

        if(!telefonoRegex.matches(telefono)) {
            formularioValido = false
            binding.etAddTelefono.setError("Introduce un teléfono válido")
        } else Usuario.telefono = telefono

        if (formularioValido) {

            val addRequest = AddRequest(Usuario.id, fechaSeleccionada, hora, nombreCaballo, comentarios)
            Log.d("addRequest", addRequest.toString())
            storeAPI(addRequest)

        } else {
            Toast.makeText(this, "Error al guardar la reserva", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeAPI(addRequest: AddRequest) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.storeReserva(addRequest).enqueue(object : Callback<StoreResponse> {
            override fun onResponse(call: Call<StoreResponse>, response: Response<StoreResponse>) {
                if(response.isSuccessful) {
                    val storeResponse = response.body()

                    if (storeResponse?.code == 200) {
                        Log.d("API_RESPONSE", "${storeResponse?.mensaje}")
                        val intent = Intent(applicationContext, MisReservas::class.java)
                        startActivity(intent)

                        sendMessage(addRequest.caballo, addRequest.fecha, addRequest.hora)

                    } else if (storeResponse?.code == 402) {
                        Log.d("API_RESPONSE", "Reserva no guardada: ${storeResponse?.mensaje}")
                        binding.tvErrorAlGuardar.visibility = View.VISIBLE
                        binding.tvErrorAlGuardar.text = "*${storeResponse?.mensaje}"
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

        if(binding.rbtnAdd10.isChecked){
            hora = "10:00:00"
        } else if (binding.rbtnAdd11.isChecked){
            hora = "11:00:00"
        } else if (binding.rbtnAdd12.isChecked){
            hora = "12:00:00"
        } else if (binding.rbtnAdd13.isChecked){
            hora = "13:00:00"
        }
        return hora
    }

    private fun sendMessage(caballo: String, fecha: String, hora: String){

        val mensaje = "¡Gracias por confirmar tu reserva!\n" +
                "Detalles de la reserva:\n" +
                "Nombre del caballo: $caballo\n" +
                "Fecha de la reserva: $fecha\n" +
                "Hora de la reserva: $hora"

        if (!Usuario.telefono.equals("") && !Usuario.telefono.isNullOrEmpty()) {
            val phoneNumber = Usuario.telefono

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(mensaje)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            applicationContext.startActivity(intent)

        } else {
            Log.d("Mensaje no enviado", "Hay un error en el número de teléfono")
        }
    }

    private fun sendWa() {
        val ACCOUNT_SID = "AC905c814be74cfd4742e251612a64b431"
        val AUTH_TOKEN = "7fbb7687a60768ee403aba0839590447"

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
        Message.creator(
                PhoneNumber("whatsapp:+34660545697"),
                PhoneNumber("whatsapp:+14155238886"),
                "Mensaje de prueba app").create()
    }

}