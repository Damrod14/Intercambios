package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExchangeAdapter(
    private val exchanges: List<Exchange>,
    private val onExchangeClick: (Exchange) -> Unit
) : RecyclerView.Adapter<ExchangeAdapter.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.exchange_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exchange, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exchange = exchanges[position]
        holder.name.text = exchange.nombre
        holder.itemView.isSelected = position == selectedPosition

        holder.itemView.setOnClickListener {
            if (selectedPosition != position) {
                notifyItemChanged(selectedPosition)
                selectedPosition = position
                notifyItemChanged(selectedPosition)
                onExchangeClick(exchange)
            }
        }
    }

    override fun getItemCount(): Int = exchanges.size
}
