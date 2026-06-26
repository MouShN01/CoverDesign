package com.example.coverdesign.data.local


import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.model.AdaptedModel3D
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

    // add to DatabaseSeeder.kt, after seeding products + base models
    private fun seedFakeAdaptedModels() {
        // pretend the user already configured prod-001 once, so AR/Preview have something to show
        db.adaptedModel3DQueries.insertOrReplaceAdaptedModel(
            AdaptedModel3D(
                id = "adapted-001",
                configId = "demo-config-001",     // matches a seeded ProductConfigEntity if you add one
                baseModelId = "model-001",
                modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb",
                previewImageUrl = "",
                status = ModelStatus.READY,
                generatedAt = currentEpochMillis()
            ).toEntity()
        )
    }

    private fun fakeProducts(): List<Pair<Product, Model3D>> = listOf(

        // ── ACCESSORY ──────────────────────────────────────────────────────────
        Product(
            id                 = "prod-001",
            name               = "Generic Symmetry iPhone Case",
            description        = "Personalized phone case with adjustable color, material, and engraved text.",
            basePrice          = 24.99,
            category           = ProductCategory.ACCESSORY,
            availableMaterials = listOf(Material.PLASTIC, Material.PLA, Material.EPOXY_RESIN),
            availableTextures  = listOf(
                Texture("tex-001", "Matte",    "", "Smooth matte surface"),
                Texture("tex-002", "Glossy",   "", "High-gloss finish"),
                Texture("tex-003", "Rough", "", "Grip-enhanced surface")
            ),
            imageUrl           = "https://techneek.ie/cdn/shop/products/76.png"
        ) to Model3D(
            id = "model-001",
            productId = "model-001",
            modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb",
            previewImageUrl = "https://techneek.ie/cdn/shop/products/76.png",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
        ),

        // ── THREE_D_PRINT ──────────────────────────────────────────────────────
        Product(
            id                 = "prod-002",
            name               = "Spectacle Frames with Adjustable Nose Bridges",
            description        = "Fully articulated 3D-printed figurine with customizable pose and finish.",
            basePrice          = 39.99,
            category           = ProductCategory.THREE_D_PRINT,
            availableMaterials = listOf(Material.PLA, Material.CARBON_FIBER, Material.EPOXY_RESIN),
            availableTextures  = listOf(
                Texture("tex-004", "Carbon weave", "", "Exposed carbon fiber weave"),
                Texture("tex-005", "Voronoi",      "", "Lattice cutout pattern")
            ),
            imageUrl           = "https://www.sigmaaldrich.com/deepweb/assets/sigmaaldrich/product/images/109/310/0c95a1dc-dba2-4cdc-8cfa-c010d5bfba5f/640/0c95a1dc-dba2-4cdc-8cfa-c010d5bfba5f.jpg"
        ) to Model3D(
            id = "model-002",
            productId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/RobotExpressive.glb",
            previewImageUrl = "https://www.sigmaaldrich.com/deepweb/assets/sigmaaldrich/product/images/109/310/0c95a1dc-dba2-4cdc-8cfa-c010d5bfba5f/640/0c95a1dc-dba2-4cdc-8cfa-c010d5bfba5f.jpg",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
        ),

        // ── JEWELRY ────────────────────────────────────────────────────────────
        Product(
            id                 = "prod-003",
            name               = "Set of rings",
            description        = "Custom pendant with personalized engraving and material finish.",
            basePrice          = 59.99,
            category           = ProductCategory.JEWELRY,
            availableMaterials = listOf(Material.GOLD_PLATED, Material.METAL, Material.PLASTIC),
            availableTextures  = listOf(
                Texture("tex-002", "Glossy", "", "Mirror-like gloss finish"),
                Texture("tex-009", "Engraved", "", "Custom engraved pattern")
            ),
            imageUrl           = "https://cdn.jewelryimages.net/static/domains/albertsjewelersga/images/store-pics/custom-jewelry-design-jpeg.webp"
        ) to Model3D(
            id = "model-003",
            productId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/NeilArmstrong.glb",
            previewImageUrl = "https://cdn.jewelryimages.net/static/domains/albertsjewelersga/images/store-pics/custom-jewelry-design-jpeg.webp",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
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
            imageUrl           = "https://i.etsystatic.com/39073443/r/il/4581b9/4774975542/il_570xN.4774975542_djm4.jpg"
        ) to Model3D(
            id = "model-004",
            productId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/Horse.glb",
            previewImageUrl = "https://i.etsystatic.com/39073443/r/il/4581b9/4774975542/il_570xN.4774975542_djm4.jpg",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
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
            imageUrl           = "https://kizik-ireland.com/cdn/shop/files/MATH2605_MensAthens2_DoeSkin-NavalAcademy_Lateral_shadow-2048x2048-ae00118.png"
        ) to Model3D(
            id = "model-005",
            productId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/ShopWindow.glb",
            previewImageUrl = "https://kizik-ireland.com/cdn/shop/files/MATH2605_MensAthens2_DoeSkin-NavalAcademy_Lateral_shadow-2048x2048-ae00118.png",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
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
            imageUrl           = "https://m.media-amazon.com/images/I/61Gz7tFv+iL._AC_UF894,1000_QL80_.jpg"
        ) to Model3D(
            id = "model-006",
            productId = "",
            modelUrl = "https://modelviewer.dev/shared-assets/models/reflective-sphere.glb",
            previewImageUrl = "https://m.media-amazon.com/images/I/61Gz7tFv+iL._AC_UF894,1000_QL80_.jpg",
            status = ModelStatus.READY,
            generatedAt = currentEpochMillis(),
        )
    )
}