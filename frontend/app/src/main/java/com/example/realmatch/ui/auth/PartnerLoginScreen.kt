package com.example.realmatch.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.ui.theme.TextPrimary
import com.example.realmatch.ui.theme.TextSecondary

@Composable
fun PartnerLoginScreen(
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
        viewModel.login(login, password, UserRole.PARTNER)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding()
            .imePadding(),
    ) {
        AuthTopBar(title = "Кабинет партнёра", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            PartnerHero()
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Место для настоящих встреч",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Управляйте заведением и предлагайте\nгостям больше приятных поводов прийти.",
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
                text = "Войти в кабинет",
                onClick = submit,
                isLoading = state.isLoading,
            )
            Spacer(Modifier.height(24.dp))
        }

        Text(
            text = "RealMatch · Для бизнеса",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = TextMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PartnerHero() {
    Box(modifier = Modifier.height(140.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(PurpleSoft, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_storefront),
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(46.dp),
            )
        }
        Surface(
            modifier = Modifier
                .padding(start = 76.dp, top = 46.dp)
                .size(74.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_heart),
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}
