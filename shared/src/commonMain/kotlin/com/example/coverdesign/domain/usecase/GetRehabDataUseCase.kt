package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.RehabData
import com.example.coverdesign.domain.repo.RehabRepository
import com.example.coverdesign.domain.util.Resource

class GetRehabDataUseCase(private val repo: RehabRepository) {
    suspend operator fun invoke(): Resource<RehabData> = repo.getRehabData()
}
