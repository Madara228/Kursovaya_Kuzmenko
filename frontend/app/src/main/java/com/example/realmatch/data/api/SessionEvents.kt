package com.example.realmatch.data.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** События сессии: сейчас — протухший/невалидный токен (401). */
object SessionEvents {
    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized: SharedFlow<Unit> = _unauthorized.asSharedFlow()

    fun notifyUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }
}
