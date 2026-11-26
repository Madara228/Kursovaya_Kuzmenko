package com.example.realmatch.ui.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.realmatch.R
import com.example.realmatch.data.models.ChatListItem
import com.example.realmatch.data.models.ChatMessage
import com.example.realmatch.data.models.InvitePreview
import com.example.realmatch.data.models.Offer
import com.example.realmatch.data.models.OfferPartner
import com.example.realmatch.ui.components.FormError
import com.example.realmatch.ui.components.OutlinedPillButton
import com.example.realmatch.ui.components.PrimaryButton
import com.example.realmatch.ui.components.UserProfileDetails
import com.example.realmatch.ui.theme.Background
import com.example.realmatch.ui.theme.Error
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleBorder
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.ui.theme.TextPrimary
import com.example.realmatch.ui.theme.TextSecondary
import com.example.realmatch.utils.decodeInviteParticipants
import com.example.realmatch.utils.extractInviteToken
import com.example.realmatch.utils.formatChatTime
import com.example.realmatch.utils.formatMessageTime
import com.example.realmatch.utils.formatOfferDate
import com.example.realmatch.utils.mediaUrl
import kotlinx.coroutines.delay

private val emojis = listOf(
    "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
    "🙂", "😉", "😍", "🥰", "😘", "😜", "🤪", "🤗", "🤔", "🤨",
    "😐", "😑", "😶", "🙄", "😏", "😬", "😴", "😷", "🤒", "🤕",
    "🥳", "😎", "🤓", "🧐", "😕", "😟", "🙁", "😮", "😯", "😲",
    "😳", "🥺", "😢", "😭", "😱", "😖", "😣", "😞", "😤", "😡",
    "👍", "👎", "👌", "✌️", "🤞", "🤝", "👏", "🙌", "🙏", "💪",
    "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "💔", "💕", "💯",
    "🔥", "✨", "🎉", "🎁", "☕", "🍕", "🍰", "🌹", "🌞", "🌙",
)

@Composable
fun ChatsScreen(
    openUserId: String? = null,
    onOpenHandled: () -> Unit = {},
    onChatOpenChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChatsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadChats() }

    LaunchedEffect(state.activeChat != null) {
        onChatOpenChanged(state.activeChat != null)
    }

    LaunchedEffect(state.chats, state.isLoading, openUserId) {
        if (openUserId == null) return@LaunchedEffect
        if (state.isLoading && state.chats.isEmpty()) return@LaunchedEffect
        val chat = state.chats.firstOrNull { it.otherUser.id == openUserId }
        if (chat != null) viewModel.openChat(chat)
        onOpenHandled()
    }

    if (state.showOffers) {
        OffersScreen(
            state = state,
            onBack = viewModel::closeOffers,
            onRetry = viewModel::retryOffers,
            onOfferClick = viewModel::createInvite,
        )
        return
    }

    if (state.inviteToken != null) {
        InviteScreen(
            state = state,
            onBack = viewModel::closeInvite,
            onRetry = viewModel::retryInvite,
            onAccept = viewModel::acceptInvite,
        )
        return
    }

    if (state.profileUserId != null) {
        ChatProfile(
            state = state,
            onBack = viewModel::closeProfile,
            onRetry = viewModel::retryProfile,
        )
        return
    }

    val activeChat = state.activeChat
    if (activeChat != null) {
        ChatConversation(
            chat = activeChat,
            state = state,
            onBack = viewModel::closeChat,
            onOpenProfile = { viewModel.openProfile(activeChat.otherUser.id) },
            onOpenOffers = viewModel::openOffers,
            onInviteClick = viewModel::openInvite,
            onRefresh = viewModel::refreshMessages,
            onSend = viewModel::sendMessage,
        )
    } else {
        ChatList(
            state = state,
            modifier = modifier,
            onOpenChat = viewModel::openChat,
            onRetry = viewModel::loadChats,
        )
    }
}

