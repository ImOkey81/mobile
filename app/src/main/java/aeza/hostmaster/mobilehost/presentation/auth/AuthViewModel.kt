package aeza.hostmaster.mobilehost.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aeza.hostmaster.mobilehost.domain.usecase.ObserveCredentialsUseCase
import aeza.hostmaster.mobilehost.domain.usecase.SaveCredentialsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isAuthenticated: Boolean = false
)

class AuthViewModel(
    private val saveCredentials: SaveCredentialsUseCase,
    private val observeCredentials: ObserveCredentialsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        viewModelScope.launch {
            observeCredentials().collect { credentials ->
                _uiState.value = _uiState.value.copy(
                    username = credentials?.username ?: _uiState.value.username,
                    password = credentials?.password ?: _uiState.value.password,
                    isAuthenticated = credentials != null
                )
            }
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun submit() {
        viewModelScope.launch {
            saveCredentials(_uiState.value.username.trim(), _uiState.value.password)
        }
    }
}
