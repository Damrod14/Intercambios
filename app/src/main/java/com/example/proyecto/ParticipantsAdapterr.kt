package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ParticipantsAdapterr(
    private val participants: List<Participant>,
    private val onParticipantClick: (Participant) -> Unit,
    private val isMultiSelect: Boolean = false
) : RecyclerView.Adapter<ParticipantsAdapterr.ViewHolder>() {

    private val selectedParticipants = mutableSetOf<Participant>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.participant_name)
        val checkBox: CheckBox = view.findViewById(R.id.participant_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant = participants[position]
        holder.name.text = participant.nombre

        if (isMultiSelect) {
            holder.checkBox.visibility = View.VISIBLE
            holder.checkBox.isChecked = selectedParticipants.contains(participant)
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedParticipants.add(participant)
                else selectedParticipants.remove(participant)
            }
        } else {
            holder.checkBox.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onParticipantClick(participant) }
    }

    override fun getItemCount(): Int = participants.size

    fun getSelectedParticipants(): List<Participant> = selectedParticipants.toList()
}

