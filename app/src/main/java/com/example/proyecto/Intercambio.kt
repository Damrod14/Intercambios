package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class Intercambio : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var exchangeNameEditText: EditText
    private lateinit var exchangeDateEditText: EditText
    private lateinit var exchangeTimeEditText: EditText
    private lateinit var exchangeLocationEditText: EditText
    private lateinit var exchangeBudgetEditText: EditText
    private lateinit var exchangeThemesEditText: EditText
    private lateinit var saveExchangeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asegúrate de que el layout corresponde a la actividad de creación de intercambio
        setContentView(R.layout.activity_create_exchange)  // O usa el layout correcto si es diferente

        db = FirebaseFirestore.getInstance()



        // Configura el listener del botón para guardar el intercambio
        saveExchangeButton.setOnClickListener {
            saveExchange()
        }
    }

    private fun saveExchange() {
        // Obtén los valores de los campos de texto
        val name = exchangeNameEditText.text.toString().trim()
        val date = exchangeDateEditText.text.toString().trim()
        val time = exchangeTimeEditText.text.toString().trim()
        val location = exchangeLocationEditText.text.toString().trim()
        val budget = exchangeBudgetEditText.text.toString().trim()
        val themes = exchangeThemesEditText.text.toString().trim()

        // Validar los datos antes de guardarlos
        if (!isValidInput(name, date, time, location, budget, themes)) return

        // Crear un objeto de intercambio
        val exchange = hashMapOf(
            "nombre" to name,
            "fecha" to date,
            "hora" to time,
            "lugar" to location,
            "presupuesto" to budget.toDoubleOrNull(),
            "temas" to themes.split(",").map { it.trim() }
        )

        // Guardar en la colección "Intercambios" de Firestore
        db.collection("Intercambios")
            .add(exchange)
            .addOnSuccessListener {
                // Mostrar mensaje de éxito
                Toast.makeText(this, "Intercambio creado con éxito", Toast.LENGTH_SHORT).show()
                navigateToExchangesActivity()
            }
            .addOnFailureListener { e ->
                // Mostrar mensaje de error
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isValidInput(
        name: String, date: String, time: String, location: String, budget: String, themes: String
    ): Boolean {
        // Validar si todos los campos están completos
        if (name.isEmpty() || date.isEmpty() || time.isEmpty() || location.isEmpty() || budget.isEmpty() || themes.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        // Validar el presupuesto
        if (budget.toDoubleOrNull() == null || budget.toDouble() <= 0) {
            Toast.makeText(this, "Por favor, ingresa un presupuesto válido", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun navigateToExchangesActivity() {
        // Redirigir a la actividad de listado de intercambios
        val intent = Intent(this, ExchangesActivity::class.java)
        startActivity(intent)
        finish()  // Finalizar la actividad actual para evitar volver atrás
    }
}
