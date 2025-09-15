package com.snappyrulerset.presentation.drawing

import com.snappyrulerset.domain.model.*
import com.snappyrulerset.util.SnapVisualFeedback

data class DrawingState(
    val shapes: List<Shape> = emptyList(),
    val currentTool: ToolType = ToolType.FREEHAND,
    val activeTool: Tool? = null,
    val isDrawing: Boolean = false,
    val currentDrawingPoints: List<Point> = emptyList(),
    val snapEnabled: Boolean = true,
    val snapCandidates: List<SnapCandidate> = emptyList(),
    val activeSnapCandidate: SnapCandidate? = null,
    val snapVisualFeedback: SnapVisualFeedback? = null,
    val precisionHUD: PrecisionHUDState = PrecisionHUDState(),
    val undoStack: List<List<Shape>> = emptyList(),
    val redoStack: List<List<Shape>> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val zoomLevel: Float = 1f,
    val canvasOffset: Point = Point(0f, 0f),
    val gridSize: Float = 50f, // 5mm in pixels (approximate)
    val exportStatus: ExportStatus = ExportStatus.Idle
)

data class PrecisionHUDState(
    val isVisible: Boolean = false,
    val position: Point = Point(0f, 0f),
    val lengthText: String = "",
    val angleText: String = "",
    val snapInfo: String = ""
)

sealed class ExportStatus {
    object Idle : ExportStatus()
    object Exporting : ExportStatus()
    data class Success(val filePath: String) : ExportStatus()
    data class Error(val message: String) : ExportStatus()
}
