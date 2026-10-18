package com.example.primecare.ui.doctor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.primecare.databinding.ItemQueueBinding
import com.example.primecare.model.AppointmentDto

/** The doctor's own checked-in patients; tapping one opens the consultation. */
class DoctorQueueAdapter(
    private val onClick: (AppointmentDto) -> Unit
) : ListAdapter<AppointmentDto, DoctorQueueAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQueueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val b = holder.binding
        b.queueNumberText.text = item.queueLabel ?: "—"
        b.patientNameText.text = item.patientFullName
        val state = if (item.in_consultation) "In consultation" else "Waiting"
        b.doctorText.text = "${item.reason} · $state"
        b.root.setOnClickListener { onClick(item) }
    }

    class ViewHolder(val binding: ItemQueueBinding) : RecyclerView.ViewHolder(binding.root)

    private object Diff : DiffUtil.ItemCallback<AppointmentDto>() {
        override fun areItemsTheSame(old: AppointmentDto, new: AppointmentDto) =
            old.appointment_id == new.appointment_id
        override fun areContentsTheSame(old: AppointmentDto, new: AppointmentDto) = old == new
    }
}
