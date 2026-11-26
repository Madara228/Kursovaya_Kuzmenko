package com.example.realmatch.ui.partner

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.realmatch.R
import com.example.realmatch.data.models.PartnerOffer
import com.example.realmatch.data.models.PartnerProfile
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
import com.example.realmatch.utils.formatOfferDate
import com.example.realmatch.utils.mediaUrl
import com.example.realmatch.utils.offerDateInputToIso
import com.example.realmatch.utils.offerDateIsoToInput
import com.example.realmatch.utils.parseOfferDateInput
import java.time.LocalDate

@Composable
fun PartnerCabinetScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PartnerCabinetViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Мое заведение",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onLogout) {
                Text("Выйти", color = Purple)
            }
        }

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
                    text = state.error ?: "Не удалось загрузить кабинет",
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

            else -> PartnerContent(
                state = state,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun PartnerContent(
    state: PartnerUiState,
    viewModel: PartnerCabinetViewModel,
) {
    val profile = state.profile ?: return
    val context = LocalContext.current

    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var discountInfo by rememberSaveable { mutableStateOf("") }
    var dateInput by rememberSaveable { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    var pendingDeactivate by remember { mutableStateOf<PartnerOffer?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) photoUri = uri
    }

    fun resetForm() {
        editingId = null
        title = ""
        discountInfo = ""
        dateInput = ""
        photoUri = null
        existingPhotoUrl = null
        formError = null
        viewModel.clearActionError()
    }

    fun openDatePicker() {
        val initial = parseOfferDateInput(dateInput) ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                dateInput = offerDateIsoToInput(LocalDate.of(year, month + 1, day).toString())
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth,
        ).show()
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            resetForm()
            viewModel.consumeSaveSuccess()
        }
    }

    val today = LocalDate.now().toString()
    val activeCount = state.offers.count { it.validUntil >= today }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        ProfileCard(profile)

        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Акции",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = activeOffersLabel(activeCount),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        Spacer(Modifier.height(10.dp))

        if (state.offers.isEmpty()) {
            Text(
                text = "Пока нет акций — создайте первую ниже.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        } else {
            state.offers.forEach { offer ->
                OfferManageCard(
                    offer = offer,
                    isDeactivating = state.deactivatingId == offer.id,
                    onEdit = {
                        editingId = offer.id
                        title = offer.title
                        discountInfo = offer.discountInfo
                        dateInput = offerDateIsoToInput(offer.validUntil)
                        photoUri = null
                        existingPhotoUrl = offer.photoUrl
                        formError = null
                        viewModel.clearActionError()
                    },
                    onDeactivate = { pendingDeactivate = offer },
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        OfferForm(
            editing = editingId != null,
            title = title,
            discountInfo = discountInfo,
            dateInput = dateInput,
            photoUri = photoUri,
            existingPhotoUrl = existingPhotoUrl,
            isSaving = state.isSaving,
            error = formError ?: state.actionError,
            onTitleChange = { title = it },
            onDiscountChange = { discountInfo = it },
            onDateChange = { dateInput = it },
            onOpenDatePicker = ::openDatePicker,
            onPickPhoto = { picker.launch("image/*") },
            onClear = ::resetForm,
            onSubmit = {
                val isoDate = offerDateInputToIso(dateInput)
                formError = when {
                    title.isBlank() || discountInfo.isBlank() -> "Заполните название и описание скидки"
                    isoDate == null -> "Укажите дату в формате ДД.ММ.ГГГГ"
                    editingId == null && photoUri == null -> "Добавьте фото акции"
                    else -> null
                }
                if (formError == null && isoDate != null) {
                    viewModel.saveOffer(
                        offerId = editingId,
                        title = title.trim(),
                        discountInfo = discountInfo.trim(),
                        validUntilIso = isoDate,
                        photoUri = photoUri,
                    )
                }
            },
        )
        Spacer(Modifier.height(24.dp))
    }

    pendingDeactivate?.let { offer ->
        AlertDialog(
            onDismissRequest = { pendingDeactivate = null },
            title = { Text("Завершить акцию?") },
            text = { Text("Акция «${offer.title}» перестанет показываться пользователям.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDeactivate = null
                    viewModel.deactivateOffer(offer.id)
                }) {
                    Text("Завершить", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeactivate = null }) {
                    Text("Отмена", color = TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun ProfileCard(profile: PartnerProfile) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PurpleSoft, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_storefront),
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Партнёр RealMatch",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            InfoRow(R.drawable.ic_person, "Контактное лицо", profile.contactPerson)
            InfoRow(R.drawable.ic_call, "Телефон", profile.phone)
            InfoRow(R.drawable.ic_storefront, "Город", profile.city)
            InfoRow(R.drawable.ic_location, "Адрес", profile.address)
            profile.contactInfo?.takeIf { it.isNotBlank() }?.let {
                InfoRow(R.drawable.ic_link, "Ссылка", it, valueColor = Purple)
            }
        }
    }
}

@Composable
private fun InfoRow(
    iconRes: Int,
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor,
            )
        }
    }
}

@Composable
private fun OfferManageCard(
    offer: PartnerOffer,
    isDeactivating: Boolean,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit,
) {
    val expired = offer.validUntil < LocalDate.now().toString()
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = mediaUrl(offer.photoUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 76.dp, height = 84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PurpleSoft),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = offer.discountInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Purple,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = if (expired) "Завершена" else "До ${formatOfferDate(offer.validUntil)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Редактировать акцию",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onDeactivate, enabled = !isDeactivating) {
                    if (isDeactivating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Purple,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "Завершить акцию",
                            tint = Error,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferForm(
    editing: Boolean,
    title: String,
    discountInfo: String,
    dateInput: String,
    photoUri: Uri?,
    existingPhotoUrl: String?,
    isSaving: Boolean,
    error: String?,
    onTitleChange: (String) -> Unit,
    onDiscountChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onOpenDatePicker: () -> Unit,
    onPickPhoto: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (editing) "Редактирование акции" else "Новая акция",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Появится в подборке мест для свиданий.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))

            AuthTextField(
                label = "Название акции",
                value = title,
                onValueChange = onTitleChange,
                placeholder = "Например, свидание со вкусом",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                label = "Описание скидки",
                value = discountInfo,
                onValueChange = onDiscountChange,
                placeholder = "Например, скидка 20% на меню",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Действует до",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = dateInput,
                onValueChange = onDateChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "ДД.ММ.ГГГГ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    IconButton(onClick = onOpenDatePicker) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = "Выбрать дату",
                            tint = TextMuted,
                        )
                    }
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

            Spacer(Modifier.height(12.dp))
            val preview = photoUri?.toString() ?: existingPhotoUrl?.let { mediaUrl(it) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PurpleSoft)
                    .clickable(onClick = onPickPhoto),
            ) {
                if (preview != null) {
                    AsyncImage(
                        model = preview,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Text(
                        text = "Нажмите, чтобы заменить фото",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        textAlign = TextAlign.Center,
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
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Загрузить фото",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Purple,
                        )
                        Text(
                            text = "JPG или PNG",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                }
            }

            FormError(error)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedPillButton(
                    text = "Очистить",
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                PrimaryButton(
                    text = if (editing) "Сохранить" else "Создать",
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                    isLoading = isSaving,
                )
            }
        }
    }
}

private fun activeOffersLabel(count: Int): String {
    val lastTwo = count % 100
    val last = count % 10
    val word = when {
        lastTwo in 11..14 -> "активных"
        last == 1 -> "активная"
        last in 2..4 -> "активные"
        else -> "активных"
    }
    return "$count $word"
}
