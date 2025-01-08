package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.util.Patterns
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var aliasEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar vistas
        nameEditText = findViewById(R.id.nameEditText)
        aliasEditText = findViewById(R.id.aliasEditText)
        emailEditText = findViewById(R.id.registerEmailEditText)
        passwordEditText = findViewById(R.id.registerPasswordEditText)
        registerButton = findViewById(R.id.registerButton)

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Configurar el botón de registro
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val alias = aliasEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validar campos
            if (validateFields(name, alias, email, password)) {
                // Registrar el usuario con Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Si el registro es exitoso, guarda los datos en Firestore
                            val user = auth.currentUser
                            val userData = hashMapOf(
                                "nombre" to name,
                                "alias" to alias,
                                "correo" to email
                            )

                            // Crear un documento para el usuario en Firestore
                            user?.let {
                                db.collection("Usuarios").document(it.uid)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                        // Redirigir al Dashboard
                                        val intent = Intent(this, DashboardActivity::class.java)
                                        startActivity(intent)
                                        finish() // Cerrar la pantalla de registro
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error al guardar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            // Si ocurre un error
                            Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    // Función para validar si los campos están vacíos o son incorrectos
    private fun validateFields(name: String, alias: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                nameEditText.error = "Por favor, ingresa tu nombre"
                false
            }
            alias.isEmpty() -> {
                aliasEditText.error = "Por favor, ingresa tu alias"
                false
            }
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailEditText.error = "Por favor, ingresa un correo válido"
                false
            }
            password.isEmpty() -> {
                passwordEditText.error = "Por favor, ingresa tu contraseña"
                false
            }
            password.length < 6 -> {
                passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
                false
            }
            else -> true
        }
    }
}
