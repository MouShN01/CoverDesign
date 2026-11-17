package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.RehabSessionStatus
import com.example.coverdesign.domain.model.RehabTask
import com.example.coverdesign.domain.repo.RehabRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PollRehabSessionUseCase(
    private val repo: RehabRepository,
) {
    companion object {
        private const val DEFAULT_POLL_INTERVAL_MS = 3_000L
        private const val MAX_POLL_INTERVAL_MS = 15_000L
        private const val MAX_ATTEMPTS = 60    // ~3–15 min depending on interval
    }

    operator fun invoke(taskId: String): Flow<Resource<RehabTask>> = flow {
        var attempts = 0
        while (attempts < MAX_ATTEMPTS) {
            val result = repo.getSessionStatus(taskId)
            emit(result)

            when (result) {
                is Resource.Success if result.data.status == RehabSessionStatus.READY -> return@flow
                is Resource.Success if result.data.status == RehabSessionStatus.FAILED -> return@flow
                is Resource.Error -> return@flow
                else -> {}
            }

            delay(minOf(DEFAULT_POLL_INTERVAL_MS * (attempts / 5 + 1), MAX_POLL_INTERVAL_MS))
            attempts++
        }
        emit(Resource.Error("Processing timed out — please try again"))
    }
}
