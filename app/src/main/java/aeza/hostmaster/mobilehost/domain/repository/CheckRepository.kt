package aeza.hostmaster.mobilehost.domain.repository

import aeza.hostmaster.mobilehost.domain.model.CheckRequest
import aeza.hostmaster.mobilehost.domain.model.ServerCheckResult

interface CheckRepository {
    suspend fun submitCheck(request: CheckRequest): ServerCheckResult
    suspend fun fetchJob(jobId: String): ServerCheckResult
}
