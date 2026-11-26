package com.example.realmatch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.realmatch.R
import com.example.realmatch.data.models.PhotoDto
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.utils.mediaUrl
import kotlinx.coroutines.launch

@Composable
fun PhotoPager(
    photos: List<PhotoDto>,
    modifier: Modifier = Modifier,
    dotsAtTop: Boolean = false,
) {
    val pageCount = photos.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(PurpleSoft),
    ) {
        if (photos.isEmpty()) {
            Box(Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(36.dp),
                )
            }
            Text(
                text = "Нет фото",
                modifier = Modifier.align(Alignment.Center).padding(top = 56.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
            )
        } else {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                AsyncImage(
                    model = mediaUrl(photos[page].url),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (photos.size > 1) {
                PagerArrow(
                    iconRes = R.drawable.ic_chevron_left,
                    contentDescription = "Предыдущее фото",
                    modifier = Modifier.align(Alignment.CenterStart).padding(10.dp),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                        }
                    },
                )
                PagerArrow(
                    iconRes = R.drawable.ic_chevron_right,
                    contentDescription = "Следующее фото",
                    modifier = Modifier.align(Alignment.CenterEnd).padding(10.dp),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                (pagerState.currentPage + 1).coerceAtMost(photos.lastIndex)
                            )
                        }
                    },
                )
                Row(
                    modifier = Modifier
                        .align(if (dotsAtTop) Alignment.TopCenter else Alignment.BottomCenter)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.22f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    photos.indices.forEach { index ->
                        val active = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(if (active) 7.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.5f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PagerArrow(
    iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.85f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = Color(0xFF30303B),
            modifier = Modifier.size(20.dp),
        )
    }
}
