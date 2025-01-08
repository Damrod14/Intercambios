package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsersAdapter(
    private val users: List<User>,
    private val onUserChecked: (User, Boolean) -> Unit
) : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.user_name)
        val userCheckbox: CheckBox = view.findViewById(R.id.user_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.userName.text = user.nombre

        // Configurar la casilla de verificación
        holder.userCheckbox.setOnCheckedChangeListener(null) // Evitar comportamientos inesperados por reciclaje
        holder.userCheckbox.isChecked = false
        holder.userCheckbox.setOnCheckedChangeListener { _, isChecked ->
            onUserChecked(user, isChecked)
        }
    }

    override fun getItemCount() = users.size
}
