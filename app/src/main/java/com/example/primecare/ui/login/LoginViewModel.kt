package com.example.primecare.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.primecare.data.local.StaffRole
import com.example.primecare.data.repository.AuthRepository
import com.example.primecare.data.repository.AuthResult
import com.example.primecare.util.Validators
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val role: StaffRole) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        val emailError = Validators.emailError(email)
        if (emailError != null) {
            _uiState.value = LoginUiState.Error(emailError)
            return
        }
        if (password.isEmpty()) {
            _uiState.value = LoginUiState.Error("Please enter your password.")
            return
        }

        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is AuthResult.Success -> _uiState.value = LoginUiState.Success(result.role)
                is AuthResult.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }

    /** Single sign-on: [idToken] comes from Google's Credential Manager. */
    fun googleLogin(idToken: String) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            when (val result = repository.googleLogin(idToken)) {
                is AuthResult.Success -> _uiState.value = LoginUiState.Success(result.role)
                is AuthResult.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
