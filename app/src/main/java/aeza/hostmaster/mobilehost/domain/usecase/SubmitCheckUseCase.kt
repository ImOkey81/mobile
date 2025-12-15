package aeza.hostmaster.mobilehost.domain.usecase

import aeza.hostmaster.mobilehost.domain.model.CheckRequest
import aeza.hostmaster.mobilehost.domain.model.ServerCheckResult
import aeza.hostmaster.mobilehost.domain.repository.CheckRepository

class SubmitCheckUseCase(private val repository: CheckRepository) {
    suspend operator fun invoke(request: CheckRequest): ServerCheckResult {
        return repository.submitCheck(request)
    }
}
