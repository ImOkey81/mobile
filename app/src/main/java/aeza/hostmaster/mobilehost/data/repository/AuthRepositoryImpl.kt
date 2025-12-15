package aeza.hostmaster.mobilehost.data.repository

import aeza.hostmaster.mobilehost.domain.model.Credentials
import aeza.hostmaster.mobilehost.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl : AuthRepository {
    private val credentialsState = MutableStateFlow<Credentials?>(null)

    override val credentialsFlow: StateFlow<Credentials?> = credentialsState

    override suspend fun saveCredentials(credentials: Credentials) {
        credentialsState.value = credentials
    }

    override fun currentCredentials(): Credentials? = credentialsState.value
}
