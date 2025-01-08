package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth  // Instancia de FirebaseAuth
    private lateinit var db: FirebaseFirestore  // Instancia de FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar las vistas
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)

        // Inicializar FirebaseAuth y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configura el botón de login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validar los campos
            if (validateFields(email, password)) {
                // Iniciar sesión con Firebase Authentication
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Si el inicio de sesión es exitoso
                            val currentUser = auth.currentUser
                            currentUser?.let {
                                // Consultar los datos del usuario desde Firestore
                                db.collection("Usuarios").document(it.uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            // Extraer los datos del usuario de Firestore
                                            val nombre = document.getString("nombre")
                                            val correo = document.getString("correo")

                                            // Mostrar un mensaje con los datos obtenidos
                                            Toast.makeText(
                                                this,
                                                "Bienvenido $nombre",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Navegar al Dashboard
                                            val intent = Intent(this, DashboardActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Usuario no encontrado en Firestore",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Error al obtener los datos del usuario: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            // Si ocurre un error
                            Toast.makeText(this, "Error en el inicio de sesión: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Configura el botón de registro
        registerButton.setOnClickListener {
            // Abrir la pantalla de registro
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Función para validar si los campos están vacíos
    private fun validateFields(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                emailEditText.error = "Por favor, ingresa tu correo"
                false
            }
            password.isEmpty() -> {
                passwordEditText.error = "Por favor, ingresa tu contraseña"
                false
            }
            else -> true
        }
    }
}
