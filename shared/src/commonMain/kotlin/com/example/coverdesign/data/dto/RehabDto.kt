package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Top-level envelope ────────────────────────────────────────────────────────

@Serializable
data class RehabResponseDto(
    @SerialName("sessions")        val sessions:        List<GaitSessionDto>,
    @SerialName("recommendations") val recommendations: List<DoctorRecommendationDto>
)

// ── Session ───────────────────────────────────────────────────────────────────

@Serializable
data class GaitSessionDto(
    @SerialName("id")               val id:              String,
    @SerialName("patient_uid")      val patientUid:      String,
    @SerialName("timestamp_ms")     val timestampMs:     Long,
    @SerialName("duration_seconds") val durationSeconds: Int,
    @SerialName("gait_analysis")    val gaitAnalysis:    GaitAnalysisResultDto,
    @SerialName("pressure_data")    val pressureData:    InsoleDataDto? = null,
    @SerialName("notes")            val notes:           String = ""
)

// ── Gait analysis ─────────────────────────────────────────────────────────────

@Serializable
data class GaitAnalysisResultDto(
    @SerialName("posture_score")   val postureScore:   Float,   // 0–100
    @SerialName("balance_score")   val balanceScore:   Float,   // 0–100
    @SerialName("symmetry_score")  val symmetryScore:  Float,   // 0–100
    @SerialName("scoliosis_score") val scoliosisScore: Float,   // 0–100; 100 = straight
    @SerialName("position_series") val positionSeries: List<BodyPositionDto>,
    @SerialName("abnormalities")   val abnormalities:  List<String>
)

@Serializable
data class BodyPositionDto(
    @SerialName("time_ms")     val timeMs:     Long,
    @SerialName("deviation_x") val deviationX: Float,  // lateral cm
    @SerialName("deviation_y") val deviationY: Float   // anterior-posterior cm
)

// ── Insole / pressure data ────────────────────────────────────────────────────

@Serializable
data class InsoleDataDto(
    @SerialName("left_foot")       val leftFoot:      FootPressureMapDto,
    @SerialName("right_foot")      val rightFoot:     FootPressureMapDto,
    @SerialName("cop_trajectory")  val copTrajectory: List<CopPointDto>,
    @SerialName("step_count")      val stepCount:     Int,
    @SerialName("cadence")         val cadence:       Float   // steps/min
)

@Serializable
data class FootPressureMapDto(
    @SerialName("heel")     val heel:     Float,  // fraction 0–1
    @SerialName("midfoot")  val midfoot:  Float,
    @SerialName("forefoot") val forefoot: Float,
    @SerialName("toes")     val toes:     Float
)

@Serializable
data class CopPointDto(
    @SerialName("time_ms") val timeMs: Long,
    @SerialName("x")       val x:      Float,  // medial-lateral mm (+ = lateral)
    @SerialName("y")       val y:      Float   // anterior-posterior mm (+ = forward)
)

// ── Doctor recommendation ─────────────────────────────────────────────────────

@Serializable
data class DoctorRecommendationDto(
    @SerialName("id")           val id:          String,
    @SerialName("doctor_name")  val doctorName:  String,
    @SerialName("timestamp_ms") val timestampMs: Long,
    @SerialName("tag")          val tag:         String,  // "insole_based" | "photogrammetric"
    @SerialName("text")         val text:        String
)