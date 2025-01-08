package com.example.proyecto

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.FirebaseFirestore

// Usuario almacenado en Firebase
data class User(
    val nombre: String = "",
    val correo: String = "",
    val alias: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        nombre = parcel.readString() ?: "",
        correo = parcel.readString() ?: "",
        alias = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
        parcel.writeString(correo)
        parcel.writeString(alias)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(parcel: Parcel): User {
                return User(parcel)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }
    }
}

// Participante en un intercambio
data class Participant(
    val nombre: String = "",
    val correo: String = "",
    val alias: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        nombre = parcel.readString() ?: "",
        correo = parcel.readString() ?: "",
        alias = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
        parcel.writeString(correo)
        parcel.writeString(alias)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Participant> = object : Parcelable.Creator<Participant> {
            override fun createFromParcel(parcel: Parcel): Participant {
                return Participant(parcel)
            }

            override fun newArray(size: Int): Array<Participant?> {
                return arrayOfNulls(size)
            }
        }
    }
}

// Intercambio con participantes
data class Exchange(
    var id: String = "",
    val clave: String = "",
    val fecha: String = "",
    val hora: String = "",
    val lugar: String = "",
    val nombre: String = "",
    val participantes: List<String> = listOf(),
    val presupuesto: Long = 0L, // Cambiado de Int a Long
    val temas: List<String> = listOf(),
    var sorteoRealizado: Boolean = false, // Nuevo campo
    val emparejamientos: Map<String, String> = mapOf() // Campo adicional para emparejamientos
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        clave = parcel.readString() ?: "",
        fecha = parcel.readString() ?: "",
        hora = parcel.readString() ?: "",
        lugar = parcel.readString() ?: "",
        nombre = parcel.readString() ?: "",
        participantes = parcel.createStringArrayList() ?: listOf(),
        presupuesto = parcel.readLong(), // Actualizado para leer un Long
        temas = parcel.createStringArrayList() ?: listOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(clave)
        parcel.writeString(fecha)
        parcel.writeString(hora)
        parcel.writeString(lugar)
        parcel.writeString(nombre)
        parcel.writeStringList(participantes)
        parcel.writeLong(presupuesto) // Actualizado para escribir un Long
        parcel.writeStringList(temas)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Exchange> = object : Parcelable.Creator<Exchange> {
            override fun createFromParcel(parcel: Parcel): Exchange {
                return Exchange(parcel)
            }

            override fun newArray(size: Int): Array<Exchange?> {
                return arrayOfNulls(size)
            }
        }
    }
}

fun getParticipantsFromNames(participantNames: List<String>, onComplete: (List<Participant>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("Usuarios")
        .whereIn("nombre", participantNames)
        .get()
        .addOnSuccessListener { documents ->
            val participants = documents.map { doc ->
                Participant(
                    nombre = doc.getString("nombre") ?: "",
                    correo = doc.getString("correo") ?: "",
                    alias = doc.getString("alias") ?: ""
                )
            }
            onComplete(participants)
        }
        .addOnFailureListener {
            onComplete(emptyList())
        }
}
