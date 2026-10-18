package com.example.primecare.ui.doctor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.primecare.databinding.ItemRepeatRequestBinding
import com.example.primecare.model.RepeatRequestDto

class RepeatRequestsAdapter(
    private val onDecision: (RepeatRequestDto, Boolean) -> Unit
) : ListAdapter<RepeatRequestDto, RepeatRequestsAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRepeatRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val b = holder.binding
        b.medicationText.text = item.dosage?.takeIf { it.isNotBlank() }
            ?.let { "${item.medication_name} — $it" } ?: item.medication_name
        b.patientText.text = "Requested by ${item.patientFullName}"
        b.approveButton.setOnClickListener { onDecision(item, true) }
        b.declineButton.setOnClickListener { onDecision(item, false) }
    }

    class ViewHolder(val binding: ItemRepeatRequestBinding) : RecyclerView.ViewHolder(binding.root)

    private object Diff : DiffUtil.ItemCallback<RepeatRequestDto>() {
        override fun areItemsTheSame(old: RepeatRequestDto, new: RepeatRequestDto) =
            old.request_id == new.request_id
        override fun areContentsTheSame(old: RepeatRequestDto, new: RepeatRequestDto) = old == new
    }
}
