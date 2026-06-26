package com.example.coverdesign.data.repoimpl

import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.local.toDomain
import com.example.coverdesign.data.local.toEntity
import com.example.coverdesign.data.remote.service.Model3DApiService
import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.currentEpochMillis
import com.example.coverdesign.domain.util.generateUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

// shared/commonMain/data/repoimpl/Model3DRepositoryImpl.kt
class Model3DRepositoryImplTest(
    private val api: Model3DApiService,
    private val db: AppDatabase
) : Model3DRepository {

    override suspend fun getBaseModelForProduct(productId: String): Resource<Model3D> =
        withContext(Dispatchers.Default) {
            val local = db.model3DQueries.selectByProductId(productId).executeAsOneOrNull()
            if (local != null) Resource.Success(local.toDomain())
            else Resource.Error("No base model found for product $productId")
        }
// TODO !!!! HEERREE ???!!! generateAdaptedModel
// shared/commonMain/data/repoimpl/Model3DRepositoryImpl.kt — temporary local-only version
override suspend fun generateAdaptedModel(config: ProductConfig): Resource<AdaptedModel3D> =
    withContext(Dispatchers.Default) {
        val baseModel = db.model3DQueries
            .selectByProductId(config.productId)
            .executeAsOneOrNull()
            ?: return@withContext Resource.Error("No base model found for product ${config.productId}")

        // STUB — replace with the real api.generateAdaptedModel(...) call once backend exists
        val adapted = AdaptedModel3D(
            id                   = generateUuid(),
            configId             = config.id,
            baseModelId          = baseModel.id,
            modelUrl             = baseModel.modelUrl,       // pretend "adapted" = same glb for now
            previewImageUrl      = baseModel.previewImageUrl,
            status               = ModelStatus.READY,         // skip PENDING/GENERATING entirely
            generatedAt          = currentEpochMillis(),
            estimatedWaitSeconds = null
        )

        db.adaptedModel3DQueries.insertOrReplaceAdaptedModel(adapted.toEntity())
        Resource.Success(adapted)
    }

    override suspend fun getAdaptedModel(configId: String): Resource<AdaptedModel3D> =
        withContext(Dispatchers.Default) {
            val local = db.adaptedModel3DQueries.selectByConfigId(configId).executeAsOneOrNull()
            if (local != null) Resource.Success(local.toDomain())
            else Resource.Error("No adapted model found for config $configId")
        }

    override fun getAdaptedModelStatus(adaptedModelId: String): Flow<ModelStatus> = flow {
        while (true) {
            val status = try {
                val dto = api.getAdaptedModelStatus(adaptedModelId)
                val s = ModelStatus.valueOf(dto.status.uppercase())
                db.adaptedModel3DQueries.updateAdaptedStatus(status = s.name, id = adaptedModelId)
                s
            } catch (e: Exception) { ModelStatus.FAILED }
            emit(status)
            if (status == ModelStatus.READY || status == ModelStatus.FAILED) break
            delay(2_000L)
        }
    }

    override suspend fun getTextures(): Resource<List<Texture>> =
        TODO()
}