@Composable
private fun ChatList(
    state: ChatsUiState,
    modifier: Modifier = Modifier,
    onOpenChat: (ChatListItem) -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Чаты",
            modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 8.dp),
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
        )

        when {
            state.isLoading && state.chats.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Purple)
            }

            state.error != null && state.chats.isEmpty() -> Column(
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
                    onClick = onRetry,
                    modifier = Modifier.width(200.dp),
                )
            }

            state.chats.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chat),
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Пока нет чатов",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Когда появится взаимная симпатия, чат откроется автоматически.",
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.chats, key = { it.responderId + it.initiatorId }) { chat ->
                    ChatRow(chat = chat, onClick = { onOpenChat(chat) })
                }
            }
        }
    }
}

@Composable
private fun ChatRow(chat: ChatListItem, onClick: () -> Unit) {
    val photo = mediaUrl(chat.otherUser.mainPhoto)
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
            ChatAvatar(url = photo, size = 56)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chat.otherUser.firstName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatChatTime(chat.lastMessageTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (extractInviteToken(chat.lastMessage) != null) {
                            "Приглашение на свидание"
                        } else {
                            chat.lastMessage?.takeIf { it.isNotBlank() } ?: "Начните общение"
                        },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (chat.lastMessage.isNullOrBlank()) TextMuted else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (chat.unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Purple),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatConversation(
    chat: ChatListItem,
    state: ChatsUiState,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenOffers: () -> Unit,
    onInviteClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onSend: (String) -> Unit,
) {
    var text by rememberSaveable(chat.otherUser.id) { mutableStateOf("") }
    var showEmoji by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Опрос только пока экран реально на переднем плане
    LaunchedEffect(chat.initiatorId, chat.responderId, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(4000)
                onRefresh()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Назад",
                    tint = TextPrimary,
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenProfile)
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChatAvatar(url = mediaUrl(chat.otherUser.mainPhoto), size = 38)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = chat.otherUser.firstName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onOpenOffers) {
                Icon(
                    painter = painterResource(R.drawable.ic_redeem),
                    contentDescription = "Предложить свидание",
                    tint = Purple,
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.messagesLoading && state.messages.isEmpty() -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Purple,
                )

                state.messages.isEmpty() -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chat),
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Начните общение",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Напишите первым — хорошее начало уже половина свидания.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.messages) { message ->
                        MessageBubble(
                            message = message,
                            isMine = message.authorId == state.myUserId,
                            onInviteClick = onInviteClick,
                        )
                    }
                }
            }
        }

        state.messagesError?.let { error ->
            Text(
                text = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        if (showEmoji) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                emojis.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { text += emoji },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                placeholder = {
                    Text(
                        text = "Сообщение…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background,
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = PurpleBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Purple,
                ),
            )
            IconButton(onClick = {
                showEmoji = !showEmoji
                if (showEmoji) keyboard?.hide()
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_emoji),
                    contentDescription = "Эмодзи",
                    tint = if (showEmoji) Purple else TextSecondary,
                )
            }
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                        showEmoji = false
                    }
                },
                enabled = text.isNotBlank() && !state.isSending,
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Purple,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = "Отправить",
                        tint = if (text.isNotBlank()) Purple else TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    onInviteClick: (String) -> Unit,
) {
    val inviteToken = extractInviteToken(message.text)
    val shape = if (isMine) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }
    val textColor = if (isMine) Color.White else TextPrimary
    val secondaryColor = if (isMine) Color.White.copy(alpha = 0.78f) else TextMuted

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(if (isMine) Purple else Color.White)
                .then(
                    if (inviteToken != null) {
                        Modifier.clickable { onInviteClick(inviteToken) }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (inviteToken != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_redeem),
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Приглашение на свидание",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isMine) "Отправлено вами" else "Нажмите, чтобы открыть",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryColor,
                )
            } else {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = formatMessageTime(message.createdAt),
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = secondaryColor,
            )
        }
    }
}

@Composable
private fun ChatAvatar(url: String?, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(PurpleSoft),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size((size / 2).dp),
            )
        }
    }
}

