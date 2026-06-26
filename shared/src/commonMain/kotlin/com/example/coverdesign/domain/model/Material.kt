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
    WOVEN_BASKET("Woven Basket"),
    ABS("Acrylonitrile Butadiene Styrene"),
    NYLON("Nylon"),
    SILICONE("Silicone"),
    LEATHER("Leather"),
    TITANIUM("Titanium"),
    STAINLESS_STEEL("Stainless Steel"),
    COTTON("Cotton"),
    POLYESTER("Polyester"),
    DENIM("Denim"),
    RUBBER("Rubber"),
    SILVER("Silver"),
    GOLD("Gold"),
    PETG("Polyethylene Terephthalate Glycol-modified filament"),
    RESIN("Resin"),
    TPU("TPU filament"),
    CERAMIC("Ceramic"),
    GLASS("Glass"),
}