package aeza.hostmaster.mobilehost.di

import aeza.hostmaster.mobilehost.data.remote.NetworkClient
import aeza.hostmaster.mobilehost.data.repository.AuthRepositoryImpl
import aeza.hostmaster.mobilehost.data.repository.CheckRepositoryImpl
import aeza.hostmaster.mobilehost.domain.usecase.FetchJobUseCase
import aeza.hostmaster.mobilehost.domain.usecase.ObserveCredentialsUseCase
import aeza.hostmaster.mobilehost.domain.usecase.SaveCredentialsUseCase
import aeza.hostmaster.mobilehost.domain.usecase.SubmitCheckUseCase
import aeza.hostmaster.mobilehost.presentation.auth.AuthViewModel
import aeza.hostmaster.mobilehost.presentation.checks.CheckViewModel

object AppModule {
    private val networkClient by lazy { NetworkClient() }
    private val authRepository by lazy { AuthRepositoryImpl() }
    private val checkRepository by lazy { CheckRepositoryImpl(networkClient, authRepository) }

    private val saveCredentialsUseCase by lazy { SaveCredentialsUseCase(authRepository) }
    private val observeCredentialsUseCase by lazy { ObserveCredentialsUseCase(authRepository) }
    private val submitCheckUseCase by lazy { SubmitCheckUseCase(checkRepository) }
    private val fetchJobUseCase by lazy { FetchJobUseCase(checkRepository) }

    fun authViewModelFactory() = viewModelFactory {
        AuthViewModel(
            saveCredentials = saveCredentialsUseCase,
            observeCredentials = observeCredentialsUseCase
        )
    }

    fun checkViewModelFactory() = viewModelFactory {
        CheckViewModel(
            submitCheck = submitCheckUseCase,
            fetchJob = fetchJobUseCase,
            observeCredentials = observeCredentialsUseCase
        )
    }
}
