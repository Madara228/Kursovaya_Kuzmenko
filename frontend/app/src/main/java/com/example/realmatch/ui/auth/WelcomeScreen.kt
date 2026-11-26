package com.example.realmatch.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.realmatch.R
import com.example.realmatch.ui.components.OutlinedPillButton
import com.example.realmatch.ui.components.PrimaryButton
import com.example.realmatch.ui.theme.Background
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.ui.theme.TextPrimary
import com.example.realmatch.ui.theme.TextSecondary

@Composable
fun WelcomeScreen(
    onUserClick: () -> Unit,
    onPartnerClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_heart),
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "RealMatch",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "НАСТОЯЩИЕ ЛЮДИ. НАСТОЯЩИЕ ЧУВСТВА.",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = TextMuted,
        )
        Spacer(Modifier.height(18.dp))
        HeroCollage()
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Настоящие люди.\nНастоящие чувства.",
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Знакомьтесь с теми, кто рядом.\nНачните с безопасного входа.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
        )
        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            text = "Войти как пользователь",
            onClick = onUserClick,
            iconRes = R.drawable.ic_shield_check,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedPillButton(
            text = "Вход для партнёров",
            onClick = onPartnerClick,
            iconRes = R.drawable.ic_storefront,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Ваши данные под надёжной защитой",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HeroCollage() {
    val photoShape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .background(PurpleSoft, RoundedCornerShape(150.dp)),
        )
        Image(
            painter = painterResource(R.drawable.welcome_sofia),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-52).dp, y = (-14).dp)
                .width(178.dp)
                .aspectRatio(0.747f)
                .rotate(-8f)
                .clip(photoShape)
                .border(4.dp, Color.White, photoShape),
        )
        Image(
            painter = painterResource(R.drawable.welcome_alex),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 56.dp, y = 20.dp)
                .width(168.dp)
                .aspectRatio(0.747f)
                .rotate(7f)
                .clip(photoShape)
                .border(4.dp, Color.White, photoShape),
        )
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 10.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shield_check),
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Знакомства с доверием",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )
            }
        }
    }
}
