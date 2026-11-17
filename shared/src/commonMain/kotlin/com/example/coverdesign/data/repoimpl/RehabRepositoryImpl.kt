package com.example.coverdesign.data.repoimpl

import com.example.coverdesign.data.dto.SubmitRehabSessionDto
import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.remote.service.RehabApiService
import com.example.coverdesign.domain.model.RehabData
import com.example.coverdesign.domain.model.RehabTask
import com.example.coverdesign.domain.repo.RehabRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RehabRepositoryImpl(
    private val api: RehabApiService
) : RehabRepository {

    override suspend fun getRehabData(): Resource<RehabData> =
        withContext(Dispatchers.Default) {
            try {
                Resource.Success(api.getRehabData().toDomain())
            } catch (e: Exception) {
                Resource.Error("Failed to load rehab data: ${e.message}", e)
            }
        }

    override suspend fun submitSession(
        videoPath: String, durationSeconds: Int
    ): Resource<String> = withContext(Dispatchers.Default) {
        try {
            val task = api.submitSession(
                SubmitRehabSessionDto(videoPath = videoPath, durationSeconds = durationSeconds)
            )
            Resource.Success(task.taskId)
        } catch (e: Exception) {
            Resource.Error("Failed to submit session: ${e.message}", e)
        }
    }

    override suspend fun getSessionStatus(taskId: String): Resource<RehabTask> =
        withContext(Dispatchers.Default) {
            try {
                Resource.Success(api.getSessionStatus(taskId).toDomain())
            } catch (e: Exception) {
                Resource.Error("Failed to get session status: ${e.message}", e)
            }
        }
}
