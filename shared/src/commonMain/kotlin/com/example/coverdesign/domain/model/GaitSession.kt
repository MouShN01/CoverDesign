package com.example.coverdesign.domain.model

data class GaitSession(
    val id: String,
    val patientUid: String,
    val timestamp: Long,
    val durationSeconds: Int,
    val gaitAnalysis: GaitAnalysisResult,
    val pressureData: InsoleData?,  // null when patient has no smart insoles
    val notes: String = ""
)

data class GaitAnalysisResult(
    val postureScore: Float,       // 0-100: body posture accuracy
    val balanceScore: Float,       // 0-100: body balance
    val symmetryScore: Float,      // 0-100: gait left/right symmetry
    val scoliosisScore: Float,     // 0-100: lateral spine alignment; 100 = straight
    val positionSeries: List<BodyPosition>,
    val abnormalities: List<String>
)

data class BodyPosition(
    val timeMs: Long,
    val deviationX: Float,  // lateral deviation in cm  (0 = ideal)
    val deviationY: Float   // anterior-posterior deviation in cm
)