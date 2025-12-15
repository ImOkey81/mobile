package aeza.hostmaster.mobilehost.domain.usecase

import aeza.hostmaster.mobilehost.domain.model.Credentials
import aeza.hostmaster.mobilehost.domain.repository.AuthRepository

class SaveCredentialsUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String) {
        repository.saveCredentials(Credentials(username, password))
    }
}
