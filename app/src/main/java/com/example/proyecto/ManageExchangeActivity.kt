package com.example.proyecto

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManageExchangeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var recyclerViewExchanges: RecyclerView
    private lateinit var recyclerViewCurrentParticipants: RecyclerView
    private lateinit var recyclerViewAvailableUsers: RecyclerView

    private var selectedExchange: Exchange? = null
    private val selectedParticipants = mutableSetOf<Participant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_exchange)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerViewExchanges = findViewById(R.id.recycler_view_exchanges)
        recyclerViewCurrentParticipants = findViewById(R.id.recycler_view_current_participants)
        recyclerViewAvailableUsers = findViewById(R.id.recycler_view_available_users)

        recyclerViewExchanges.layoutManager = LinearLayoutManager(this)
        recyclerViewCurrentParticipants.layoutManager = LinearLayoutManager(this)
        recyclerViewAvailableUsers.layoutManager = LinearLayoutManager(this)

        // Configuración de botones
        findViewById<Button>(R.id.btn_show_add_participant_dialog).setOnClickListener {
            showAddParticipantDialog()
        }

        findViewById<Button>(R.id.btn_add_selected_users).setOnClickListener {
            addSelectedParticipantsToExchange()
        }

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
                    recyclerViewExchanges.adapter = ExchangeAdapter(filteredExchanges) { selectedExchange ->
                        this.selectedExchange = selectedExchange
                        loadParticipants(selectedExchange.participantes)
                    }
                } else {
                    Toast.makeText(this, "No se encontraron intercambios para el usuario.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar intercambios: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadParticipants(participantNames: List<String>) {
        if (participantNames.isEmpty()) {
            Toast.makeText(this, "No hay participantes en este intercambio.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Usuarios")
            .whereIn("nombre", participantNames)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No se encontraron participantes en la base de datos.", Toast.LENGTH_SHORT).show()
                } else {
                    val participants = documents.map { doc ->
                        Participant(
                            alias = doc.getString("alias") ?: "",
                            nombre = doc.getString("nombre") ?: "",
                            correo = doc.getString("correo") ?: ""
                        )
                    }

                    showParticipantsInRecyclerView(participants)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar participantes: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showParticipantsInRecyclerView(participants: List<Participant>) {
        recyclerViewCurrentParticipants.adapter = ParticipantsAdapterr(
            participants = participants,
            onParticipantClick = { participant ->
                removeParticipant(selectedExchange!!, participant)
            }
        )
    }

    private fun removeParticipant(exchange: Exchange, participant: Participant) {
        val updatedParticipants = exchange.participantes.toMutableList()
        updatedParticipants.remove(participant.nombre)

        if (exchange.id.isEmpty()) {
            Toast.makeText(this, "Error: ID del intercambio no encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Intercambios").document(exchange.id)
            .update("participantes", updatedParticipants)
            .addOnSuccessListener {
                Toast.makeText(this, "Participante eliminado correctamente", Toast.LENGTH_SHORT).show()
                loadParticipants(updatedParticipants)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar participante: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddParticipantDialog() {
        val currentParticipants = selectedExchange?.participantes ?: listOf()

        db.collection("Usuarios")
            .get()
            .addOnSuccessListener { documents ->
                val allUsers = documents.map { doc ->
                    Participant(
                        alias = doc.getString("alias") ?: "",
                        nombre = doc.getString("nombre") ?: "",
                        correo = doc.getString("correo") ?: ""
                    )
                }

                // Filtrar usuarios que no están en el intercambio actual
                val availableUsers = allUsers.filter { user -> !currentParticipants.contains(user.nombre) }

                if (availableUsers.isEmpty()) {
                    Toast.makeText(this, "No hay usuarios disponibles para añadir.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Mostrar usuarios disponibles en el RecyclerView
                showAvailableUsersRecyclerView(availableUsers)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar usuarios: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAvailableUsersRecyclerView(availableUsers: List<Participant>) {
        recyclerViewAvailableUsers.adapter = ParticipantsAdapterr(
            participants = availableUsers,
            onParticipantClick = { participant ->
                toggleParticipantSelection(participant)
            },
            isMultiSelect = true
        )
    }

    private fun toggleParticipantSelection(participant: Participant) {
        if (selectedParticipants.contains(participant)) {
            selectedParticipants.remove(participant)
        } else {
            selectedParticipants.add(participant)
        }
    }

    private fun addSelectedParticipantsToExchange() {
        val selectedNames = selectedParticipants.map { it.nombre }
        val updatedParticipants = selectedExchange?.participantes?.toMutableList() ?: mutableListOf()

        // Añadir los nuevos participantes
        updatedParticipants.addAll(selectedNames)

        if (selectedExchange?.id.isNullOrEmpty()) {
            Toast.makeText(this, "Error: ID del intercambio no encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        // Actualizar en Firestore
        db.collection("Intercambios").document(selectedExchange!!.id)
            .update("participantes", updatedParticipants)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuarios añadidos correctamente.", Toast.LENGTH_SHORT).show()
                loadParticipants(updatedParticipants) // Recargar participantes
                selectedParticipants.clear() // Limpiar selección
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al añadir usuarios: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
