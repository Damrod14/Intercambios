package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ViewExchangesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var recyclerViewExchanges: RecyclerView
    private lateinit var exchangeDetailsTextView: TextView
    private lateinit var backButton: Button
    private lateinit var performExchangeButton: Button
    private lateinit var viewExchangeButton: Button

    private var selectedExchange: Exchange? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_exchanges)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerViewExchanges = findViewById(R.id.recycler_view_exchanges)
        exchangeDetailsTextView = findViewById(R.id.exchangeDetailsTextView)
        backButton = findViewById(R.id.backButton)
        performExchangeButton = findViewById(R.id.performExchangeButton)
        viewExchangeButton = findViewById(R.id.viewExchangeButton)

        recyclerViewExchanges.layoutManager = LinearLayoutManager(this)

        backButton.setOnClickListener { finish() }
        performExchangeButton.setOnClickListener { performExchange() }
        viewExchangeButton.setOnClickListener { viewExchange() }

        initialize()
    }

    private fun initialize() {
        val currentUserEmail = auth.currentUser?.email ?: ""
        db.collection("Usuarios")
            .whereEqualTo("correo", currentUserEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userName = documents.documents[0].getString("nombre") ?: ""
                    loadUserExchanges(userName)
                } else {
                    Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener datos del usuario: ${it.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun loadUserExchanges(userName: String) {
        val normalizedUserName = userName.trim().lowercase()
        db.collection("Intercambios")
            .get()
            .addOnSuccessListener { documents ->
                val filteredExchanges = documents.mapNotNull { doc ->
                    val exchange = doc.toObject(Exchange::class.java).apply {
                        id = doc.id
                    }
                    val normalizedParticipants = exchange.participantes.map { it.trim().lowercase() }
                    if (normalizedParticipants.contains(normalizedUserName)) exchange else null
                }

                if (filteredExchanges.isNotEmpty()) {
                    setupRecyclerView(filteredExchanges)
                } else {
                    Toast.makeText(this, "No se encontraron intercambios para el usuario.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar intercambios: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView(exchanges: List<Exchange>) {
        recyclerViewExchanges.adapter = ExchangeAdapter(exchanges) { selectedExchange ->
            this.selectedExchange = selectedExchange
            displayExchangeDetails(selectedExchange)
        }
    }

    private fun displayExchangeDetails(exchange: Exchange) {
        val detallesParticipantes = if (exchange.participantes.isNotEmpty()) {
            exchange.participantes.joinToString(", ")
        } else {
            "Sin participantes"
        }

        val detallesTemas = if (exchange.temas.isNotEmpty()) {
            exchange.temas.joinToString(", ")
        } else {
            "Sin temas"
        }

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val isExchangeDateReached = exchange.fecha <= currentDate
        val exchangeStatus = if (exchange.sorteoRealizado) {
            "El sorteo ya fue realizado."
        } else {
            if (isExchangeDateReached) {
                "El sorteo puede realizarse ahora."
            } else {
                "El sorteo aún no puede realizarse."
            }
        }

        performExchangeButton.isEnabled = isExchangeDateReached && !exchange.sorteoRealizado
        viewExchangeButton.isEnabled = exchange.sorteoRealizado

        val details = """
            Intercambio: ${exchange.nombre}
            
            Participantes: $detallesParticipantes
            
            Presupuesto Máximo: ${exchange.presupuesto} pesos
            
            Fecha Límite de Registro: ${exchange.fecha}
            
            Tematica: $detallesTemas
            
            Status del sorteo: $exchangeStatus
            
        """.trimIndent()

        exchangeDetailsTextView.text = details
    }

    private fun performExchange() {
        val exchange = selectedExchange ?: return

        // Lógica del sorteo
        val shuffledParticipants = exchange.participantes.shuffled()
        val pairs = shuffledParticipants.zip(shuffledParticipants.drop(1) + shuffledParticipants.first())
        val emparejamientos = pairs.toMap()

        // Actualizar Firestore con los emparejamientos
        db.collection("Intercambios").document(exchange.id)
            .update(
                "sorteoRealizado", true,
                "emparejamientos", emparejamientos
            )
            .addOnSuccessListener {
                Toast.makeText(this, "El sorteo se realizó con éxito.", Toast.LENGTH_SHORT).show()
                exchange.sorteoRealizado = true
                displayExchangeDetails(exchange)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al realizar el sorteo: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun viewExchange() {
        val exchange = selectedExchange ?: return

        if (!exchange.sorteoRealizado) {
            Toast.makeText(this, "El sorteo aún no se ha realizado.", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this, ExchangeDetailsActivity::class.java).apply {
                putExtra("EXCHANGE_ID", exchange.id)
            }
            startActivity(intent)
        }
    }
}
