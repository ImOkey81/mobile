package aeza.hostmaster.mobilehost.data.repository

import aeza.hostmaster.mobilehost.data.remote.NetworkClient
import aeza.hostmaster.mobilehost.domain.model.CheckRequest
import aeza.hostmaster.mobilehost.domain.model.ServerCheckResult
import aeza.hostmaster.mobilehost.domain.repository.AuthRepository
import aeza.hostmaster.mobilehost.domain.repository.CheckRepository

class CheckRepositoryImpl(
    private val networkClient: NetworkClient,
    private val authRepository: AuthRepository
) : CheckRepository {

    override suspend fun submitCheck(request: CheckRequest): ServerCheckResult {
        val credentials = authRepository.currentCredentials()
            ?: return ServerCheckResult(statusCode = null, jobId = null, body = null, error = "Укажите логин и пароль")
        return networkClient.submitCheck(credentials, request)
    }

    override suspend fun fetchJob(jobId: String): ServerCheckResult {
        val credentials = authRepository.currentCredentials()
            ?: return ServerCheckResult(statusCode = null, jobId = null, body = null, error = "Укажите логин и пароль")
        return networkClient.fetchJob(credentials, jobId)
    }
}
