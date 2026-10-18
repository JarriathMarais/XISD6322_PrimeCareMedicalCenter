package com.example.primecare.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.primecare.databinding.ItemAppointmentBinding
import com.example.primecare.model.AppointmentDto
import com.example.primecare.util.FormatUtils

class AppointmentsAdapter : ListAdapter<AppointmentDto, AppointmentsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemAppointmentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appointment: AppointmentDto) {
            binding.timeText.text = FormatUtils.formatTime(appointment.appointment_time)
            binding.patientNameText.text = appointment.patientFullName
            binding.reasonText.text = appointment.reason
            binding.doctorDeptText.text =
                "${FormatUtils.doctorOrUnassigned(appointment.doctorFullName)} · ${appointment.department_name ?: "General"}"
            FormatUtils.applyStatusPill(binding.statusPill, appointment)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<AppointmentDto>() {
        override fun areItemsTheSame(old: AppointmentDto, new: AppointmentDto) =
            old.appointment_id == new.appointment_id
        override fun areContentsTheSame(old: AppointmentDto, new: AppointmentDto) = old == new
    }
}
