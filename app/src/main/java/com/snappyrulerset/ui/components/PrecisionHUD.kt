package com.snappyrulerset.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snappyrulerset.domain.model.Point
import com.snappyrulerset.presentation.drawing.PrecisionHUDState

@Composable
fun PrecisionHUD(
    state: PrecisionHUDState,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    Card(
        modifier = modifier
            .offset(
                x = (state.position.x / 2).dp,
                y = (state.position.y / 2).dp
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            if (state.lengthText.isNotEmpty()) {
                Text(
                    text = state.lengthText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (state.angleText.isNotEmpty()) {
                Text(
                    text = state.angleText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (state.snapInfo.isNotEmpty()) {
                Text(
                    text = "Snap: ${state.snapInfo}",
                    color = Color.Cyan,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
