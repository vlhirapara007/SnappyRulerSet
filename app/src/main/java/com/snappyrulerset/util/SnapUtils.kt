package com.snappyrulerset.util

import com.snappyrulerset.domain.model.Point
import com.snappyrulerset.domain.model.SnapCandidate
import com.snappyrulerset.domain.model.SnapType
import com.snappyrulerset.domain.model.AngleSnap
import kotlin.math.*

object SnapUtils {

    private val commonAngles = listOf(0f, 30f, 45f, 60f, 90f, 120f, 135f, 150f, 180f)

    fun calculateSnapRadius(zoomLevel: Float): Float {
        // Dynamic snap radius: larger at low zoom, smaller at high zoom
        return max(20f, 50f / zoomLevel)
    }

    fun findAngleSnap(currentAngle: Float): AngleSnap? {
        val normalizedAngle = normalizeAngle(currentAngle)

        return commonAngles.minByOrNull { abs(normalizedAngle - it) }?.let { snapAngle ->
            AngleSnap(normalizedAngle, snapAngle)
        }?.takeIf { it.isSnappable() }
    }

    private fun normalizeAngle(angle: Float): Float {
        var normalized = angle % 360f
        if (normalized < 0) normalized += 360f
        return normalized
    }

    fun selectBestSnapCandidate(
        candidates: List<SnapCandidate>,
        priorityTypes: List<SnapType> = listOf(
            SnapType.ENDPOINT,
            SnapType.ANGLE_90,
            SnapType.ANGLE_45,
            SnapType.MIDPOINT,
            SnapType.GRID
        )
    ): SnapCandidate? {
        if (candidates.isEmpty()) return null

        // First, try to find candidates with priority types
        for (priorityType in priorityTypes) {
            val priorityCandidates = candidates.filter { it.type == priorityType }
            if (priorityCandidates.isNotEmpty()) {
                return priorityCandidates.minByOrNull { it.distance }
            }
        }

        // If no priority candidates, return the closest one
        return candidates.minByOrNull { it.distance }
    }

    fun createVisualSnapFeedback(snapCandidate: SnapCandidate): SnapVisualFeedback {
        return SnapVisualFeedback(
            position = snapCandidate.point,
            type = snapCandidate.type,
            shouldHighlight = true,
            shouldShowTick = snapCandidate.type in listOf(
                SnapType.ANGLE_30, SnapType.ANGLE_45, SnapType.ANGLE_60, 
                SnapType.ANGLE_90, SnapType.ANGLE_120, SnapType.ANGLE_135, 
                SnapType.ANGLE_150, SnapType.ANGLE_180
            )
        )
    }
}

data class SnapVisualFeedback(
    val position: Point,
    val type: SnapType,
    val shouldHighlight: Boolean,
    val shouldShowTick: Boolean
)
