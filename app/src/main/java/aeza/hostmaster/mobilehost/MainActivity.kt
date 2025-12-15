package aeza.hostmaster.mobilehost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import aeza.hostmaster.mobilehost.di.AppModule
import aeza.hostmaster.mobilehost.presentation.auth.AuthViewModel
import aeza.hostmaster.mobilehost.presentation.checks.CheckViewModel
import aeza.hostmaster.mobilehost.presentation.navigation.AppNavHost
import aeza.hostmaster.mobilehost.presentation.theme.MobileHostTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileHostApp()
        }
    }
}

@Composable
private fun MobileHostApp() {
    MobileHostTheme {
        val navController = rememberNavController()

        val authViewModel: AuthViewModel = viewModel(
            factory = AppModule.authViewModelFactory()
        )

        val checkViewModel: CheckViewModel = viewModel(
            factory = AppModule.checkViewModelFactory()
        )

        AppNavHost(
            navController = navController,
            authViewModel = authViewModel,
            checkViewModel = checkViewModel
        )
    }
}

