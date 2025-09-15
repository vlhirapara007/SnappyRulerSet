package com.snappyrulerset.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.snappyrulerset.data.repository.DrawingRepository
import com.snappyrulerset.domain.usecase.DrawingUseCase
import com.snappyrulerset.domain.usecase.ExportUseCase
import com.snappyrulerset.presentation.drawing.ExportStatus
import com.snappyrulerset.presentation.viewmodel.DrawingViewModel
import com.snappyrulerset.ui.components.DrawingCanvas
import com.snappyrulerset.ui.components.PrecisionHUD
import com.snappyrulerset.ui.components.ToolBar
import com.snappyrulerset.util.DpiCalibration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Initialize dependencies
    val repository = remember { DrawingRepository(context) }
    val drawingUseCase = remember { DrawingUseCase(repository) }
    val exportUseCase = remember { ExportUseCase(repository) }
    val dpiCalibration = remember { DpiCalibration(context) }

    // ViewModel factory
    val viewModel: DrawingViewModel = remember {
        DrawingViewModel(drawingUseCase, exportUseCase, dpiCalibration)
    }

    val state by viewModel.state.collectAsState()

    // Handle export status
    LaunchedEffect(state.exportStatus) {
        when (state.exportStatus) {
            is ExportStatus.Success -> {
                // You could show a toast or snackbar here
            }
            is ExportStatus.Error -> {
                // You could show an error dialog here
            }
            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            // Main drawing area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                DrawingCanvas(
                    state = state,
                    onDrawingStart = viewModel::onDrawingStart,
                    onDrawingEnd = viewModel::onDrawingEnd,
                    onZoomChanged = viewModel::onZoomChanged,
                    onOffsetChanged = viewModel::onCanvasOffsetChanged,
                    modifier = Modifier.fillMaxSize()
                )

                // Precision HUD overlay
                PrecisionHUD(
                    state = state.precisionHUD,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                // Export status overlay
                if (state.exportStatus is ExportStatus.Exporting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Exporting...")
                            }
                        }
                    }
                }
            }

            // Tool bar
            ToolBar(
                selectedTool = state.currentTool,
                snapEnabled = state.snapEnabled,
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                onToolSelected = viewModel::onToolSelected,
                onSnapToggled = viewModel::onSnapToggled,
                onUndo = viewModel::onUndo,
                onRedo = viewModel::onRedo,
                onExport = {
                    // Export with screen dimensions
                    with(density) {
                        val width = 1080 // Standard width
                        val height = 1920 // Standard height
                        viewModel.onExport(width, height)
                    }
                }
            )
        }
    }
}
