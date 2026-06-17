package com.example.coverdesign.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Material(val displayName: String) {
    PLA("PLA"),
    METAL("Metal"),
    WOOD("Wood"),
    PLASTIC("Plastic"),
    CARBON_FIBER("Carbon Fiber"),
    EPOXY_RESIN("Epoxy Resin"),
    GOLD_PLATED("Gold-plated"),
    VORONOI("Voronoi"),
    WOVEN_BASKET("Woven Basket")
}