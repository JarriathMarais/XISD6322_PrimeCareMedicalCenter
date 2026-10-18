package com.example.primecare.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.primecare.databinding.ItemCheckinBinding
import com.example.primecare.model.AppointmentDto
import com.example.primecare.util.FormatUtils

class CheckinAdapter(
    private val onCheckIn: (AppointmentDto) -> Unit
) : ListAdapter<AppointmentDto, CheckinAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCheckinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val b = holder.binding
        b.patientNameText.text = item.patientFullName
        b.timeReasonText.text = "${FormatUtils.formatTime(item.appointment_time)} · ${item.reason}"
        val canCheckIn = item.status == "Booked"
        b.checkinButton.text = if (canCheckIn) "Check In" else "Checked In"
        b.checkinButton.isEnabled = canCheckIn
        b.checkinButton.setOnClickListener { onCheckIn(item) }
    }

    class ViewHolder(val binding: ItemCheckinBinding) : RecyclerView.ViewHolder(binding.root)

    private object Diff : DiffUtil.ItemCallback<AppointmentDto>() {
        override fun areItemsTheSame(old: AppointmentDto, new: AppointmentDto) =
            old.appointment_id == new.appointment_id
        override fun areContentsTheSame(old: AppointmentDto, new: AppointmentDto) = old == new
    }
}
