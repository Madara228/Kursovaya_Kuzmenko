package com.example.realmatch.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.realmatch.R
import com.example.realmatch.data.local.UserRole
import com.example.realmatch.ui.components.AuthTextField
import com.example.realmatch.ui.components.AuthTopBar
import com.example.realmatch.ui.components.FormError
import com.example.realmatch.ui.components.PrimaryButton
import com.example.realmatch.ui.theme.Background
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextSecondary

@Composable
fun EsiaLoginScreen(
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.loggedInRole) {
        if (state.loggedInRole != null) onLoggedIn()
    }

    val submit = {
        keyboard?.hide()
        viewModel.login(login, password, UserRole.USER)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding()
            .imePadding(),
    ) {
        AuthTopBar(title = "Вход через Госуслуги", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(PurpleSoft, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shield_check),
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(34.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "госуслуги",
                style = MaterialTheme.typography.displayMedium,
                color = Purple,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Единая система идентификации\nи аутентификации",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(36.dp))

            AuthTextField(
                label = "Логин",
                value = login,
                onValueChange = { login = it },
                placeholder = "Введите логин",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(18.dp))
            AuthTextField(
                label = "Пароль",
                value = password,
                onValueChange = { password = it },
                placeholder = "Введите пароль",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
                imeAction = ImeAction.Done,
                onDone = submit,
            )
            FormError(state.error)
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Войти",
                onClick = submit,
                isLoading = state.isLoading,
            )
            Spacer(Modifier.height(24.dp))
        }

        DemoInfoCard(
            text = "Это имитация Госуслуг для демонстрации. Регистрация аккаунтов происходит через базу данных",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun DemoInfoCard(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PurpleSoft, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_shield_check),
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
    }
}
