package com.snappyrulerset.domain.model

import kotlin.math.sqrt

data class Point(val x: Float, val y: Float) {
    fun distanceTo(other: Point): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)
    operator fun minus(other: Point): Point = Point(x - other.x, y - other.y)
    operator fun times(scalar: Float): Point = Point(x * scalar, y * scalar)

    companion object {
        val Zero = Point(0f, 0f)
    }
}
