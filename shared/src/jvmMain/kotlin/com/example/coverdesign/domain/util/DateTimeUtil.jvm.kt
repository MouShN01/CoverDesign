package com.example.coverdesign.domain.util

import kotlin.toString

actual fun currentEpochMillis(): Long = System.currentTimeMillis()

// jvmMain
actual fun generateUuid(): String = java.util.UUID.randomUUID().toString()