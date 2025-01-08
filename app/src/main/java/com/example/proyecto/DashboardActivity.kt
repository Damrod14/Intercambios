package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var createExchangeButton: Button
    private lateinit var viewExchangesButton: Button
    private lateinit var joinExchangeButton: Button // Nuevo botón
    private lateinit var manageExchangeButton: Button
    private lateinit var logoutButton: Button
    private lateinit var userNameTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Inicializar FirebaseAuth y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar vistas
        createExchangeButton = findViewById(R.id.btn_create_exchange)
        viewExchangesButton = findViewById(R.id.btn_view_exchanges)
        joinExchangeButton = findViewById(R.id.btn_join_exchange) // Nuevo botón
        manageExchangeButton = findViewById(R.id.btn_manage_exchange)
        logoutButton = findViewById(R.id.btn_logout)
        userNameTextView = findViewById(R.id.tv_user_name)

        // Obtener el usuario autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Mostrar el nombre del usuario
            val userName = currentUser.displayName ?: "Usuario"
            userNameTextView.text = "Bienvenido, $userName"
        } else {
            // Si no hay usuario autenticado, redirigir al login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar los botones
        createExchangeButton.setOnClickListener {
            val intent = Intent(this, CreateExchangeActivity::class.java)
            startActivity(intent)
        }

        viewExchangesButton.setOnClickListener {
            val intent = Intent(this, ViewExchangesActivity::class.java)
            startActivity(intent)
        }

        joinExchangeButton.setOnClickListener {
            val intent = Intent(this, JoinExchangeActivity::class.java) // Nueva actividad
            startActivity(intent)
        }

        manageExchangeButton.setOnClickListener {
            val intent = Intent(this, ManageExchangeActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            // Cerrar sesión y regresar a la pantalla principal
            auth.signOut()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
