package com.example.realmatch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.realmatch.data.api.SessionEvents
import com.example.realmatch.data.local.TokenManager
import com.example.realmatch.data.local.UserRole
import com.example.realmatch.ui.auth.EsiaLoginScreen
import com.example.realmatch.ui.auth.PartnerLoginScreen
import com.example.realmatch.ui.auth.WelcomeScreen
import com.example.realmatch.ui.main.MainScreen
import com.example.realmatch.ui.partner.PartnerCabinetScreen

object Routes {
    const val WELCOME = "welcome"
    const val USER_LOGIN = "user_login"
    const val PARTNER_LOGIN = "partner_login"
    const val USER_HOME = "user_home"
    const val PARTNER_HOME = "partner_home"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val tokens = remember { TokenManager.getInstance(context) }
    // новый пользователь попадает в профиль, чтобы заполнить анкету
    var startOnProfile by remember { mutableStateOf(tokens.consumeOnboardingPending()) }
    val startDestination = remember {
        when {
            !tokens.isLoggedIn -> Routes.WELCOME
            tokens.role == UserRole.PARTNER -> Routes.PARTNER_HOME
            else -> Routes.USER_HOME
        }
    }
    val navController = rememberNavController()

    fun goHome(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun logout() {
        tokens.clear()
        goHome(Routes.WELCOME)
    }

    LaunchedEffect(Unit) {
        SessionEvents.unauthorized.collect {
            if (tokens.isLoggedIn) {
                tokens.clear()
                Toast.makeText(context, "Сессия истекла, войдите снова", Toast.LENGTH_SHORT).show()
                goHome(Routes.WELCOME)
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onUserClick = { navController.navigate(Routes.USER_LOGIN) },
                onPartnerClick = { navController.navigate(Routes.PARTNER_LOGIN) },
            )
        }
        composable(Routes.USER_LOGIN) {
            EsiaLoginScreen(
                onBack = { navController.popBackStack() },
                onLoggedIn = {
                    startOnProfile = tokens.consumeOnboardingPending()
                    goHome(Routes.USER_HOME)
                },
            )
        }
        composable(Routes.PARTNER_LOGIN) {
            PartnerLoginScreen(
                onBack = { navController.popBackStack() },
                onLoggedIn = { goHome(Routes.PARTNER_HOME) },
            )
        }
        composable(Routes.USER_HOME) {
            MainScreen(onLogout = { logout() }, startOnProfile = startOnProfile)
        }
        composable(Routes.PARTNER_HOME) {
            PartnerCabinetScreen(onLogout = { logout() })
        }
    }
}
