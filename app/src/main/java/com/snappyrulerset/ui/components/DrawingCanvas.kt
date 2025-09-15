package com.snappyrulerset.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import com.snappyrulerset.domain.model.*
import com.snappyrulerset.presentation.drawing.DrawingState
import com.snappyrulerset.ui.theme.*
import com.snappyrulerset.util.SnapVisualFeedback

@Composable
fun DrawingCanvas(
    state: DrawingState,
    onDrawingStart: (Point) -> Unit,
    onDrawingEnd: (Point) -> Unit,
    onZoomChanged: (Float) -> Unit,
    onOffsetChanged: (Point) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDrawingStart(Point(offset.x, offset.y))
                    },
                    onDrag = { change, dragAmount ->
                        // Freehand drawing: add points as finger moves
                        if (change.pressed) {
                            onDrawingStart(Point(change.position.x, change.position.y))
                        }
                        // TODO: If a tool is selected, handle tool movement/rotation here
                        // TODO: Implement snapping logic here (to grid, points, angles, etc.)
                    },
                    onDragEnd = {
                        // End the current drawing stroke or tool manipulation
                        onDrawingEnd(Point.Zero) // Use fully qualified name
                        // TODO: Finalize snapping, commit shape/tool position
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        onDrawingStart(Point(offset.x, offset.y))
                        tryAwaitRelease()
                        onDrawingEnd(Point(offset.x, offset.y))
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom != 1f) {
                        val newZoom = (state.zoomLevel * zoom).coerceIn(0.5f, 5f)
                        onZoomChanged(newZoom)
                    }

                    if (pan != Offset.Zero) {
                        val newOffset = Point(
                            state.canvasOffset.x + pan.x,
                            state.canvasOffset.y + pan.y
                        )
                        onOffsetChanged(newOffset)
                    }
                }
            }
    ) {
        // Draw grid
        drawGrid(state.gridSize, state.canvasOffset, state.zoomLevel)

        // Draw all shapes
        state.shapes.forEach { shape ->
            drawShape(shape, state.canvasOffset, state.zoomLevel)
        }

        // Draw current drawing path
        if (state.isDrawing && state.currentDrawingPoints.isNotEmpty()) {
            drawCurrentPath(state.currentDrawingPoints, state.canvasOffset, state.zoomLevel)
        }

        // Draw snap visual feedback
        state.snapVisualFeedback?.let { feedback ->
            drawSnapFeedback(feedback, state.canvasOffset, state.zoomLevel)
        }

        // Draw tools
        state.activeTool?.let { tool ->
            drawTool(tool, state.canvasOffset, state.zoomLevel)
        }
    }
}

private fun DrawScope.drawGrid(
    gridSize: Float,
    offset: Point,
    zoom: Float
) {
    val adjustedGridSize = gridSize * zoom
    val startX = (-offset.x % adjustedGridSize)
    val startY = (-offset.y % adjustedGridSize)

    // Draw vertical lines
    var x = startX
    while (x < size.width) {
        drawLine(
            color = GridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 0.5.dp.toPx()
        )
        x += adjustedGridSize
    }

    // Draw horizontal lines
    var y = startY
    while (y < size.height) {
        drawLine(
            color = GridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 0.5.dp.toPx()
        )
        y += adjustedGridSize
    }
}

private fun DrawScope.drawShape(
    shape: com.snappyrulerset.domain.model.Shape,
    offset: Point,
    zoom: Float
) {
    when (shape) {
        is com.snappyrulerset.domain.model.Shape.Line -> {
            val start = transformPoint(shape.start, offset, zoom)
            val end = transformPoint(shape.end, offset, zoom)
            drawLine(
                color = DrawingBlack,
                start = Offset(start.x, start.y),
                end = Offset(end.x, end.y),
                strokeWidth = 2.dp.toPx()
            )
        }
        is com.snappyrulerset.domain.model.Shape.FreehandPath -> {
            if (shape.points.size >= 2) {
                val path = Path()
                val transformedPoints = shape.points.map { transformPoint(it, offset, zoom) }

                path.moveTo(transformedPoints.first().x, transformedPoints.first().y)
                transformedPoints.drop(1).forEach { point ->
                    path.lineTo(point.x, point.y)
                }

                drawPath(
                    path = path,
                    color = DrawingBlack,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        is com.snappyrulerset.domain.model.Shape.Circle -> {
            val center = transformPoint(shape.center, offset, zoom)
            val radius = shape.radius * zoom
            drawCircle(
                color = DrawingBlack,
                radius = radius,
                center = Offset(center.x, center.y),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawCurrentPath(
    points: List<Point>,
    offset: Point,
    zoom: Float
) {
    if (points.size >= 2) {
        val path = Path()
        val transformedPoints = points.map { transformPoint(it, offset, zoom) }

        path.moveTo(transformedPoints.first().x, transformedPoints.first().y)
        transformedPoints.drop(1).forEach { point ->
            path.lineTo(point.x, point.y)
        }

        drawPath(
            path = path,
            color = DrawingGray,
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        )
    }
}

private fun DrawScope.drawSnapFeedback(
    feedback: SnapVisualFeedback,
    offset: Point,
    zoom: Float
) {
    val transformedPoint = transformPoint(feedback.position, offset, zoom)
    val center = Offset(transformedPoint.x, transformedPoint.y)

    if (feedback.shouldHighlight) {
        // Draw highlight circle
        drawCircle(
            color = SnapHighlight,
            radius = 8.dp.toPx(),
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }

    if (feedback.shouldShowTick) {
        // Draw tick mark
        drawLine(
            color = SnapTick,
            start = Offset(center.x - 6.dp.toPx(), center.y),
            end = Offset(center.x + 6.dp.toPx(), center.y),
            strokeWidth = 3.dp.toPx()
        )
        drawLine(
            color = SnapTick,
            start = Offset(center.x, center.y - 6.dp.toPx()),
            end = Offset(center.x, center.y + 6.dp.toPx()),
            strokeWidth = 3.dp.toPx()
        )
    }
}

private fun DrawScope.drawTool(
    tool: Tool,
    offset: Point,
    zoom: Float
) {
    // This would draw virtual tools like rulers, protractors, etc.
    // For now, just draw a placeholder
    when (tool.type) {
        ToolType.RULER -> {
            // Draw ruler representation
            val toolCenter = transformPoint(tool.position, offset, zoom)
            drawRect(
                color = SnapHighlight.copy(alpha = 0.5f),
                topLeft = Offset(toolCenter.x - 100.dp.toPx(), toolCenter.y - 10.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(200.dp.toPx(), 20.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        else -> {
            // Handle other tools
        }
    }
}

private fun transformPoint(point: Point, offset: Point, zoom: Float): Point {
    return Point(
        (point.x + offset.x) * zoom,
        (point.y + offset.y) * zoom
    )
}
