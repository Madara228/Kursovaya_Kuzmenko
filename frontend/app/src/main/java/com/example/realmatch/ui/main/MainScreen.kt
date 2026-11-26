package com.example.realmatch.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.realmatch.R
import com.example.realmatch.ui.chats.ChatsScreen
import com.example.realmatch.ui.feed.FeedScreen
import com.example.realmatch.ui.matches.MatchesScreen
import com.example.realmatch.ui.profile.ProfileScreen
import com.example.realmatch.ui.theme.Background
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.ui.theme.TextSecondary

private enum class MainTab(val label: String, val icon: Int) {
    FEED("Лента", R.drawable.ic_explore),
    MATCHES("Метчи", R.drawable.ic_heart),
    CHATS("Чаты", R.drawable.ic_chat),
    PROFILE("Профиль", R.drawable.ic_person),
}

@Composable
fun MainScreen(onLogout: () -> Unit, startOnProfile: Boolean = false) {
    var selectedTab by rememberSaveable {
        mutableStateOf(if (startOnProfile) MainTab.PROFILE else MainTab.FEED)
    }
    // после первого входа нового пользователя всегда открываем профиль,
    // даже если saved state восстановил другую вкладку
    LaunchedEffect(Unit) {
        if (startOnProfile) selectedTab = MainTab.PROFILE
    }
    var pendingChatUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var chatOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (!chatOpen) {
                MainBottomBar(selected = selectedTab, onSelect = { selectedTab = it })
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (selectedTab) {
                MainTab.FEED -> FeedScreen(
                    onOpenChats = { userId ->
                        pendingChatUserId = userId
                        selectedTab = MainTab.CHATS
                    },
                )

                MainTab.MATCHES -> MatchesScreen()

                MainTab.CHATS -> ChatsScreen(
                    openUserId = pendingChatUserId,
                    onOpenHandled = { pendingChatUserId = null },
                    onChatOpenChanged = { chatOpen = it },
                )

                MainTab.PROFILE -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun MainBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                MainTab.entries.forEach { tab ->
                    val active = tab == selected
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (active) PurpleSoft else Color.Transparent)
                            .clickable { onSelect(tab) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(tab.icon),
                            contentDescription = tab.label,
                            tint = if (active) Purple else TextMuted,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) Purple else TextSecondary,
                        )
                    }
                }
            }
        }
    }
}
