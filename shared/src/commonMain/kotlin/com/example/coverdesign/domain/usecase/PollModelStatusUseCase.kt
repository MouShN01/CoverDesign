package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PollModelStatusUseCase(
    private val model3dRepo: Model3DRepository
) {
    companion object {
        private const val DEFAULT_POLL_INTERVAL_MS = 3_000L
        private const val MAX_POLL_INTERVAL_MS = 15_000L
        private const val MAX_ATTEMPTS = 60    // ~3–15 min depending on interval
    }

    operator fun invoke(taskId: String): Flow<Resource<AdaptedModel3D>> = flow {
        var attempts = 0
        println("loadAdaptedModel PollModelStatusUseCase")
        while (attempts < MAX_ATTEMPTS) {
            val result = model3dRepo.getGenerationStatus(taskId)

            println("loadAdaptedModel result $result")

            emit(result)

            when (result) {
                is Resource.Success if result.data.status == ModelStatus.READY -> {
                    return@flow
                }
                is Resource.Success if result.data.status == ModelStatus.FAILED -> return@flow
                is Resource.Error -> {
                    return@flow
                }
                else -> {}
            }

            // use server hint if available, otherwise back off gradually
            val intervalMs = if (result is Resource.Success) {
                val hint = result.data.estimatedWaitSeconds
                if (hint != null && hint > 0)
                    minOf(hint * 1000L, MAX_POLL_INTERVAL_MS)
                else
                    minOf(DEFAULT_POLL_INTERVAL_MS * (attempts / 5 + 1), MAX_POLL_INTERVAL_MS)
            } else {
                DEFAULT_POLL_INTERVAL_MS
            }

            delay(intervalMs)
            attempts++
            println("loadAdaptedModel $attempts")
        }

        // exceeded max attempts
        emit(Resource.Error("Generation timed out — please try again"))
    }
}