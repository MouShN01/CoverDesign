package com.example.coverdesign.domain.ar

import android.content.Context
import com.google.ar.core.ArCoreApk
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual object ArCapability : KoinComponent {
    private val context: Context by inject()

    actual fun isSupported(): Boolean = try {
        ArCoreApk.getInstance()
            .checkAvailability(context)
            .isSupported
    } catch (e: Exception) { false }
}