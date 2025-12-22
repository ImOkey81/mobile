package aeza.hostmaster.mobilehost.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import aeza.hostmaster.mobilehost.presentation.auth.AuthScreen
import aeza.hostmaster.mobilehost.presentation.auth.AuthViewModel
import aeza.hostmaster.mobilehost.presentation.checks.CheckViewModel
import aeza.hostmaster.mobilehost.presentation.checks.ChecksScreen

private const val AUTH_ROUTE = "auth"
private const val CHECKS_ROUTE = "checks"

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    checkViewModel: CheckViewModel
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val checksState by checkViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate(CHECKS_ROUTE) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = AUTH_ROUTE) {
        composable(AUTH_ROUTE) {
            AuthScreen(
                state = authState,
                onUsernameChange = authViewModel::onUsernameChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onSubmit = authViewModel::submit
            )
        }

        composable(CHECKS_ROUTE) {
            ChecksScreen(
                state = checksState,
                onTabSelected = checkViewModel::updateSelectedTab,
                onHttpTargetChange = checkViewModel::updateHttpTarget,
                onPingTargetChange = checkViewModel::updatePingTarget,
                onTcpHostChange = checkViewModel::updateTcpHost,
                onTraceTargetChange = checkViewModel::updateTraceTarget,
                onDnsTargetChange = checkViewModel::updateDnsTarget,
                onSubmit = checkViewModel::submitCurrentCheck,
                onRefreshJob = checkViewModel::refreshJob
            )
        }
    }
}
