package aeza.hostmaster.mobilehost.domain.usecase

import aeza.hostmaster.mobilehost.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveCredentialsUseCase(private val repository: AuthRepository) {
    operator fun invoke(): StateFlow<aeza.hostmaster.mobilehost.domain.model.Credentials?> = repository.credentialsFlow
}
