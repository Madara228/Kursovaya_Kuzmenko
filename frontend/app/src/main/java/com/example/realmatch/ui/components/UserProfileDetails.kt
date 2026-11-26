package com.example.realmatch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.realmatch.R
import com.example.realmatch.data.models.PublicUserProfile
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.ui.theme.TextPrimary
import com.example.realmatch.ui.theme.TextSecondary
import com.example.realmatch.utils.calculateAge
import com.example.realmatch.utils.formatBirthDate
import com.example.realmatch.utils.genderLabel

@Composable
fun UserProfileDetails(
    profile: PublicUserProfile,
    modifier: Modifier = Modifier,
) {
    val age = calculateAge(profile.birthDate)
    val meta = listOfNotNull(formatBirthDate(profile.birthDate), genderLabel(profile.gender))
        .joinToString(" · ")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        PhotoPager(
            photos = profile.photos,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = if (age != null) "${profile.firstName} ${profile.lastName}, $age" else
                "${profile.firstName} ${profile.lastName}",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = meta,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(PurpleSoft)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_heart),
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Взаимная симпатия",
                style = MaterialTheme.typography.labelMedium,
                color = Purple,
            )
        }
        if (!profile.city.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = profile.city,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "О себе",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = profile.bio?.takeIf { it.isNotBlank() } ?: "Пользователь пока ничего не рассказал о себе",
            style = MaterialTheme.typography.bodyLarge,
            color = if (profile.bio.isNullOrBlank()) TextMuted else TextPrimary,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(24.dp))
    }
}
