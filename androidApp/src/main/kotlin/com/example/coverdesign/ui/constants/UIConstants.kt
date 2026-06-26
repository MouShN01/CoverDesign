package com.example.coverdesign.ui.constants

import com.example.coverdesign.domain.model.Texture

object UIConstants {
    val DEFAULT_TEXTURES = listOf(
        Texture(
            id = "tex-glossy",
            name = "Glossy",
            previewUrl = "textures/glossy.png",
            description = "Smooth high-gloss finish"
        ),
        Texture(
            id = "tex-rough",
            name = "Rough",
            previewUrl = "textures/rough.png",
            description = "Matte rough surface"
        ),
        Texture(
            id = "tex-transparent",
            name = "Transparent",
            previewUrl = "textures/transparent.png",
            description = "Clear / translucent finish"
        )
    )
}
