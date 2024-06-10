package com.example.reservas_hipica.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.reservas_hipica.R
import com.example.reservas_hipica.model.Reserva

class ReservaAdapter (private val listaReservas: List<Reserva>, private val onClickListener: (Reserva) -> Unit):
    RecyclerView.Adapter<ReservaViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ReservaViewHolder(layoutInflater.inflate((R.layout.item_reserva), parent, false))
    }

    override fun getItemCount(): Int {
        return listaReservas.size
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        val item = listaReservas[position]
        holder.render(item, onClickListener)
    }
}