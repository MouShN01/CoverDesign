package com.example.coverdesign.domain.repo

import com.example.coverdesign.data.dto.GenerateModelRequestDto
import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.util.Resource

interface Model3DRepository {

    // ── base model (3D tab — no generation) ──────────────────────────────────
    suspend fun getBaseModelForProduct(productId: String): Resource<Model3D>

    // ── generation pipeline ───────────────────────────────────────────────────

    /** POST — returns taskId immediately */
    suspend fun submitGeneration(request: GenerateModelRequestDto): Resource<String>

    /** GET — single status poll, called repeatedly by PollModelStatusUseCase */
    suspend fun getGenerationStatus(taskId: String): Resource<AdaptedModel3D>

    // ── local cache ───────────────────────────────────────────────────────────

    /** Lookup by configId — used when re-opening from history */
    suspend fun getAdaptedModel(configId: String): Resource<AdaptedModel3D?>

    /** All adapted models for a config — prosthetic cover can have multiple iterations */
    suspend fun getAllAdaptedModelsByConfigId(configId: String): Resource<List<AdaptedModel3D>>

    /** Lookup by adaptedModelId — used when viewing 3D model from an existing order */
    suspend fun getAdaptedModelById(id: String): Resource<AdaptedModel3D?>

    /** Lookup by taskId — used when resuming mid-generation */
    //suspend fun getModel(taskId: String): Resource<Model3D?>

    /** Persist / update after each poll */
    suspend fun saveModel(model: AdaptedModel3D): Resource<Unit>
}