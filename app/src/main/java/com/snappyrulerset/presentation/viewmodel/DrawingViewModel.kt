package com.snappyrulerset.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snappyrulerset.domain.model.*
import com.snappyrulerset.domain.usecase.DrawingUseCase
import com.snappyrulerset.domain.usecase.ExportUseCase
import com.snappyrulerset.presentation.drawing.DrawingState
import com.snappyrulerset.presentation.drawing.PrecisionHUDState
import com.snappyrulerset.presentation.drawing.ExportStatus
import com.snappyrulerset.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

class DrawingViewModel(
    private val drawingUseCase: DrawingUseCase,
    private val exportUseCase: ExportUseCase,
    private val dpiCalibration: DpiCalibration
) : ViewModel() {

    private val _state = MutableStateFlow(DrawingState())
    val state: StateFlow<DrawingState> = _state.asStateFlow()

    private val maxUndoSteps = 20

    fun onToolSelected(toolType: ToolType) {
        _state.value = _state.value.copy(
            currentTool = toolType,
            activeTool = if (toolType != ToolType.FREEHAND) {
                Tool(type = toolType, isActive = true)
            } else null
        )
    }

    fun onDrawingStart(point: Point) {
        val currentState = _state.value

        val snappedPoint = if (currentState.snapEnabled) {
            findBestSnapPoint(point) ?: point
        } else point

        // Save current state to undo stack
        saveToUndoStack()

        _state.value = currentState.copy(
            isDrawing = true,
            currentDrawingPoints = listOf(snappedPoint),
            precisionHUD = PrecisionHUDState(
                isVisible = true,
                position = snappedPoint
            )
        )

        updateSnapCandidates(snappedPoint)
    }

    fun onDrawingMove(point: Point) {
        val currentState = _state.value
        if (!currentState.isDrawing) return

        val snappedPoint = if (currentState.snapEnabled) {
            findBestSnapPoint(point) ?: point
        } else point

        val updatedPoints = currentState.currentDrawingPoints + snappedPoint

        // Update precision HUD
        val hudState = if (updatedPoints.size >= 2) {
            val startPoint = updatedPoints.first()
            val length = startPoint.distanceTo(snappedPoint)
            val angle = calculateAngle(startPoint, snappedPoint)

            PrecisionHUDState(
                isVisible = true,
                position = snappedPoint,
                lengthText = dpiCalibration.formatLength(length),
                angleText = dpiCalibration.formatAngle(angle),
                snapInfo = currentState.activeSnapCandidate?.type?.name ?: ""
            )
        } else currentState.precisionHUD

        _state.value = currentState.copy(
            currentDrawingPoints = updatedPoints,
            precisionHUD = hudState
        )

        updateSnapCandidates(snappedPoint)
    }

    fun onDrawingEnd(point: Point) {
        val currentState = _state.value
        if (!currentState.isDrawing) return

        val snappedPoint = if (currentState.snapEnabled) {
            findBestSnapPoint(point) ?: point
        } else point

        val finalPoints = currentState.currentDrawingPoints + snappedPoint

        // Create shape based on current tool
        when (currentState.currentTool) {
            ToolType.FREEHAND -> {
                if (finalPoints.size >= 2) {
                    drawingUseCase.addFreehandPath(finalPoints)
                }
            }
            ToolType.RULER -> {
                if (finalPoints.size >= 2) {
                    drawingUseCase.addLine(finalPoints.first(), finalPoints.last())
                }
            }
            ToolType.COMPASS -> {
                if (finalPoints.size >= 2) {
                    val center = finalPoints.first()
                    val radius = center.distanceTo(finalPoints.last())
                    drawingUseCase.addCircle(center, radius)
                }
            }
            else -> {
                // Handle other tools
                if (finalPoints.size >= 2) {
                    drawingUseCase.addLine(finalPoints.first(), finalPoints.last())
                }
            }
        }

        _state.value = currentState.copy(
            isDrawing = false,
            currentDrawingPoints = emptyList(),
            shapes = drawingUseCase.getAllShapes(),
            snapCandidates = emptyList(),
            activeSnapCandidate = null,
            snapVisualFeedback = null,
            precisionHUD = PrecisionHUDState()
        )
    }

    fun onSnapToggled(enabled: Boolean) {
        _state.value = _state.value.copy(snapEnabled = enabled)
    }

    fun onUndo() {
        val currentState = _state.value
        if (currentState.undoStack.isNotEmpty()) {
            val previousShapes = currentState.undoStack.last()
            val newUndoStack = currentState.undoStack.dropLast(1)
            val newRedoStack = currentState.redoStack + listOf(currentState.shapes)

            // Restore shapes in repository
            drawingUseCase.clearAll()
            previousShapes.forEach { shape ->
                when (shape) {
                    is Shape.Line -> drawingUseCase.addLine(shape.start, shape.end)
                    is Shape.FreehandPath -> drawingUseCase.addFreehandPath(shape.points)
                    is Shape.Circle -> drawingUseCase.addCircle(shape.center, shape.radius)
                }
            }

            _state.value = currentState.copy(
                shapes = previousShapes,
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                canUndo = newUndoStack.isNotEmpty(),
                canRedo = true
            )
        }
    }

    fun onRedo() {
        val currentState = _state.value
        if (currentState.redoStack.isNotEmpty()) {
            val nextShapes = currentState.redoStack.last()
            val newRedoStack = currentState.redoStack.dropLast(1)
            val newUndoStack = currentState.undoStack + listOf(currentState.shapes)

            // Restore shapes in repository
            drawingUseCase.clearAll()
            nextShapes.forEach { shape ->
                when (shape) {
                    is Shape.Line -> drawingUseCase.addLine(shape.start, shape.end)
                    is Shape.FreehandPath -> drawingUseCase.addFreehandPath(shape.points)
                    is Shape.Circle -> drawingUseCase.addCircle(shape.center, shape.radius)
                }
            }

            _state.value = currentState.copy(
                shapes = nextShapes,
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                canUndo = true,
                canRedo = newRedoStack.isNotEmpty()
            )
        }
    }

    fun onExport(width: Int, height: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(exportStatus = ExportStatus.Exporting)

            try {
                val filePath = exportUseCase.exportDrawing(
                    shapes = _state.value.shapes,
                    width = width,
                    height = height
                )

                _state.value = _state.value.copy(
                    exportStatus = if (filePath != null) {
                        ExportStatus.Success(filePath)
                    } else {
                        ExportStatus.Error("Failed to export drawing")
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    exportStatus = ExportStatus.Error(e.message ?: "Unknown error")
                )
            }
        }
    }

    fun onZoomChanged(newZoomLevel: Float) {
        _state.value = _state.value.copy(zoomLevel = newZoomLevel)
    }

    fun onCanvasOffsetChanged(newOffset: Point) {
        _state.value = _state.value.copy(canvasOffset = newOffset)
    }

    private fun saveToUndoStack() {
        val currentState = _state.value
        var newUndoStack = currentState.undoStack + listOf(currentState.shapes)

        // Limit undo stack size
        if (newUndoStack.size > maxUndoSteps) {
            newUndoStack = newUndoStack.drop(1)
        }

        _state.value = currentState.copy(
            undoStack = newUndoStack,
            redoStack = emptyList(), // Clear redo stack on new action
            canUndo = true,
            canRedo = false
        )
    }

    private fun findBestSnapPoint(targetPoint: Point): Point? {
        val snapRadius = SnapUtils.calculateSnapRadius(_state.value.zoomLevel)
        val candidates = drawingUseCase.findSnapCandidates(
            targetPoint = targetPoint,
            snapRadius = snapRadius,
            gridSize = _state.value.gridSize
        )

        val bestCandidate = drawingUseCase.getBestSnapCandidate(candidates)

        _state.value = _state.value.copy(
            snapCandidates = candidates,
            activeSnapCandidate = bestCandidate,
            snapVisualFeedback = bestCandidate?.let { 
                SnapUtils.createVisualSnapFeedback(it) 
            }
        )

        return bestCandidate?.point
    }

    private fun updateSnapCandidates(targetPoint: Point) {
        if (!_state.value.snapEnabled) return

        val snapRadius = SnapUtils.calculateSnapRadius(_state.value.zoomLevel)
        val candidates = drawingUseCase.findSnapCandidates(
            targetPoint = targetPoint,
            snapRadius = snapRadius,
            gridSize = _state.value.gridSize
        )

        val bestCandidate = drawingUseCase.getBestSnapCandidate(candidates)

        _state.value = _state.value.copy(
            snapCandidates = candidates,
            activeSnapCandidate = bestCandidate,
            snapVisualFeedback = bestCandidate?.let { 
                SnapUtils.createVisualSnapFeedback(it) 
            }
        )
    }

    private fun calculateAngle(start: Point, end: Point): Float {
        return Math.toDegrees(atan2(end.y - start.y, end.x - start.x).toDouble()).toFloat()
    }
}
