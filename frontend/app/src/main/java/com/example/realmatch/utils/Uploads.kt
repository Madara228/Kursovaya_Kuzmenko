package com.example.realmatch.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// shortcut: файл читается в память целиком; для больших изображений перейти на стриминг
suspend fun uriToMultipart(
    context: Context,
    uri: Uri,
    partName: String = "file",
): MultipartBody.Part = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw IllegalStateException("Не удалось прочитать файл")
    val mimeType = resolver.getType(uri) ?: "image/jpeg"
    val fileName = displayName(resolver, uri) ?: "photo.jpg"
    MultipartBody.Part.createFormData(
        partName,
        fileName,
        bytes.toRequestBody(mimeType.toMediaTypeOrNull()),
    )
}

private fun displayName(resolver: ContentResolver, uri: Uri): String? =
    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
