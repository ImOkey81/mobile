package aeza.hostmaster.mobilehost.domain.repository

import aeza.hostmaster.mobilehost.domain.model.Credentials
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val credentialsFlow: StateFlow<Credentials?>
    suspend fun saveCredentials(credentials: Credentials)
    fun currentCredentials(): Credentials?
}
