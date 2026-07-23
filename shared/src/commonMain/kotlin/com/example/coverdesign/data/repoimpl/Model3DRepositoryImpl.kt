package com.example.coverdesign.data.repoimpl

import com.example.coverdesign.data.dto.GenerateModelRequestDto
import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.local.toDomain
import com.example.coverdesign.data.local.toEntity
import com.example.coverdesign.data.remote.service.Model3DApiService
import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess

class Model3DRepositoryImpl(
    private val api: Model3DApiService,
    private val db: AppDatabase
) : Model3DRepository {

    // ── Base model — fetches existing asset, no generation ───────────────────

    override suspend fun getBaseModelForProduct(productId: String): Resource<Model3D> = try {
        val dto = api.getBaseModel(productId)
        Resource.Success(
            Model3D(
                configId = dto.id,
                status = ModelStatus.READY,
                modelUrl = dto.modelUrl,
                productId = productId,
                previewImageUrl = dto.previewImageUrl,
                generatedAt = dto.generatedAt,
                //   estimatedWaitSeconds = dto.estimatedWaitSeconds
            )
        )
    } catch (e: Exception) {
        Resource.Error("Failed to load base model: ${e.message}", e)
    }

    // ── Step 1: submit generation — returns taskId immediately ──────────────

    override suspend fun submitGeneration(
        request: GenerateModelRequestDto
    ): Resource<String> = try {
        val response = api.submitGeneration(request)
        println("submitGeneration $response")
        Resource.Success(response.taskId)
    } catch (e: Exception) {
        Resource.Error("Failed to submit generation: ${e.message}", e)
    }

    // ── Step 2: poll status — called repeatedly by PollModelStatusUseCase ────

    override suspend fun getGenerationStatus(taskId: String): Resource<AdaptedModel3D> = try {
        val dto = api.getGenerationStatus(taskId)
        val model = dto.toDomain()

        // persist to local DB on every poll so the app can resume if closed
        saveModel(model).onSuccess {
            println("loadAdaptedModel saved to local DB")
        }.onError { string, throwable ->
            println("getGenerationStatus Error $string")
            throw throwable ?: Exception(string)
        }

        Resource.Success(model)
    } catch (e: Exception) {
        Resource.Error("Failed to get generation status: ${e.message}", e)
    }

    // ── Local DB — adapted model (config pipeline) ────────────────────────────

    override suspend fun getAdaptedModel(configId: String): Resource<AdaptedModel3D?> = try {
        val entity = db.adaptedModel3DQueries
            .selectByConfigId(configId)
            .executeAsOneOrNull()
        Resource.Success(entity?.toDomain())
    } catch (e: Exception) {
        Resource.Error("Failed to load cached model: ${e.message}", e)
    }

    override suspend fun getAdaptedModelById(id: String): Resource<AdaptedModel3D?> = try {
        val entity = db.adaptedModel3DQueries
            .selectAdaptedById(id)
            .executeAsOneOrNull()
        Resource.Success(entity?.toDomain())
    } catch (e: Exception) {
        Resource.Error("Failed to load cached model by id: ${e.message}", e)
    }

//    override suspend fun getModel(taskId: String): Resource<Model3D?> = try {
//        val entity = db.model3DQueries
//            .selectByTaskId(taskId)
//            .executeAsOneOrNull()
//        Resource.Success(entity?.toDomain())
//    } catch (e: Exception) {
//        Resource.Error("Failed to load cached model: ${e.message}", e)
//    }

    override suspend fun saveModel(model: AdaptedModel3D): Resource<Unit> = try {
        db.adaptedModel3DQueries.insertOrReplaceAdaptedModel(
            model.toEntity()
        )

//           productId taskId = model.taskId,
//            configId = model.configId,
//            status = model.status.name,
//            modelUrl = model.modelUrl,
//            previewImageUrl = model.previewImageUrl,
//            estimatedWaitSeconds = model.estimatedWaitSeconds?.toLong(),
//            generatedAt = model.generatedAt

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Failed to save model: ${e.message}", e)
    }
}