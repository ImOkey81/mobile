package aeza.hostmaster.mobilehost.domain.usecase

import aeza.hostmaster.mobilehost.domain.model.ServerCheckResult
import aeza.hostmaster.mobilehost.domain.repository.CheckRepository

class FetchJobUseCase(private val repository: CheckRepository) {
    suspend operator fun invoke(jobId: String): ServerCheckResult {
        return repository.fetchJob(jobId)
    }
}
