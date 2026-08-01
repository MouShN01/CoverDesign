package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.Resource.Loading
import com.example.coverdesign.domain.util.Resource.Success
import com.example.coverdesign.domain.util.currentEpochMillis
import com.example.coverdesign.domain.util.generateUuid

// ─────────────────────────────────────────────────────────────────────────────
// SaveOrderLocallyUseCase
//
// Creates a DRAFT Order in local SQLite right after the config is saved
// and before 3D generation is submitted.
//
// At this point:
//   - config is fully populated with Firebase Storage URLs (uploads done)
//   - adaptedModelId is null — model doesn't exist yet
//   - status is DRAFT — will become SUBMITTED on "Proceed to Order" tap
//
// PriceEstimate is recalculated here from the final config so the stored
// price always reflects what was shown to the user at submission time.
// ─────────────────────────────────────────────────────────────────────────────

class SaveOrderLocallyUseCase(
    private val orderRepo: OrderRepository
) {
    suspend operator fun invoke(
        config: ProductConfig,
        estimate: PriceEstimate? = null,
    ): Resource<Order> {

        val calcEstimate = estimate ?: PriceEstimate(
            configId = config.id,
            currency = "USD",
            basePrice = 0.0,
            materialSurcharges = emptyMap(),
            textureSurcharge = 0.0,
            inscriptionSurcharge = 0.0,
            elementSurcharges = emptyMap(),
            sizeMultiplier = 1.0,
            sizeMultiplierLabel = "",
            quantity = config.quantity,
            subtotal = 0.0,
            total = 0.0
        )
        val order = Order(
            id = generateUuid(),
            adaptedModelId = null,          // set later in ModelPreviewViewModel
            config = config,
            priceEstimate = calcEstimate,
            status = OrderStatus.DRAFT,
            createdAt = currentEpochMillis()
        )

        return when (val result = orderRepo.saveOrderLocally(order)) {
            is Resource.Success -> Success(order)
            is Resource.Error -> result
            Resource.Loading -> {
                Loading
            }
        }
    }
}