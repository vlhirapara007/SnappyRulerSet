package com.snappyrulerset.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.snappyrulerset.R
import com.snappyrulerset.domain.model.ToolType
import com.snappyrulerset.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolBar(
    selectedTool: ToolType,
    snapEnabled: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onToolSelected: (ToolType) -> Unit,
    onSnapToggled: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Tool selection row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToolButton(
                    icon = Icons.Default.Edit,
                    label = "Free",
                    isSelected = selectedTool == ToolType.FREEHAND,
                    onClick = { onToolSelected(ToolType.FREEHAND) }
                )

                ToolButton(
                    icon = Icons.Default.Straighten,
                    label = stringResource(R.string.ruler),
                    isSelected = selectedTool == ToolType.RULER,
                    onClick = { onToolSelected(ToolType.RULER) }
                )

                ToolButton(
                    icon = Icons.Default.ChangeHistory,
                    label = "45°",
                    isSelected = selectedTool == ToolType.SET_SQUARE_45,
                    onClick = { onToolSelected(ToolType.SET_SQUARE_45) }
                )

                ToolButton(
                    icon = Icons.Default.ChangeHistory,
                    label = "30°",
                    isSelected = selectedTool == ToolType.SET_SQUARE_30_60,
                    onClick = { onToolSelected(ToolType.SET_SQUARE_30_60) }
                )

                ToolButton(
                    icon = Icons.Default.PanoramaFishEye,
                    label = stringResource(R.string.protractor),
                    isSelected = selectedTool == ToolType.PROTRACTOR,
                    onClick = { onToolSelected(ToolType.PROTRACTOR) }
                )

                ToolButton(
                    icon = Icons.Default.RadioButtonUnchecked,
                    label = stringResource(R.string.compass),
                    isSelected = selectedTool == ToolType.COMPASS,
                    onClick = { onToolSelected(ToolType.COMPASS) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Snap toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSnapToggled(!snapEnabled) }
                ) {
                    Switch(
                        checked = snapEnabled,
                        onCheckedChange = onSnapToggled
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (snapEnabled) stringResource(R.string.snap_enabled) 
                               else stringResource(R.string.snap_disabled),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Undo button
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = stringResource(R.string.undo),
                        tint = if (canUndo) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                // Redo button
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = stringResource(R.string.redo),
                        tint = if (canRedo) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                // Export button
                IconButton(onClick = onExport) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(R.string.export),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ToolButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) SelectedTool.copy(alpha = 0.2f) 
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) SelectedTool else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) SelectedTool else MaterialTheme.colorScheme.onSurface
        )
    }
}
