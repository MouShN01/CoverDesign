package com.example.coverdesign.domain.usecase

import com.example.coverdesign.data.dto.GenerateModelRequestDto
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource

class GenerateProstheticCoverModelUseCase(
    private val model3dRepo: Model3DRepository
) {
    suspend operator fun invoke(request: GenerateModelRequestDto): Resource<String> {
        println("submitGeneration GenerateProstheticCoverModelUseCase invoke")
        return model3dRepo.submitGeneration(request)
    }
}