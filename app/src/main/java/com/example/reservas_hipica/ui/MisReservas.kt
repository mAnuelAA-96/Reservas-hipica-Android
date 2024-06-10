package com.example.reservas_hipica.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.reservas_hipica.R
import com.example.reservas_hipica.adapter.ReservaAdapter
import com.example.reservas_hipica.databinding.ActivityMisReservasBinding
import com.example.reservas_hipica.model.Caballo
import com.example.reservas_hipica.model.Reserva
import com.example.reservas_hipica.model.Usuario
import com.example.reservas_hipica.retrofit.ApiService
import com.example.reservas_hipica.retrofit.RetrofitClient
import com.example.reservas_hipica.retrofit.request.DeleteRequest
import com.example.reservas_hipica.retrofit.response.StoreResponse
import com.example.reservas_hipica.retrofit.response.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisReservas : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityMisReservasBinding
    private lateinit var reservaAdapter: ReservaAdapter

    private var listaReservas = mutableListOf<Reserva>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisReservasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        getReservas(Usuario.id)
        getListaCaballos()

        binding.fbtnAdd.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if (v == binding.fbtnAdd) {
            val intent = Intent(this, AddReserva::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        //binding.recyclerView.layoutManager = LinearLayoutManager(this)
        reservaAdapter =
            ReservaAdapter(listaReservas) {reserva ->
                onItemSelected(reserva)
            }
        binding.recyclerView.adapter = reservaAdapter

        val itemSwipeLeft = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                target: ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                onSwipedLeft(viewHolder)
            }
        }

        val swapLeft = ItemTouchHelper(itemSwipeLeft)
        swapLeft.attachToRecyclerView(binding.recyclerView)
    }

    private fun onSwipedLeft(viewHolder: ViewHolder) {
        val position = viewHolder.position
        val reserva = listaReservas.get(position)
        val builder = AlertDialog.Builder(this)
            .setTitle("Borrar reserva")
            .setMessage("¿Quieres borrar la reserva?")
            .setPositiveButton("Confirmar") { dialog, which ->
                deleteReserva(reserva)
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                reservaAdapter.notifyDataSetChanged()
            }
        builder.show()
    }

    private fun onItemSelected(reserva: Reserva) {
        val intent = Intent(this, EditReserva::class.java)
        intent.putExtra("id", reserva.id)
        intent.putExtra("id_alumno", reserva.id_alumno)
        intent.putExtra("id_caballo", reserva.id_caballo)
        intent.putExtra("nombre_caballo", reserva.caballo)
        intent.putExtra("fecha", reserva.fecha)
        intent.putExtra("hora", reserva.hora)
        intent.putExtra("comentarios", reserva.comentarios)
        startActivity(intent)
        Toast.makeText(this, reserva.caballo, Toast.LENGTH_SHORT).show()
    }

    private fun getReservas(userId: Int) {

        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.getReservas(userId).enqueue(object : Callback<List<Reserva>> {
            override fun onResponse(call: Call<List<Reserva>>, response: Response<List<Reserva>>) {
                if (response.isSuccessful) {
                    val reservations = response.body()
                    reservations?.forEach { reservation ->
                        listaReservas.add(reservation)
                        Log.d("API_RESPONSE", "Fecha: ${reservation.fecha}, Hora: ${reservation.hora}, Caballo: ${reservation.caballo}, Comentarios: ${reservation.comentarios}")
                    }
                    reservaAdapter.notifyDataSetChanged()
                } else {
                    Log.e("API_ERROR", "Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Reserva>>, t: Throwable) {
                Log.e("API_ERROR", "Error during GET request", t)
            }
        })
    }

    private fun getListaCaballos() {

        AddReserva.listaCaballos.clear()
        val apiService = RetrofitClient.instance.create(ApiService::class.java)

        apiService.getListaCaballos().enqueue(object : Callback<List<Caballo>> {

            override fun onResponse(call: Call<List<Caballo>>, response: Response<List<Caballo>>) {
                if(response.isSuccessful) {
                    val caballos = response.body()
                    caballos?.forEach { caballo ->
                        AddReserva.listaCaballos.add(caballo.nombre)
                        Log.d("API_RESPONSE", "Caballo ${caballo.nombre} añadido a la lista")
                    }
                } else {
                    Log.e("API_ERROR", "Error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Caballo>>, t: Throwable) {
                Log.e("API_ERROR", "Error during GET request", t)
            }
        })
    }

    private fun deleteReserva(reserva: Reserva) {
        val apiService = RetrofitClient.instance.create(ApiService::class.java)
        val deleteRequest = DeleteRequest(reserva.id)

        apiService.deleteReserva(deleteRequest).enqueue(object : Callback<StoreResponse> {
            override fun onResponse(call: Call<StoreResponse>, response: Response<StoreResponse>) {
                if(response.isSuccessful) {
                    val storeResponse = response.body()
                    Log.d("API_RESPONSE", "${storeResponse?.mensaje}")
                    listaReservas.clear()
                    getReservas(Usuario.id)
                } else {
                    Log.e("API_ERROR", "Error: ${response.code()}, ${response.message()}")
                }
            }

            override fun onFailure(call: Call<StoreResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error during POST request")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_mis_reservas, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.logoutMenu -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {

        Usuario.id = 0
        Usuario.nombre = ""
        Usuario.email = ""
        Usuario.telefono = ""

        listaReservas.clear()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }

    override fun onDestroy() {
        listaReservas.clear()
        super.onDestroy()
    }
}