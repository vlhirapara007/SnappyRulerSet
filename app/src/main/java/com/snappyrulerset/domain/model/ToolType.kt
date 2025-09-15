package com.snappyrulerset.domain.model

enum class ToolType {
    FREEHAND,
    RULER,
    SET_SQUARE_45,
    SET_SQUARE_30_60,
    PROTRACTOR,
    COMPASS
}

data class Tool(
    val type: ToolType,
    val position: Point = Point(0f, 0f),
    val rotation: Float = 0f,
    val isActive: Boolean = false
)
