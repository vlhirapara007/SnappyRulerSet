# Snappy Ruler Set - Android Drawing App

A professional Android drawing application with intelligent geometry tools that snap for quick, accurate construction.

## Features

- **Freehand Drawing**: Natural pen/finger drawing experience
- **Geometry Tools**:
  - Ruler with rotation and snapping
  - Set Squares (45° and 30°-60°)
  - Protractor for angle measurement
  - Compass for circles and arcs
- **Smart Snapping**: Magnetic snapping to grid, points, and common angles
- **Precision HUD**: Real-time display of measurements
- **Undo/Redo**: Up to 20 steps
- **Export**: Save drawings as PNG/JPEG

## Architecture

This app follows **MVVM (Model-View-ViewModel)** architecture with clean architecture principles:

### Key Components

- **Domain Layer**: Business logic and entities (Point, Shape, Tool, SnapCandidate)
- **Data Layer**: Repository pattern for data management
- **Presentation Layer**: ViewModels managing UI state
- **UI Layer**: Jetpack Compose for modern UI

### State Management

- Uses StateFlow for reactive state management
- Unidirectional data flow
- Clean separation of concerns

### Snapping Strategy

- Dynamic snap radius based on zoom level
- Priority-based snap candidate selection
- Spatial indexing for performance
- Visual feedback with haptic confirmation

## Technical Details

- **Platform**: Android API 26+
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Performance**: Optimized for 60fps drawing
- **Precision**: 1mm length granularity, ±0.5° angle accuracy

## Build Instructions

1. Clone/extract the project
2. Open in Android Studio
3. Sync Gradle files
4. Run on device or emulator

## Calibration

The app uses device DPI for real-world measurements. Default assumption: 160 DPI = 1dp ≈ 1px. Measurements are calibrated based on device screen density.

## Testing

Run unit tests for geometry calculations:
```
./gradlew test
```

## Performance Notes

- Canvas drawing optimized for smooth 60fps
- Snapping calculations use spatial indexing
- Memory-efficient undo/redo with state snapshots
- Lazy evaluation of snap candidates

## Trade-offs

- Simplified path conversion for export (could be enhanced)
- Basic spatial indexing (could use R-tree for complex scenes)
- Fixed snap priorities (could be user-configurable)

---

Built with ❤️ using modern Android development practices.
