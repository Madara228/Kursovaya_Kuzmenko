package com.example.realmatch.ui.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.realmatch.R
import com.example.realmatch.data.models.MatchItem
import com.example.realmatch.data.models.PublicUserProfile
import com.example.realmatch.ui.components.OutlinedPillButton
import com.example.realmatch.ui.components.UserProfileDetails
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.ui.theme.TextPrimary
import com.example.realmatch.ui.theme.TextSecondary
import com.example.realmatch.utils.mediaUrl

@Composable
fun MatchesScreen(
    modifier: Modifier = Modifier,
    viewModel: MatchesViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    if (state.selectedUserId != null) {
        MatchDetail(
            state = state,
            onBack = viewModel::closeUser,
            onRetry = viewModel::retryDetail,
        )
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Метчи",
            modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 8.dp),
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
        )

        when {
            state.isLoading && state.matches.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Purple)
            }

            state.error != null && state.matches.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedPillButton(
                    text = "Повторить",
                    onClick = viewModel::load,
                    modifier = Modifier.width(200.dp),
                )
            }

            state.matches.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_heart),
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Пока нет мэтчей",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Свайпните вправо в ленте. Если симпатия взаимна — пользователь появится здесь.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 12.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.matches, key = { it.id }) { match ->
                    MatchRow(
                        match = match,
                        onClick = { viewModel.openUser(match.user.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchRow(match: MatchItem, onClick: () -> Unit) {
    val mainPhoto = (match.user.photos.firstOrNull { it.isMain } ?: match.user.photos.firstOrNull())
        ?.let { mediaUrl(it.url) }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(PurpleSoft),
                contentAlignment = Alignment.Center,
            ) {
                if (mainPhoto != null) {
                    AsyncImage(
                        model = mainPhoto,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_person),
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.user.firstName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Взаимная симпатия",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun MatchDetail(
    state: MatchesUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Назад",
                    tint = TextPrimary,
                )
            }
            Text(
                text = "Профиль",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        }

        val detail = state.detail
        when {
            state.isDetailLoading && detail == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Purple)
            }

            detail == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.detailError ?: "Не удалось загрузить профиль",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onRetry) {
                    Text("Повторить", color = Purple)
                }
            }

            else -> UserProfileDetails(detail)
        }
    }
}

