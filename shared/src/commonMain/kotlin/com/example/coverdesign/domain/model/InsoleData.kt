package com.example.coverdesign.domain.model

data class InsoleData(
    val leftFoot: FootPressureMap,
    val rightFoot: FootPressureMap,
    val copTrajectory: List<CopPoint>,
    val stepCount: Int,
    val cadence: Float  // steps/min
)

data class FootPressureMap(
    val heel: Float,
    val midfoot: Float,
    val forefoot: Float,
    val toes: Float
)

data class CopPoint(
    val timeMs: Long,
    val x: Float,  // medial-lateral mm (+ = lateral)
    val y: Float   // anterior-posterior mm (+ = forward)
)
