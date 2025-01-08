package com.example.proyecto

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ViewExchangeDetailsActivity : AppCompatActivity() {

    private lateinit var exchangeTitleTextView: TextView
    private lateinit var exchangeDetailsTextView: TextView
    private lateinit var participantsTextView: TextView
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_exchange_details)

        // Inicializar las vistas
        exchangeTitleTextView = findViewById(R.id.exchangeTitleTextView)
        exchangeDetailsTextView = findViewById(R.id.exchangeDetailsTextView)
        participantsTextView = findViewById(R.id.participantsTextView)
        confirmButton = findViewById(R.id.confirmButton)

        // Obtener el ID del intercambio desde el Intent
        val exchangeId = intent.getIntExtra("EXCHANGE_ID", -1)

        // Aquí puedes cargar los detalles del intercambio usando el ID
        loadExchangeDetails(exchangeId)

        // Configurar el botón de confirmación
        confirmButton.setOnClickListener {
            // Lógica para confirmar la participación
            // Por ahora, solo mostramos un mensaje
            Toast.makeText(this, "Confirmaste tu participación en el intercambio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadExchangeDetails(exchangeId: Int) {
        // Aquí deberías cargar los detalles del intercambio usando el ID.
        // Por ahora, vamos a simular algunos datos de ejemplo.

        if (exchangeId != -1) {
            // Simulación de datos de un intercambio
            val exchange = getExchangeById(exchangeId)

            // Mostrar los detalles en los TextViews
            exchangeTitleTextView.text = exchange.title
            exchangeDetailsTextView.text = "Fecha límite: ${exchange.deadline}\nMonto máximo: \$${exchange.maxAmount}"
            participantsTextView.text = "Participantes: ${exchange.participants.joinToString(", ")}"
        } else {
            Toast.makeText(this, "No se pudo cargar el intercambio", Toast.LENGTH_SHORT).show()
        }
    }

    // Simulamos los detalles de un intercambio
    private fun getExchangeById(exchangeId: Int): Exchange {
        // Aquí podrías consultar una base de datos o una API real.
        // Vamos a crear un intercambio simulado.

        return Exchange(
            id = exchangeId,
            title = "Intercambio de Navidad",
            maxAmount = 500.0,
            deadline = "25/12/2024",
            participants = listOf("Juan", "María", "Carlos")
        )
    }

    // Clase de datos para representar un intercambio
    data class Exchange(
        val id: Int,
        val title: String,
        val maxAmount: Double,
        val deadline: String,
        val participants: List<String>
    )
}
