package com.example.primecare.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.model.AdminDashboardStats
import kotlinx.coroutines.launch

sealed class AdminDashboardUiState {
    object Loading : AdminDashboardUiState()
    data class Loaded(val stats: AdminDashboardStats) : AdminDashboardUiState()
    data class Error(val message: String) : AdminDashboardUiState()
}

class AdminDashboardViewModel(private val repository: ClinicRepository) : ViewModel() {

    private val _uiState = MutableLiveData<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: LiveData<AdminDashboardUiState> = _uiState

    fun loadStats() {
        viewModelScope.launch {
            when (val result = repository.adminStats()) {
                is DataResult.Success -> _uiState.value = AdminDashboardUiState.Loaded(result.data)
                is DataResult.Error -> _uiState.value = AdminDashboardUiState.Error(result.message)
            }
        }
    }

    class Factory(private val repository: ClinicRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminDashboardViewModel::class.java)) {
                return AdminDashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
