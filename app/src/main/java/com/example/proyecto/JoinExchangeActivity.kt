package com.example.proyecto

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JoinExchangeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var joinExchangeButton: Button
    private lateinit var exchangeCodeEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_exchange)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        joinExchangeButton = findViewById(R.id.btn_confirm_join)
        exchangeCodeEditText = findViewById(R.id.join_exchange_code)

        joinExchangeButton.setOnClickListener {
            val exchangeCode = exchangeCodeEditText.text.toString().trim()
            if (exchangeCode.isNotEmpty()) {
                joinExchange(exchangeCode)
            } else {
                Toast.makeText(this, "Por favor, ingrese la clave del intercambio.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun joinExchange(exchangeCode: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No se encontró un usuario autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserEmail = currentUser.email ?: "Correo desconocido"

        // Buscar el nombre del usuario en Firestore
        db.collection("Usuarios")
            .whereEqualTo("correo", currentUserEmail)
            .get()
            .addOnSuccessListener { userDocuments ->
                if (!userDocuments.isEmpty) {
                    val currentUserName = userDocuments.documents[0].getString("nombre") ?: "Usuario desconocido"

                    // Buscar el intercambio por su clave
                    db.collection("Intercambios")
                        .whereEqualTo("clave", exchangeCode)
                        .get()
                        .addOnSuccessListener { exchangeDocuments ->
                            if (!exchangeDocuments.isEmpty) {
                                val document = exchangeDocuments.documents[0]
                                val exchange = document.toObject(Exchange::class.java)
                                if (exchange != null) {
                                    // Verificar si el sorteo ya fue realizado
                                    if (exchange.sorteoRealizado) {
                                        Toast.makeText(
                                            this,
                                            "No puedes unirte al intercambio porque el sorteo ya ha sido realizado.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@addOnSuccessListener
                                    }

                                    // Verificar si el usuario ya es parte del intercambio
                                    if (!exchange.participantes.contains(currentUserName)) {
                                        // Añadir el usuario a los participantes
                                        val updatedParticipants = exchange.participantes.toMutableList()
                                        updatedParticipants.add(currentUserName)

                                        // Actualizar Firestore
                                        db.collection("Intercambios").document(document.id)
                                            .update("participantes", updatedParticipants)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    this,
                                                    "Te has unido al intercambio '${exchange.nombre}' exitosamente.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    this,
                                                    "Error al unirse al intercambio: ${it.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    } else {
                                        Toast.makeText(this, "Ya eres parte de este intercambio.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this, "Intercambio no válido.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "No se encontró ningún intercambio con esta clave.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al buscar el intercambio: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "No se encontró el usuario en Firestore.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al buscar el usuario: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
