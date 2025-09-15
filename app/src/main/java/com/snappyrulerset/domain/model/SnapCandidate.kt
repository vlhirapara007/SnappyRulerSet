package com.snappyrulerset.domain.model

import kotlin.math.abs

data class SnapCandidate(
    val point: Point,
    val type: SnapType,
    val distance: Float,
    val confidence: Float = 1f,
    val metadata: Map<String, Any> = emptyMap()
) {
    fun isWithinThreshold(threshold: Float): Boolean = distance <= threshold
}

enum class SnapType {
    GRID,
    ENDPOINT,
    MIDPOINT,
    INTERSECTION,
    ANGLE_30,
    ANGLE_45,
    ANGLE_60,
    ANGLE_90,
    ANGLE_120,
    ANGLE_135,
    ANGLE_150,
    ANGLE_180
}

data class AngleSnap(
    val angle: Float,
    val snapAngle: Float,
    val tolerance: Float = 2.5f
) {
    fun isSnappable(): Boolean = abs(angle - snapAngle) <= tolerance
}
