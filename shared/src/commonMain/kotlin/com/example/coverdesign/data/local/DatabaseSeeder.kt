package com.example.coverdesign.data.local

// shared/commonMain/data/local/DatabaseSeeder.kt
import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.util.currentEpochMillis

// shared/commonMain/data/local/DatabaseSeeder.kt
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

    // returns pairs so model is always seeded alongside its product
    private fun fakeProducts(): List<Pair<Product, Model3D>> = listOf(

        Product(
            id                 = "prod-001",
            name               = "Forearm Cover — Classic",
            description        = "Lightweight prosthetic forearm cover with smooth finish.",
            basePrice          = 149.99,
            category           = ProductCategory.UPPER_LIMB,
            availableMaterials = listOf(Material.PLA, Material.CARBON_FIBER, Material.PLASTIC),
            availableTextures  = listOf(
                Texture("tex-001", "Matte",    "", "Smooth matte surface"),
                Texture("tex-002", "Glossy",   "", "High-gloss finish"),
                Texture("tex-003", "Textured", "", "Grip-enhanced surface")
            ),
            imageUrl           = ""
        ) to Model3D(
            id = "model-001",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb",
            previewImageUrl = "",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        ),

        Product(
            id                 = "prod-002",
            name               = "Hand Shell — Sport",
            description        = "Ergonomic hand shell designed for active use.",
            basePrice          = 199.99,
            category           = ProductCategory.UPPER_LIMB,
            availableMaterials = listOf(Material.CARBON_FIBER, Material.EPOXY_RESIN),
            availableTextures  = listOf(
                Texture("tex-004", "Carbon weave", "", "Exposed carbon fiber weave"),
                Texture("tex-005", "Voronoi",      "", "Lattice cutout pattern")
            ),
            imageUrl           = ""
        ) to Model3D(
            id = "model-002",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/RobotExpressive.glb",
            previewImageUrl = "",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        ),

        Product(
            id                 = "prod-003",
            name               = "Finger Prosthetic — Slim",
            description        = "Precision-fitted finger cover with natural appearance.",
            basePrice          = 89.99,
            category           = ProductCategory.OTHER,
            availableMaterials = listOf(Material.PLA, Material.PLASTIC),
            availableTextures  = listOf(
                Texture("tex-001", "Matte", "", "Smooth matte surface"),
                Texture("tex-006", "Skin",  "", "Skin-tone optimised finish")
            ),
            imageUrl           = ""
        ) to Model3D(
            id = "model-003",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/NeilArmstrong.glb",
            previewImageUrl = "",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        ),

        Product(
            id                 = "prod-004",
            name               = "Lower Leg Cover — Geometric",
            description        = "Bold geometric patterned lower leg cover.",
            basePrice          = 249.99,
            category           = ProductCategory.OTHER,
            availableMaterials = listOf(
                Material.PLA, Material.CARBON_FIBER,
                Material.METAL, Material.EPOXY_RESIN
            ),
            availableTextures  = listOf(
                Texture("tex-007", "Geometric", "", "Sharp geometric relief pattern"),
                Texture("tex-005", "Voronoi",   "", "Open lattice structure"),
                Texture("tex-008", "Woven",     "", "Woven basket texture")
            ),
            imageUrl           = ""
        ) to Model3D(
            id = "model-004",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/Horse.glb",
            previewImageUrl = "",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        ),

        Product(
            id                 = "prod-005",
            name               = "Cosmetic Cover — Elegance",
            description        = "Slim decorative cosmetic cover for standard prosthetics.",
            basePrice          = 119.99,
            category           = ProductCategory.COSMETIC_COVER,
            availableMaterials = listOf(Material.PLASTIC, Material.GOLD_PLATED),
            availableTextures  = listOf(
                Texture("tex-002", "Glossy",   "", "Mirror-like gloss finish"),
                Texture("tex-009", "Engraved", "", "Custom engraved pattern")
            ),
            imageUrl           = ""
        ) to Model3D(
            id = "model-005",
            configId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/ShopWindow.glb",
            previewImageUrl = "",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
            estimatedWaitSeconds = null
        )
    )
}