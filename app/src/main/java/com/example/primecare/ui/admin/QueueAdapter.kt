package com.example.primecare.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.primecare.databinding.ItemQueueBinding
import com.example.primecare.model.AppointmentDto

class QueueAdapter : ListAdapter<AppointmentDto, QueueAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQueueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.queueNumberText.text = item.queueLabel ?: "—"
        holder.binding.patientNameText.text = item.patientFullName
        holder.binding.doctorText.text = item.doctorFullName
            ?.let { "Waiting for $it" } ?: "Waiting for doctor assignment"
    }

    class ViewHolder(val binding: ItemQueueBinding) : RecyclerView.ViewHolder(binding.root)

    private object Diff : DiffUtil.ItemCallback<AppointmentDto>() {
        override fun areItemsTheSame(old: AppointmentDto, new: AppointmentDto) =
            old.appointment_id == new.appointment_id
        override fun areContentsTheSame(old: AppointmentDto, new: AppointmentDto) = old == new
    }
}