@Composable
private fun ChatProfile(
    state: ChatsUiState,
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

        val profile = state.profile
        when {
            state.isProfileLoading && profile == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Purple)
            }

            profile == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.profileError ?: "Не удалось загрузить профиль",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onRetry) {
                    Text("Повторить", color = Purple)
                }
            }

            else -> UserProfileDetails(profile = profile)
        }
    }
}

@Composable
private fun OffersScreen(
    state: ChatsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOfferClick: (String) -> Unit,
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
                text = "Специальные предложения",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        FormError(state.offersError, Modifier.padding(horizontal = 24.dp))

        when {
            state.offersLoading && state.offers.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Purple)
            }

            state.offers.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.offersError ?: "Пока нет активных предложений",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedPillButton(
                    text = "Повторить",
                    onClick = onRetry,
                    modifier = Modifier.width(200.dp),
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(state.offers, key = { it.id }) { offer ->
                    OfferCard(
                        offer = offer,
                        isInviting = state.invitingOfferId == offer.id,
                        enabled = state.invitingOfferId == null,
                        onInvite = { onOfferClick(offer.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OfferCard(
    offer: Offer,
    isInviting: Boolean,
    enabled: Boolean,
    onInvite: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column {
            AsyncImage(
                model = mediaUrl(offer.photoUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = offer.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Text(
                        text = "до ${formatOfferDate(offer.validUntil)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = offer.discountInfo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Purple,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = offer.partner.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = offerPlace(offer.partner),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = onInvite,
                        enabled = enabled,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        if (isInviting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "Предложить",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteScreen(
    state: ChatsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAccept: () -> Unit,
) {
    val participants = state.inviteToken?.let(::decodeInviteParticipants)
    val isSender = state.myUserId != null && participants?.senderId == state.myUserId
    val isReceiver = state.myUserId != null && participants?.receiverId == state.myUserId

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
                text = "Приглашение",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
        }

        when {
            state.inviteAccepted -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PurpleSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Промокод получен",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Половинки промокода отправлены в чат. Сложите их вместе, чтобы получить скидку.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Вернуться в чат",
                    onClick = onBack,
                )
            }

            state.inviteLoading && state.invitePreview == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Purple)
            }

            state.invitePreview == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.inviteError ?: "Не удалось загрузить приглашение",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onRetry) {
                    Text("Повторить", color = Purple)
                }
            }

            else -> InvitePreviewContent(
                preview = state.invitePreview,
                error = state.inviteError,
                accepting = state.acceptingInvite,
                isSender = isSender,
                isReceiver = isReceiver,
                onAccept = onAccept,
            )
        }
    }
}

@Composable
private fun InvitePreviewContent(
    preview: InvitePreview,
    error: String?,
    accepting: Boolean,
    isSender: Boolean,
    isReceiver: Boolean,
    onAccept: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(PurpleSoft, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_redeem),
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(34.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = preview.offerTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = preview.partnerName,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Приглашает: ${preview.initiatorName}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(20.dp))
        if (isReceiver) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (preview.isValid) PurpleSoft else Color(0xFFFFE9EC))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = if (preview.isValid) {
                        "Приглашение действительно"
                    } else {
                        "Срок действия истёк или промокод уже получен"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (preview.isValid) Purple else Error,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Принять приглашение",
                onClick = onAccept,
                enabled = preview.isValid,
                isLoading = accepting,
            )
            FormError(error)
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PurpleSoft)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = if (isSender) {
                        "Вы отправили это приглашение"
                    } else {
                        "Приглашение адресовано другому пользователю"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = Purple,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (isSender) {
                    "Принять его может только получатель."
                } else {
                    "Принять может только приглашённый пользователь."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Город + адрес заведения: «Москва, ул. Ленина, 1». */
private fun offerPlace(partner: OfferPartner): String = listOfNotNull(
    partner.city?.takeIf { it.isNotBlank() },
    partner.address.takeIf { it.isNotBlank() },
).joinToString(", ")
