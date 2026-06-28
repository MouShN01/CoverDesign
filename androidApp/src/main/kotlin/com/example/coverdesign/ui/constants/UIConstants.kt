package com.example.coverdesign.ui.constants

import com.example.coverdesign.domain.model.Texture

object UIConstants {
    const val TEXTURE_ID_PREFIX = "tex-"
    val DEFAULT_TEXTURES = listOf(
        Texture(
            id = "tex-glossy",
            name = "Glossy",
            previewUrl = null, //"textures/glossy.png",
            description = "Smooth high-gloss finish"
        ),
        Texture(
            id = "tex-rough",
            name = "Rough",
            previewUrl = null, //"textures/rough.png",
            description = "Matte rough surface"
        ),
        Texture(
            id = "tex-transparent",
            name = "Transparent",
            previewUrl = null, //"textures/transparent.png",
            description = "Clear / translucent finish"
        )
    )
}
