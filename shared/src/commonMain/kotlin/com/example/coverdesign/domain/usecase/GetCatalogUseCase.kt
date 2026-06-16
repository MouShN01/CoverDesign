package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.repo.ProductRepository

// shared/commonMain/domain/usecase/GetCatalogUseCase.kt        FR1
class GetCatalogUseCase(private val repo: ProductRepository) {
    suspend operator fun invoke(): Resource<List<Product>> =
        repo.getCatalog()
}
