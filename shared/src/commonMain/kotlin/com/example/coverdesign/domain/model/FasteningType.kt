package com.example.coverdesign.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class FasteningType(val displayName: String, val description: String) {
    STRAP("Strap", "Traditional adjustable strap — secure fit, easy to tighten"),
    MAGNETIC("Magnetic", "Magnetic closure — quick to attach, tool-free release")
}
