package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExchangesAdapter(
    private val exchanges: List<Exchange>,
    private val onExchangeClick: (Exchange) -> Unit
) : RecyclerView.Adapter<ExchangesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.exchange_name) // Asegúrate de que el ID corresponde al layout XML
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exchange, parent, false) // Asegúrate de que este layout es correcto
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exchange = exchanges[position]
        holder.name.text = exchange.nombre // Cambiar 'titulo' por 'nombre'
        holder.itemView.setOnClickListener {
            onExchangeClick(exchange)
        }
    }

    override fun getItemCount(): Int = exchanges.size
}
