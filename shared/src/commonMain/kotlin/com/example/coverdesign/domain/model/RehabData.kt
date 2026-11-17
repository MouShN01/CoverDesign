package com.example.coverdesign.domain.model

/** A patient's rehabilitation data as served by the backend. */
data class RehabData(
    val sessions: List<GaitSession> = emptyList(),
    val recommendations: List<DoctorRecommendation> = emptyList()
)

/** Processing state of an uploaded session recording. */
enum class RehabSessionStatus { PROCESSING, READY, FAILED }

/** Result of submitting a recorded video for gait processing. */
data class RehabTask(
    val taskId: String,
    val status: RehabSessionStatus,
    val session: GaitSession? = null   // populated once status == READY
)
