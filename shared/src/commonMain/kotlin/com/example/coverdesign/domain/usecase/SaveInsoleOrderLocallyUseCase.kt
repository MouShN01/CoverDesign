package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.InsoleOrderConfig
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.model.pricing.PricingEngine
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.util.Resource

class SaveInsoleOrderLocallyUseCase(private val repo: OrderRepository) {
    suspend operator fun invoke(
        config: InsoleOrderConfig,
        userPrice: PriceEstimate?,
        basePrice: Double
    ): Resource<Unit> {
        val price = userPrice ?: PricingEngine.calculateForInsole(config, basePrice)
        return repo.saveInsoleOrderLocally(config.copy(priceEstimate = price))
    }
}
