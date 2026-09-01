package com.example.coverdesign.domain.model

data class DoctorRecommendation(
    val id: String,
    val doctorName: String,
    val timestampMs: Long,
    val tag: RecommendationTag,
    val text: String
)

enum class RecommendationTag {
    INSOLE_BASED,
    PHOTOGRAMMETRIC
}
