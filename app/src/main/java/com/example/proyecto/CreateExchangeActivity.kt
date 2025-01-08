package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateExchangeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private var usersList: MutableList<User> = mutableListOf()
    private var selectedUsers: MutableList<User> = mutableListOf()

    private lateinit var saveExchangeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_exchange)

        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario está autenticado
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        db = FirebaseFirestore.getInstance()

        // Configuración del RecyclerView
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        usersRecyclerView.layoutManager = LinearLayoutManager(this)

        // Referencia al botón
        saveExchangeButton = findViewById(R.id.saveExchangeButton)

        // Obtener usuarios desde Firestore
        getUsersFromFirestore()

        // Configurar el adaptador
        usersAdapter = UsersAdapter(usersList) { user, isSelected ->
            if (isSelected) {
                selectedUsers.add(user)
            } else {
                selectedUsers.remove(user)
            }
        }

        usersRecyclerView.adapter = usersAdapter

        // Guardar intercambio
        saveExchangeButton.setOnClickListener {
            saveExchange()
        }
    }

    private fun getUsersFromFirestore() {
        db.collection("Usuarios")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val user = User(
                        correo = document.getString("correo") ?: "",
                        nombre = document.getString("nombre") ?: ""
                    )
                    usersList.add(user)
                }
                usersAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al obtener usuarios: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveExchange() {
        val nombreIntercambio = findViewById<EditText>(R.id.exchangeNameEditText).text.toString().trim()
        val fecha = findViewById<EditText>(R.id.exchangeDateEditText).text.toString().trim()
        val hora = findViewById<EditText>(R.id.exchangeTimeEditText).text.toString().trim()
        val lugar = findViewById<EditText>(R.id.exchangeLocationEditText).text.toString().trim()
        val clave = findViewById<EditText>(R.id.exchangeKeyEditText).text.toString().trim()
        val presupuesto = findViewById<EditText>(R.id.exchangeBudgetEditText).text.toString().trim()
        val temas = findViewById<EditText>(R.id.exchangeThemesEditText).text.toString().trim()

        if (nombreIntercambio.isEmpty() || fecha.isEmpty() || hora.isEmpty() || lugar.isEmpty() || clave.isEmpty() || presupuesto.isEmpty() || temas.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (presupuesto.toDoubleOrNull() == null || presupuesto.toDouble() <= 0) {
            Toast.makeText(this, "Por favor, ingresa un presupuesto válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (clave.length < 4) {
            Toast.makeText(this, "La clave debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        val exchangeData = hashMapOf(
            "nombre" to nombreIntercambio,
            "fecha" to fecha,
            "hora" to hora,
            "lugar" to lugar,
            "clave" to clave,
            "presupuesto" to presupuesto.toDoubleOrNull(),
            "temas" to temas.split(",").map { it.trim() },
            "participantes" to selectedUsers.map { it.nombre }
        )

        db.collection("Intercambios")
            .add(exchangeData)
            .addOnSuccessListener {
                Toast.makeText(this, "Intercambio creado con éxito", Toast.LENGTH_SHORT).show()

                // Enviar correos a los participantes seleccionados
                for (user in selectedUsers) {
                    sendEmail(
                        user.correo,
                        "Invitación intercambio: $nombreIntercambio",
                        """
                        Hola ${user.nombre},

                        Esta es una invitacion  al intercambio $nombreIntercambio!
                        Identificador: $clave
                        El intercambio será el día :"$fecha"
                        A las: $hora En: $lugar
                        Presupuesto será de: $presupuesto pesos
                        """.trimIndent()
                    )
                }

                navigateToDashboard()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el intercambio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendEmail(recipient: String, subject: String, message: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            startActivity(Intent.createChooser(intent, "Enviar correo..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No hay aplicaciones de correo disponibles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
