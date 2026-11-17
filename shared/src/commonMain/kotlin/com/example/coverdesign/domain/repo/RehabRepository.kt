package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.model.RehabData
import com.example.coverdesign.domain.model.RehabTask
import com.example.coverdesign.domain.util.Resource

// shared/commonMain/domain/repo/RehabRepository.kt
interface RehabRepository {

    /** Fetch the current user's gait sessions + doctor recommendations. */
    suspend fun getRehabData(): Resource<RehabData>

    /** Notify the backend of an uploaded session video; returns the processing task id. */
    suspend fun submitSession(videoPath: String, durationSeconds: Int): Resource<String>

    /** Poll processing status for a submitted session. */
    suspend fun getSessionStatus(taskId: String): Resource<RehabTask>
}
