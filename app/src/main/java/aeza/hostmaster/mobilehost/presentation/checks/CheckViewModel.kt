package aeza.hostmaster.mobilehost.presentation.checks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aeza.hostmaster.mobilehost.domain.model.CheckRequest
import aeza.hostmaster.mobilehost.domain.model.ServerCheckResult
import aeza.hostmaster.mobilehost.domain.usecase.FetchJobUseCase
import aeza.hostmaster.mobilehost.domain.usecase.ObserveCredentialsUseCase
import aeza.hostmaster.mobilehost.domain.usecase.SubmitCheckUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckUiState(
    val selectedTab: CheckTab = CheckTab.HTTP,
    val httpTarget: String = "https://example.com",
    val pingTarget: String = "1.1.1.1",
    val tcpHost: String = "scanme.nmap.org",
    val tcpPort: String = "80",
    val traceTarget: String = "google.com",
    val dnsTarget: String = "example.com",
    val loading: Boolean = false,
    val result: ServerCheckResult? = null,
    val errorMessage: String? = null,
    val credentialsMissing: Boolean = true
)

class CheckViewModel(
    private val submitCheck: SubmitCheckUseCase,
    private val fetchJob: FetchJobUseCase,
    observeCredentials: ObserveCredentialsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckUiState())
    val uiState: StateFlow<CheckUiState> = _uiState

    init {
        viewModelScope.launch {
            observeCredentials().collect { credentials ->
                _uiState.update { state ->
                    state.copy(
                        credentialsMissing = credentials == null,
                        errorMessage = state.errorMessage?.takeUnless { credentials == null }
                    )
                }
            }
        }
    }

    fun updateSelectedTab(tab: CheckTab) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
    }

    fun updateHttpTarget(value: String) = _uiState.update { it.copy(httpTarget = value) }
    fun updatePingTarget(value: String) = _uiState.update { it.copy(pingTarget = value) }
    fun updateTcpHost(value: String) = _uiState.update { it.copy(tcpHost = value) }
    fun updateTcpPort(value: String) = _uiState.update { it.copy(tcpPort = value) }
    fun updateTraceTarget(value: String) = _uiState.update { it.copy(traceTarget = value) }
    fun updateDnsTarget(value: String) = _uiState.update { it.copy(dnsTarget = value) }

    fun submitCurrentCheck() {
        val request = buildRequest() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            val result = submitCheck(request)
            _uiState.update { it.copy(loading = false, result = result, errorMessage = result.error) }
        }
    }

    fun refreshJob(jobId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            val result = fetchJob(jobId)
            _uiState.update { it.copy(loading = false, result = result, errorMessage = result.error) }
        }
    }

    private fun buildRequest(): CheckRequest? {
        val state = _uiState.value
        if (state.credentialsMissing) {
            _uiState.update { it.copy(errorMessage = "Сначала введите логин и пароль") }
            return null
        }

        return when (state.selectedTab) {
            CheckTab.HTTP -> CheckRequest(type = CheckTab.HTTP.type, target = state.httpTarget)
            CheckTab.PING -> CheckRequest(type = CheckTab.PING.type, target = state.pingTarget)
            CheckTab.TRACE -> CheckRequest(type = CheckTab.TRACE.type, target = state.traceTarget)
            CheckTab.DNS -> CheckRequest(type = CheckTab.DNS.type, target = state.dnsTarget)
            CheckTab.TCP -> {
                val port = state.tcpPort.toIntOrNull()
                if (port == null || port <= 0) {
                    _uiState.update { it.copy(errorMessage = "Введите корректный порт") }
                    null
                } else {
                    CheckRequest(type = CheckTab.TCP.type, target = state.tcpHost, port = port)
                }
            }
        }
    }
}
