package com.example.realmatch.ui.feed

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.realmatch.R
import com.example.realmatch.data.models.FeedUser
import com.example.realmatch.ui.components.OutlinedPillButton
import com.example.realmatch.ui.components.PhotoPager
import com.example.realmatch.ui.theme.Error
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.ui.theme.TextPrimary
import com.example.realmatch.ui.theme.TextSecondary
import com.example.realmatch.utils.calculateAge
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    onOpenChats: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Лента",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            val current = state.current
            when {
                current != null -> key(current.id) {
                    SwipeCard(
                        user = current,
                        enabled = !state.isSwiping,
                        onSwipe = { action ->
                            if (action == ACTION_LIKE) viewModel.like() else viewModel.dislike()
                        },
                    )
                }

                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Purple,
                )

                state.error != null -> FeedError(
                    message = state.error.orEmpty(),
                    modifier = Modifier.align(Alignment.Center),
                    onRetry = viewModel::load,
                )

                else -> EmptyFeed(
                    modifier = Modifier.align(Alignment.Center),
                    onRefresh = viewModel::load,
                )
            }
        }

        if (state.error != null && state.current != null) {
            Text(
                text = state.error.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Error,
                textAlign = TextAlign.Center,
            )
        }

        val actionsEnabled = state.current != null && !state.isSwiping
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = viewModel::dislike,
                enabled = actionsEnabled,
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Дизлайк",
                        tint = if (actionsEnabled) TextSecondary else TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(Modifier.width(28.dp))
            Surface(
                onClick = viewModel::like,
                enabled = actionsEnabled,
                modifier = Modifier.size(74.dp),
                shape = CircleShape,
                color = Purple,
                shadowElevation = 8.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_heart),
                        contentDescription = "Лайк",
                        tint = if (actionsEnabled) Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
    }

    state.matchUser?.let { matched ->
        MatchDialog(
            matched = matched,
            myPhotoUrl = state.myPhotoUrl,
            onSayHello = {
                viewModel.dismissMatch()
                onOpenChats(matched.id)
            },
            onDismiss = viewModel::dismissMatch,
        )
    }
}

@Composable
private fun SwipeCard(
    user: FeedUser,
    enabled: Boolean,
    onSwipe: (String) -> Unit,
) {
    val offsetX = remember(user.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var busy by remember(user.id) { mutableStateOf(false) }
    val threshold = with(LocalDensity.current) { 96.dp.toPx() }
    val age = calculateAge(user.birthDate)

    fun finish(action: String) {
        if (busy) return
        busy = true
        val direction = if (action == ACTION_LIKE) 1f else -1f
        scope.launch {
            offsetX.animateTo(direction * 1600f, tween(220))
            onSwipe(action)
        }
    }

    val dragModifier = if (enabled) {
        Modifier.pointerInput(user.id) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                },
                onDragEnd = {
                    when {
                        offsetX.value > threshold -> finish(ACTION_LIKE)
                        offsetX.value < -threshold -> finish(ACTION_DISLIKE)
                        else -> scope.launch { offsetX.animateTo(0f, spring()) }
                    }
                },
                onDragCancel = { scope.launch { offsetX.animateTo(0f, spring()) } },
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = offsetX.value / 70f
            }
            .then(dragModifier)
            .clip(RoundedCornerShape(28.dp))
            .background(PurpleSoft),
    ) {
        PhotoPager(
            photos = user.photos,
            modifier = Modifier.fillMaxSize(),
            dotsAtTop = true,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.78f),
                    )
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = if (age != null) "${user.firstName}, $age" else user.firstName,
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
            )
            user.city?.takeIf { it.isNotBlank() }?.let { city ->
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_location),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
            user.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FeedError(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier.padding(24.dp),
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
            text = "Не удалось загрузить ленту",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedPillButton(
            text = "Повторить",
            onClick = onRetry,
            modifier = Modifier.width(200.dp),
        )
    }
}

@Composable
private fun EmptyFeed(modifier: Modifier = Modifier, onRefresh: () -> Unit) {
    Column(
        modifier = modifier.padding(24.dp),
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
            text = "Анкеты закончились",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Загляните позже — новые анкеты появятся, когда зарегистрируются другие пользователи.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedPillButton(
            text = "Обновить",
            onClick = onRefresh,
            modifier = Modifier.width(200.dp),
        )
    }
}
