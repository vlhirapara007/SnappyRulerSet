package com.snappyrulerset.domain.usecase

import com.snappyrulerset.data.repository.DrawingRepository
import com.snappyrulerset.domain.model.Shape
import com.snappyrulerset.domain.model.Point
import com.snappyrulerset.domain.model.SnapCandidate
import com.snappyrulerset.util.GeometryUtils
import com.snappyrulerset.util.SnapUtils
import java.util.UUID

class DrawingUseCase(private val repository: DrawingRepository) {

    fun addFreehandPath(points: List<Point>) {
        if (points.size >= 2) {
            val shape = Shape.FreehandPath(
                id = UUID.randomUUID().toString(),
                points = points
            )
            repository.addShape(shape)
        }
    }

    fun addLine(start: Point, end: Point) {
        val shape = Shape.Line(
            id = UUID.randomUUID().toString(),
            start = start,
            end = end
        )
        repository.addShape(shape)
    }

    fun addCircle(center: Point, radius: Float) {
        val shape = Shape.Circle(
            id = UUID.randomUUID().toString(),
            center = center,
            radius = radius
        )
        repository.addShape(shape)
    }

    fun getAllShapes(): List<Shape> = repository.getAllShapes()

    fun findSnapCandidates(
        targetPoint: Point,
        snapRadius: Float,
        gridSize: Float
    ): List<SnapCandidate> {
        return GeometryUtils.findNearestSnapCandidates(
            targetPoint = targetPoint,
            shapes = repository.getAllShapes(),
            snapRadius = snapRadius,
            gridSize = gridSize
        )
    }

    fun getBestSnapCandidate(candidates: List<SnapCandidate>): SnapCandidate? {
        return SnapUtils.selectBestSnapCandidate(candidates)
    }

    fun clearAll() = repository.clearAll()
}

class ExportUseCase(private val repository: DrawingRepository) {

    suspend fun exportDrawing(
        shapes: List<Shape>,
        width: Int,
        height: Int,
        filename: String = "drawing_${System.currentTimeMillis()}"
    ): String? {
        val bitmap = repository.exportToBitmap(shapes, width, height)
        return repository.saveBitmapToFile(bitmap, filename)
    }
}
