package com.example.reservas_hipica.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.reservas_hipica.databinding.ItemReservaBinding
import com.example.reservas_hipica.model.Reserva
import com.example.reservas_hipica.model.Usuario
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReservaViewHolder (view : View) : RecyclerView.ViewHolder(view) {

    var binding = ItemReservaBinding.bind(view)

    fun render(reserva: Reserva, onClickListener: (Reserva) -> Unit){

        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = formatoFecha.parse(reserva.fecha)
        val calendar = Calendar.getInstance()
        calendar.time = fecha ?: Date()
        val diaSemana = calendar.get(Calendar.DAY_OF_WEEK)
        var dia = ""

        when (diaSemana) {
            1 -> dia = "Domingo"
            7 -> dia = "Sábado"
            else -> dia = "Error"
        }

        val day = String.format("%02d", fecha.date)
        val month = String.format("%02d", fecha.month + 1)
        val year = fecha.year + 1900

        binding.tvDiaSemana.text = dia
        binding.tvFecha.text = "${day}-${month}-${year}"
        binding.tvHora.text = reserva.hora.substring(0, 5)
        binding.tvNombreCaballo.text = reserva.caballo
        /*
        binding.tvNombreJinete.text = Usuario.nombre
        binding.tvNumeroTelefono.text = Usuario.email

         */

        itemView.setOnClickListener{onClickListener(reserva)}
    }
}