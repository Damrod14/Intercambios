package com.example.proyecto

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ParticipantDialogFragment(
    private val participants: List<Participant>,
    private val onParticipantsSelected: (List<Participant>) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_available_users, null)

        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recycler_view_available_users)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val selectedParticipants = mutableListOf<Participant>()

        recyclerView.adapter = ParticipantsAdapterr(
            participants = participants,
            onParticipantClick = { participant ->
                if (selectedParticipants.contains(participant)) {
                    selectedParticipants.remove(participant)
                } else {
                    selectedParticipants.add(participant)
                }
            },
            isMultiSelect = true
        )


        builder.setView(dialogView)
            .setTitle("Seleccionar Participantes")
            .setPositiveButton("Añadir") { _, _ ->
                onParticipantsSelected(selectedParticipants)
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }
}
