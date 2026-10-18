package com.example.primecare.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.primecare.data.repository.AppointmentRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.model.AppointmentDto
import kotlinx.coroutines.launch

sealed class AppointmentsListUiState {
    object Loading : AppointmentsListUiState()
    data class Loaded(val appointments: List<AppointmentDto>) : AppointmentsListUiState()
    data class Error(val message: String) : AppointmentsListUiState()
}

class AppointmentsListViewModel(private val repository: AppointmentRepository) : ViewModel() {

    private val _uiState = MutableLiveData<AppointmentsListUiState>(AppointmentsListUiState.Loading)
    val uiState: LiveData<AppointmentsListUiState> = _uiState

    init {
        load()
    }

    fun load() {
        _uiState.value = AppointmentsListUiState.Loading
        viewModelScope.launch {
            when (val result = repository.getTodayAppointments()) {
                is DataResult.Success -> _uiState.value = AppointmentsListUiState.Loaded(result.data)
                is DataResult.Error -> _uiState.value = AppointmentsListUiState.Error(result.message)
            }
        }
    }

    class Factory(private val repository: AppointmentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppointmentsListViewModel::class.java)) {
                return AppointmentsListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
