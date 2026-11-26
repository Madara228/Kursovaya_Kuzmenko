package com.example.realmatch.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.realmatch.data.models.PhotoDto
import com.example.realmatch.ui.components.AuthTextField
import com.example.realmatch.ui.components.FormError
import com.example.realmatch.ui.components.OutlinedPillButton
import com.example.realmatch.ui.components.PrimaryButton
import com.example.realmatch.ui.theme.Background
import com.example.realmatch.ui.theme.Error
import com.example.realmatch.ui.theme.Purple
import com.example.realmatch.ui.theme.PurpleBorder
import com.example.realmatch.ui.theme.PurpleSoft
import com.example.realmatch.ui.theme.TextMuted
import com.example.realmatch.ui.theme.TextPrimary
import com.example.realmatch.ui.theme.TextSecondary
import com.example.realmatch.utils.calculateAge
import com.example.realmatch.utils.formatBirthDate
import com.example.realmatch.utils.genderLabel
import com.example.realmatch.utils.mediaUrl

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.uploadPhoto(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Профиль",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onLogout) {
                Text("Выйти", color = Purple)
            }
        }

        FormError(state.error, Modifier.padding(horizontal = 24.dp))

        when {
            state.isLoading && state.profile == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Purple)
            }

            state.profile == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Не удалось загрузить профиль",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton(text = "Повторить", onClick = viewModel::loadProfile)
            }

            else -> ProfileContent(
                state = state,
                onCityChange = viewModel::setCity,
                onBioChange = viewModel::setBio,
                onSave = viewModel::saveProfile,
                onEditToggle = {
                    if (state.isEditing) viewModel.cancelEditing() else viewModel.startEditing()
                },
                onPhotoEditToggle = viewModel::togglePhotoEditing,
                onAddPhoto = { picker.launch("image/*") },
                onMakeMain = viewModel::setMainPhoto,
                onDeletePhoto = viewModel::deletePhoto,
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onCityChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSave: () -> Unit,
    onEditToggle: () -> Unit,
    onPhotoEditToggle: () -> Unit,
    onAddPhoto: () -> Unit,
    onMakeMain: (String) -> Unit,
    onDeletePhoto: (String) -> Unit,
) {
    val profile = state.profile ?: return
    val age = calculateAge(profile.birthDate)
    val meta = listOfNotNull(formatBirthDate(profile.birthDate), genderLabel(profile.gender))
        .joinToString(" · ")
    val dirty = state.editCity.trim() != profile.city.orEmpty().trim() ||
        state.editBio.trim() != profile.bio.orEmpty().trim()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        if (!state.isEditing && !state.isPhotoEditing) {
            val incomplete = profile.photos.isEmpty() ||
                profile.city.isNullOrBlank() ||
                profile.bio.isNullOrBlank()
            if (incomplete) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurpleSoft,
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = null,
                            tint = Purple,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Заполните анкету: добавьте фото, город и пару слов о себе — так вас чаще будут лайкать.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        PhotosSection(
            photos = profile.photos,
            isEditing = state.isPhotoEditing,
            isBusy = state.isPhotoBusy,
            onToggleEdit = onPhotoEditToggle,
            onAddPhoto = onAddPhoto,
            onMakeMain = onMakeMain,
            onDeletePhoto = onDeletePhoto,
        )

        Spacer(Modifier.height(20.dp))
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

        if (state.isEditing) {
            Spacer(Modifier.height(20.dp))
            AuthTextField(
                label = "Город",
                value = state.editCity,
                onValueChange = onCityChange,
                placeholder = "Укажите город",
                modifier = Modifier.fillMaxWidth(),
            )
        } else if (!profile.city.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
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
        if (state.isEditing) {
            OutlinedTextField(
                value = state.editBio,
                onValueChange = onBioChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 112.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                placeholder = {
                    Text(
                        text = "Расскажите о себе",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                    )
                },
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
        } else {
            Text(
                text = profile.bio?.takeIf { it.isNotBlank() } ?: "Расскажите о себе",
                style = MaterialTheme.typography.bodyLarge,
                color = if (profile.bio.isNullOrBlank()) TextMuted else TextPrimary,
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            PrimaryButton(
                text = "Сохранить",
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = state.isEditing && dirty,
                isLoading = state.isSaving,
            )
            Spacer(Modifier.width(12.dp))
            OutlinedPillButton(
                text = if (state.isEditing) "Отмена" else "Изменить",
                onClick = onEditToggle,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PhotosSection(
    photos: List<PhotoDto>,
    isEditing: Boolean,
    isBusy: Boolean,
    onToggleEdit: () -> Unit,
    onAddPhoto: () -> Unit,
    onMakeMain: (String) -> Unit,
    onDeletePhoto: (String) -> Unit,
) {
    val main = photos.firstOrNull { it.isMain } ?: photos.firstOrNull()
    val others = photos.filter { it.id != main?.id }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clip(RoundedCornerShape(24.dp))
                .background(PurpleSoft),
        ) {
            if (main != null) {
                AsyncImage(
                    model = mediaUrl(main.url),
                    contentDescription = "Главное фото",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera),
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Нет фото",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Purple,
                )
            }
        }
        if (isEditing && main != null) {
            DeleteBadge(
                onDelete = { onDeletePhoto(main.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
            )
        }
    }

    if (others.isNotEmpty() || isEditing) {
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            others.forEach { photo ->
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = isEditing) { onMakeMain(photo.id) },
                ) {
                    AsyncImage(
                        model = mediaUrl(photo.url),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (isEditing) {
                        DeleteBadge(
                            onDelete = { onDeletePhoto(photo.id) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp),
                        )
                    }
                }
            }
            if (isEditing && photos.size < 6) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable(onClick = onAddPhoto),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Добавить фото",
                        tint = Purple,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }

    if (isEditing && others.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Нажмите на маленькое фото, чтобы сделать его главным",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onToggleEdit) {
            Text(if (isEditing) "Готово" else "Изменить фото", color = Purple)
        }
    }
}

@Composable
private fun DeleteBadge(onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.92f))
            .clickable(onClick = onDelete),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = "Удалить фото",
            tint = Error,
            modifier = Modifier.size(15.dp),
        )
    }
}
