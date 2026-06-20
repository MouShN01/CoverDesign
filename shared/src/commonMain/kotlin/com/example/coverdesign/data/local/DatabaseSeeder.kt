package com.example.coverdesign.data.local


import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.util.currentEpochMillis

class DatabaseSeeder(private val db: AppDatabase) {

    fun seedIfEmpty() {
        val count = db.catalogQueries.countProducts().executeAsOne()
        if (count > 0L) return

        db.transaction {
            fakeProducts().forEach { (product, model) ->
                db.catalogQueries.insertOrReplaceProduct(
                    product.toEntity(cachedAt = currentEpochMillis())
                )
                db.model3DQueries.insertOrReplaceModel3D(
                    model.toEntity(productId = product.id)
                )
            }
        }
    }

    private fun fakeProducts(): List<Pair<Product, Model3D>> = listOf(

        // ── ACCESSORY ──────────────────────────────────────────────────────────
        Product(
            id                 = "prod-001",
            name               = "Custom Phone Case",
            description        = "Personalized phone case with adjustable color, material, and engraved text.",
            basePrice          = 24.99,
            category           = ProductCategory.ACCESSORY,
            availableMaterials = listOf(Material.PLASTIC, Material.PLA, Material.EPOXY_RESIN),
            availableTextures  = listOf(
                Texture("tex-001", "Matte",    "", "Smooth matte surface"),
                Texture("tex-002", "Glossy",   "", "High-gloss finish"),
                Texture("tex-003", "Textured", "", "Grip-enhanced surface")
            ),
            imageUrl           = "https://images.unsplash.com/photo-1601593346740-925612772716?w=600&q=80"
        ) to Model3D(
            id = "model-001",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb",
            previewImageUrl = "https://images.unsplash.com/photo-1601593346740-925612772716?w=300&q=80",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        ),

        // ── THREE_D_PRINT ──────────────────────────────────────────────────────
        Product(
            id                 = "prod-002",
            name               = "Articulated Desk Figurine",
            description        = "Fully articulated 3D-printed figurine with customizable pose and finish.",
            basePrice          = 39.99,
            category           = ProductCategory.THREE_D_PRINT,
            availableMaterials = listOf(Material.PLA, Material.CARBON_FIBER, Material.EPOXY_RESIN),
            availableTextures  = listOf(
                Texture("tex-004", "Carbon weave", "", "Exposed carbon fiber weave"),
                Texture("tex-005", "Voronoi",      "", "Lattice cutout pattern")
            ),
            imageUrl           = "https://images.unsplash.com/photo-1631377647363-7d6ed5b73089?w=600&q=80"
        ) to Model3D(
            id = "model-002",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/RobotExpressive.glb",
            previewImageUrl = "https://images.unsplash.com/photo-1631377647363-7d6ed5b73089?w=300&q=80",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        ),

        // ── JEWELRY ────────────────────────────────────────────────────────────
        Product(
            id                 = "prod-003",
            name               = "Engraved Pendant",
            description        = "Custom pendant with personalized engraving and material finish.",
            basePrice          = 59.99,
            category           = ProductCategory.JEWELRY,
            availableMaterials = listOf(Material.GOLD_PLATED, Material.METAL, Material.PLASTIC),
            availableTextures  = listOf(
                Texture("tex-002", "Glossy", "", "Mirror-like gloss finish"),
                Texture("tex-009", "Engraved", "", "Custom engraved pattern")
            ),
            imageUrl           = "https://images.unsplash.com/photo-1611652022419-a9419f74343d?w=600&q=80"
        ) to Model3D(
            id = "model-003",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/NeilArmstrong.glb",
            previewImageUrl = "https://images.unsplash.com/photo-1611652022419-a9419f74343d?w=300&q=80",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        ),

        // ── CLOTHING ───────────────────────────────────────────────────────────
        Product(
            id                 = "prod-004",
            name               = "Custom Print T-Shirt",
            description        = "T-shirt with personalized print, color, and fit options.",
            basePrice          = 34.99,
            category           = ProductCategory.CLOTHING,
            availableMaterials = listOf(Material.PLASTIC, Material.WOVEN_BASKET),
            availableTextures  = listOf(
                Texture("tex-007", "Geometric", "", "Sharp geometric relief pattern"),
                Texture("tex-008", "Woven",     "", "Woven basket texture")
            ),
            imageUrl           = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&q=80"
        ) to Model3D(
            id = "model-004",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/Horse.glb",
            previewImageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=300&q=80",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        ),

        // ── FOOTWEAR ───────────────────────────────────────────────────────────
        Product(
            id                 = "prod-005",
            name               = "Custom Sneaker Design",
            description        = "Sneakers with personalized colorway, material, and sole texture.",
            basePrice          = 89.99,
            category           = ProductCategory.FOOTWEAR,
            availableMaterials = listOf(Material.PLASTIC, Material.CARBON_FIBER),
            availableTextures  = listOf(
                Texture("tex-002", "Glossy",   "", "High-gloss finish"),
                Texture("tex-003", "Textured", "", "Grip-enhanced surface")
            ),
            imageUrl           = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80"
        ) to Model3D(
            id = "model-005",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/ShopWindow.glb",
            previewImageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=300&q=80",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        ),

        // ── INTERIOR_DECOR ─────────────────────────────────────────────────────
        Product(
            id                 = "prod-006",
            name               = "Custom Vase",
            description        = "Decorative vase with adjustable shape, color, and texture.",
            basePrice          = 44.99,
            category           = ProductCategory.INTERIOR_DECOR,
            availableMaterials = listOf(Material.PLA, Material.WOOD, Material.EPOXY_RESIN),
            availableTextures  = listOf(
                Texture("tex-005", "Voronoi",   "", "Lattice cutout pattern"),
                Texture("tex-008", "Woven",     "", "Woven basket texture")
            ),
            imageUrl           = "https://images.unsplash.com/photo-1578500494198-246f612d3b3d?w=600&q=80"
        ) to Model3D(
            id = "model-006",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/reflective-sphere.glb",
            previewImageUrl = "https://images.unsplash.com/photo-1578500494198-246f612d3b3d?w=300&q=80",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        )
    )
}