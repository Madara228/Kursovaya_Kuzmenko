package com.example.realmatch.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.example.realmatch.R
import com.example.realmatch.data.models.FeedUser
import com.example.realmatch.ui.components.OutlinedPillButton
import com.example.realmatch.ui.components.PrimaryButton
import com.example.realmatch.ui.theme.Background
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.ui.theme.TextSecondary
import com.example.realmatch.utils.mediaUrl

@Composable
fun MatchDialog(
    matched: FeedUser,
    myPhotoUrl: String?,
    onSayHello: () -> Unit,
    onDismiss: () -> Unit,
) {
    val matchedPhoto = (matched.photos.firstOrNull { it.isMain } ?: matched.photos.firstOrNull())
        ?.let { mediaUrl(it.url) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "ВЗАИМНАЯ СИМПАТИЯ",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = TextMuted,
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                ) {
                    MatchAvatar(
                        url = myPhotoUrl,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = (-46).dp),
                    )
                    MatchAvatar(
                        url = matchedPhoto,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 46.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Purple),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_heart),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Это мэтч!",
                    style = MaterialTheme.typography.displayMedium,
                    color = Purple,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Вы и ${matched.firstName} понравились друг другу.\nСамое время написать первым.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Поздороваться",
                    onClick = onSayHello,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedPillButton(
                    text = "Продолжить",
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun MatchAvatar(url: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(PurpleSoft)
            .border(4.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(44.dp),
            )
        }
    }
}
