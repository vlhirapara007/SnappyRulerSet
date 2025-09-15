package com.snappyrulerset.domain.model

import androidx.compose.ui.graphics.Path

sealed class Shape {
    abstract val id: String
    abstract val points: List<Point>
    abstract fun toPath(): Path

    data class Line(
        override val id: String,
        val start: Point,
        val end: Point
    ) : Shape() {
        override val points: List<Point> = listOf(start, end)

        override fun toPath(): Path {
            return Path().apply {
                moveTo(start.x, start.y)
                lineTo(end.x, end.y)
            }
        }

        fun length(): Float = start.distanceTo(end)
        fun midpoint(): Point = Point((start.x + end.x) / 2, (start.y + end.y) / 2)
    }

    data class FreehandPath(
        override val id: String,
        override val points: List<Point>
    ) : Shape() {
        override fun toPath(): Path {
            val path = Path()
            if (points.isNotEmpty()) {
                path.moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point ->
                    path.lineTo(point.x, point.y)
                }
            }
            return path
        }
    }

    data class Circle(
        override val id: String,
        val center: Point,
        val radius: Float
    ) : Shape() {
        override val points: List<Point> = listOf(center)

        override fun toPath(): Path {
            val path = Path()
            path.addOval(androidx.compose.ui.geometry.Rect(
                center.x - radius,
                center.y - radius,
                center.x + radius,
                center.y + radius
            ))
            return path
        }
    }
}
