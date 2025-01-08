package com.example.proyecto

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExchangeDetailsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var exchangeDetailsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_details)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        exchangeDetailsTextView = findViewById(R.id.exchangeDetailsTextView)

        val exchangeId = intent.getStringExtra("EXCHANGE_ID")
        if (exchangeId != null) {
            loadExchangeDetails(exchangeId)
        } else {
            exchangeDetailsTextView.text = "No se proporcionó un ID de intercambio."
        }
    }

    private fun loadExchangeDetails(exchangeId: String) {
        db.collection("Intercambios").document(exchangeId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val exchange = document.toObject(Exchange::class.java)
                    if (exchange != null) {
                        displayAssignedPerson(exchange)
                    } else {
                        exchangeDetailsTextView.text = "No se pudieron cargar los detalles del intercambio."
                    }
                } else {
                    exchangeDetailsTextView.text = "Intercambio no encontrado."
                }
            }
            .addOnFailureListener {
                exchangeDetailsTextView.text = "Error al cargar los detalles del intercambio: ${it.message}"
            }
    }

    private fun displayAssignedPerson(exchange: Exchange) {
        val currentUserEmail = auth.currentUser?.email ?: "Usuario desconocido"

        // Buscar el nombre del usuario actual en la colección "Usuarios"
        db.collection("Usuarios")
            .whereEqualTo("correo", currentUserEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val currentUserName = documents.documents[0].getString("nombre") ?: "Usuario desconocido"

                    // Buscar a la persona asignada en el campo "emparejamientos"
                    val assignedPerson = exchange.emparejamientos[currentUserName]

                    if (assignedPerson != null) {
                        val details = """
                        Nombre del intercambio: ${exchange.nombre}
                        
                        Integrante asignado: $assignedPerson
                        
                        Presupuesto: ${exchange.presupuesto} pesos
                        
                        Fecha: ${exchange.fecha}
                        
                        Tematica: ${exchange.temas.joinToString(", ")}
                    """.trimIndent()

                        exchangeDetailsTextView.text = details
                    } else {
                        exchangeDetailsTextView.text = "No se encontró a quién regalar en este intercambio."
                    }
                } else {
                    exchangeDetailsTextView.text = "Usuario no encontrado en Firesbase."
                }
            }
            .addOnFailureListener {
                exchangeDetailsTextView.text = "Error al cargar datos del usuario: ${it.message}"
            }
    }


}
