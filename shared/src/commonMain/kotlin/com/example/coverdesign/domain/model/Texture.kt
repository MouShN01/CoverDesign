package com.example.coverdesign.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Texture(
    val id: String,
    val name: String,
    val previewUrl: String?,
    val description: String?
)