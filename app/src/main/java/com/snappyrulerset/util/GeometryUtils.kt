package com.snappyrulerset.util

import com.snappyrulerset.domain.model.Point
import com.snappyrulerset.domain.model.Shape
import com.snappyrulerset.domain.model.SnapCandidate
import com.snappyrulerset.domain.model.SnapType
import kotlin.math.*

object GeometryUtils {

    fun calculateAngle(point1: Point, vertex: Point, point2: Point): Float {
        val angle1 = atan2(point1.y - vertex.y, point1.x - vertex.x)
        val angle2 = atan2(point2.y - vertex.y, point2.x - vertex.x)
        var angle = Math.toDegrees((angle2 - angle1).toDouble()).toFloat()

        if (angle < 0) angle += 360f
        return angle
    }

    fun rotatePoint(point: Point, center: Point, angleDegrees: Float): Point {
        val angleRad = Math.toRadians(angleDegrees.toDouble())
        val cos = cos(angleRad).toFloat()
        val sin = sin(angleRad).toFloat()

        val translatedX = point.x - center.x
        val translatedY = point.y - center.y

        val rotatedX = translatedX * cos - translatedY * sin
        val rotatedY = translatedX * sin + translatedY * cos

        return Point(rotatedX + center.x, rotatedY + center.y)
    }

    fun findLineIntersection(line1Start: Point, line1End: Point, 
                           line2Start: Point, line2End: Point): Point? {
        val x1 = line1Start.x
        val y1 = line1Start.y
        val x2 = line1End.x
        val y2 = line1End.y
        val x3 = line2Start.x
        val y3 = line2Start.y
        val x4 = line2End.x
        val y4 = line2End.y

        val denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
        if (abs(denom) < 1e-10) return null // Lines are parallel

        val t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom
        val u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / denom

        if (t in 0.0..1.0 && u in 0.0..1.0) {
            val intersectionX = x1 + t * (x2 - x1)
            val intersectionY = y1 + t * (y2 - y1)
            return Point(intersectionX, intersectionY)
        }

        return null
    }

    fun snapToGrid(point: Point, gridSize: Float): Point {
        val snappedX = (point.x / gridSize).roundToInt() * gridSize
        val snappedY = (point.y / gridSize).roundToInt() * gridSize
        return Point(snappedX, snappedY)
    }

    fun findNearestSnapCandidates(
        targetPoint: Point,
        shapes: List<Shape>,
        snapRadius: Float,
        gridSize: Float
    ): List<SnapCandidate> {
        val candidates = mutableListOf<SnapCandidate>()

        // Grid snapping
        val gridSnap = snapToGrid(targetPoint, gridSize)
        val gridDistance = targetPoint.distanceTo(gridSnap)
        if (gridDistance <= snapRadius) {
            candidates.add(SnapCandidate(gridSnap, SnapType.GRID, gridDistance))
        }

        // Shape-based snapping
        shapes.forEach { shape ->
            when (shape) {
                is Shape.Line -> {
                    // Endpoint snapping
                    val startDistance = targetPoint.distanceTo(shape.start)
                    if (startDistance <= snapRadius) {
                        candidates.add(SnapCandidate(shape.start, SnapType.ENDPOINT, startDistance))
                    }

                    val endDistance = targetPoint.distanceTo(shape.end)
                    if (endDistance <= snapRadius) {
                        candidates.add(SnapCandidate(shape.end, SnapType.ENDPOINT, endDistance))
                    }

                    // Midpoint snapping
                    val midpoint = shape.midpoint()
                    val midDistance = targetPoint.distanceTo(midpoint)
                    if (midDistance <= snapRadius) {
                        candidates.add(SnapCandidate(midpoint, SnapType.MIDPOINT, midDistance))
                    }
                }
                is Shape.Circle -> {
                    val centerDistance = targetPoint.distanceTo(shape.center)
                    if (centerDistance <= snapRadius) {
                        candidates.add(SnapCandidate(shape.center, SnapType.ENDPOINT, centerDistance))
                    }
                }
                else -> {
                    // Handle other shapes
                }
            }
        }

        return candidates.sortedBy { it.distance }
    }
}
