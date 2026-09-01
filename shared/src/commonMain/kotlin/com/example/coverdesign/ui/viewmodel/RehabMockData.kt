package com.example.coverdesign.ui.viewmodel

import com.example.coverdesign.domain.model.BodyPosition
import com.example.coverdesign.domain.model.CopPoint
import com.example.coverdesign.domain.model.DoctorRecommendation
import com.example.coverdesign.domain.model.FootPressureMap
import com.example.coverdesign.domain.model.GaitAnalysisResult
import com.example.coverdesign.domain.model.GaitSession
import com.example.coverdesign.domain.model.InsoleData
import com.example.coverdesign.domain.model.RecommendationTag
import com.example.coverdesign.domain.util.currentEpochMillis
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

object RehabMockData {

    private val DAY_MS = 86_400_000L

    fun generateSessions(): List<GaitSession> {
        val now = currentEpochMillis()
        return listOf(
            buildSession("s1", now,            postureScore = 85f, balanceScore = 87f, symmetryScore = 83f, durationSec = 420),
            buildSession("s2", now - 7 * DAY_MS,  postureScore = 78f, balanceScore = 80f, symmetryScore = 76f, durationSec = 390),
            buildSession("s3", now - 14 * DAY_MS, postureScore = 70f, balanceScore = 74f, symmetryScore = 72f, durationSec = 360),
            buildSession("s4", now - 21 * DAY_MS, postureScore = 63f, balanceScore = 68f, symmetryScore = 65f, durationSec = 330),
            buildSession("s5", now - 28 * DAY_MS, postureScore = 55f, balanceScore = 60f, symmetryScore = 58f, durationSec = 300),
        )
    }

    // COP lissajous: x = A·sin(t), y = B·sin(2t + π/4) — no Random needed
    fun generateCopTrajectory(n: Int = 120): List<CopPoint> {
        val twoPI = 2.0 * PI
        return (0 until n).map { i ->
            val t = i.toDouble() * twoPI / n
            CopPoint(
                timeMs = i * 100L,
                x = (30.0 * sin(t)).toFloat(),
                y = (20.0 * sin(2.0 * t + PI / 4.0)).toFloat()
            )
        }
    }

    // Body deviation: damped sine (patient correcting posture over a session)
    private fun generateDeviationSeries(n: Int = 60): List<BodyPosition> {
        val twoPI = 2.0 * PI
        return (0 until n).map { i ->
            val t = i.toDouble() / n.toDouble()
            BodyPosition(
                timeMs = i * 300L,
                deviationX = (10.0 * exp(-2.0 * t) * sin(6.0 * PI * t)).toFloat(),
                deviationY = (6.0 * exp(-1.5 * t) * cos(5.0 * PI * t)).toFloat()
            )
        }
    }

    fun generateRecommendations(): List<DoctorRecommendation> {
        val now = currentEpochMillis()
        return listOf(
            DoctorRecommendation(
                id = "r1",
                doctorName = "Dr. Rossi",
                timestampMs = now - 2 * DAY_MS,
                tag = RecommendationTag.INSOLE_BASED,
                text = "Adjust left insole angle by 2 degrees. Continue current exercises and increase walking duration to 30 min daily."
            ),
            DoctorRecommendation(
                id = "r2",
                doctorName = "Dr. Rossi",
                timestampMs = now - 7 * DAY_MS,
                tag = RecommendationTag.PHOTOGRAMMETRIC,
                text = "Posture correction exercises showing good results. Maintain current routine and focus on shoulder alignment."
            )
        )
    }

    private fun buildSession(
        id: String,
        timestamp: Long,
        postureScore: Float,
        balanceScore: Float,
        symmetryScore: Float,
        durationSec: Int
    ) = GaitSession(
        id = id,
        patientUid = "mock_patient",
        timestamp = timestamp,
        durationSeconds = durationSec,
        gaitAnalysis = GaitAnalysisResult(
            postureScore = postureScore,
            balanceScore = balanceScore,
            symmetryScore = symmetryScore,
            positionSeries = generateDeviationSeries(),
            abnormalities = if (postureScore < 70f) listOf("Slight anterior lean detected") else emptyList()
        ),
        pressureData = InsoleData(
            leftFoot = FootPressureMap(heel = 0.35f, midfoot = 0.15f, forefoot = 0.35f, toes = 0.15f),
            rightFoot = FootPressureMap(heel = 0.33f, midfoot = 0.17f, forefoot = 0.34f, toes = 0.16f),
            copTrajectory = generateCopTrajectory(),
            stepCount = (durationSec / 0.6f).toInt(),
            cadence = 100f
        )
    )
}
