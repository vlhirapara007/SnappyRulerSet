package com.snappyrulerset

import com.snappyrulerset.domain.model.Point
import com.snappyrulerset.util.GeometryUtils
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.*

class GeometryUtilsTest {

    @Test
    fun testCalculateAngle() {
        val vertex = Point(0f, 0f)
        val point1 = Point(1f, 0f)
        val point2 = Point(0f, 1f)

        val angle = GeometryUtils.calculateAngle(point1, vertex, point2)
        assertEquals(90f, angle, 0.1f)
    }

    @Test
    fun testRotatePoint() {
        val point = Point(1f, 0f)
        val center = Point(0f, 0f)
        val rotatedPoint = GeometryUtils.rotatePoint(point, center, 90f)

        assertEquals(0f, rotatedPoint.x, 0.1f)
        assertEquals(1f, rotatedPoint.y, 0.1f)
    }

    @Test
    fun testSnapToGrid() {
        val point = Point(23f, 27f)
        val gridSize = 10f
        val snappedPoint = GeometryUtils.snapToGrid(point, gridSize)

        assertEquals(20f, snappedPoint.x, 0.1f)
        assertEquals(30f, snappedPoint.y, 0.1f)
    }
}
