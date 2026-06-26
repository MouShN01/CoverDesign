package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.repo.ProductRepository

class CalculatePriceUseCase(private val repo: ProductRepository) {
    suspend operator fun invoke(config: ProductConfig): Resource<PriceEstimate> =
        repo.calculatePrice(config)
}