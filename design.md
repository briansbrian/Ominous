# Ominous - Technical Design Document

## Architecture Overview

Ominous follows Clean Architecture principles with MVVM pattern, using Jetpack Compose for UI and Hilt for dependency injection.

### Architecture Layers

```
┌─────────────────────────────────────┐
│           Presentation Layer        │
│  ┌─────────────┐ ┌─────────────────┐│
│  │ MainActivity│ │ FloatingWidget  ││
│  │   (Compose) │ │   (Compose)     ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│            Domain Layer             │
│  ┌─────────────┐ ┌─────────────────┐│
│  │ ViewModels  │ │  Use Cases      ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│             Data Layer              │
│  ┌─────────────┐ ┌─────────────────┐│
│  │ Repository  │ │ Room Database   ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
```

## Project Structure

```
app/src/main/java/com/example/ominous/
├── OminousApplication.kt
├── MainActivity.kt
├── data/
│   ├── database/
│   │   ├── OminousDatabase.kt
│   │   ├── entities/
│   │   │   ├── Note.kt
│   │   │   └── Screenshot.kt
│   │   └── dao/
│   │       ├── NoteDao.kt
│   │       └── ScreenshotDao.kt
│   ├── repository/
│   │   └── NoteRepository.kt
│   └── services/
│       ├── FloatingWidgetService.kt
│       ├── ClipboardMonitorService.kt
│       └── ScreenshotCaptureService.kt
├── domain/
│   ├── model/
│   │   ├── NoteWithScreenshots.kt
│   │   └── ExportFormat.kt
│   ├── usecase/
│   │   ├── CreateNoteUseCase.kt
│   │   ├── CaptureScreenshotUseCase.kt
│   │   ├── ExportNotesUseCase.kt
│   │   └── ClipboardMonitorUseCase.kt
│   └── repository/
│       └── INoteRepository.kt
├── presentation/
│   ├── main/
│   │   ├── MainViewModel.kt
│   │   ├── MainScreen.kt
│   │   └── components/
│   │       ├── NoteCard.kt
│   │       ├── PinnedNoteCard.kt
│   │       └── ExportDialog.kt
│   ├── floating/
│   │   ├── FloatingWidgetViewModel.kt
│   │   ├── FloatingWidgetCompose.kt
│   │   └── components/
│   │       ├── FloatingIcon.kt
│   │       ├── ExpandedWidget.kt
│   │       └── ScreenshotThumbnail.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── di/
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── ServiceModule.kt
└── utils/
    ├── PermissionHelper.kt
    ├── ColorExtractor.kt
    ├── ExportHelper.kt
    └── Constants.kt
```

## Data Models

### Database Entities

```kotlin
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val tags: List<String> = emptyList()
)

@Entity(
    tableName = "screenshots",
    foreignKeys = [ForeignKey(
        entity = Note::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Screenshot(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null
)
```

### Domain Models

```kotlin
data class NoteWithScreenshots(
    val note: Note,
    val screenshots: List<Screenshot>
)

enum class ExportFormat {
    MARKDOWN, HTML, PLAIN_TEXT
}

data class ExportOptions(
    val format: ExportFormat,
    val includeImages: Boolean,
    val dateRange: Pair<Long, Long>? = null
)
```

## Key Components

### 1. Floating Widget Service

**FloatingWidgetService.kt**
- Manages floating window lifecycle
- Handles overlay permissions
- Coordinates with FloatingWidgetCompose for UI

**Key Features:**
- WindowManager integration for floating windows
- Adaptive positioning based on screen edges
- Auto-minimize after inactivity
- Termination gesture handling

### 2. Screenshot Capture

**ScreenshotCaptureService.kt**
- MediaProjection API integration
- UI hiding during capture (150ms)
- Image compression and storage
- Thumbnail generation

### 3. Clipboard Monitor

**ClipboardMonitorService.kt**
- Background clipboard monitoring
- Content filtering (sensitive data detection)
- Auto-append to active notes
- Batch processing for rapid copies

### 4. Main App UI

**MainScreen.kt**
- Maroon-gold theme implementation
- Pinned notes (circular) at top
- Regular notes in vertical column
- Multi-selection with long-press
- Export dialog integration

### 5. Export System

**ExportHelper.kt**
- Smart ZIP vs single file logic
- Format conversion (MD, HTML, TXT)
- Image embedding/referencing
- File organization

## Color Scheme

```kotlin
object OminousColors {
    val DeepMaroon = Color(0xFF8B1538)
    val RichGold = Color(0xFFD4AF37)
    val DarkCharcoal = Color(0xFF2C2C2C)
    val WarmBrown = Color(0xFF3C2E26)
    val CreamGold = Color(0xFFF5E6D3)
    val BrightGold = Color(0xFFFFD700)
    
    // Adaptive colors for floating widget
    val FloatingBackground = Color.White
    val FloatingBorderDefault = Color(0xFFCCCCCC)
}
```

## Services Architecture

### Foreground Service Strategy

```kotlin
class FloatingWidgetService : Service() {
    // Persistent notification for floating widget
    // WindowManager for overlay management
    // Lifecycle coordination with MainActivity
}

class ClipboardMonitorService : Service() {
    // Background clipboard monitoring
    // Content filtering and processing
    // Integration with active note
}
```

### Quick Settings Tile

```kotlin
class OminousTileService : TileService() {
    // Quick Settings integration
    // Permission checking
    // Direct floating widget launch
}
```

## Performance Considerations

### Memory Management
- Lazy loading of screenshots
- Image compression (JPEG 80% quality)
- Database pagination for large note collections
- Efficient bitmap recycling

### Battery Optimization
- Minimal CPU usage when minimized
- Smart clipboard monitoring (pause during inactivity)
- Efficient database queries with proper indexing
- Background service optimization

### Storage Strategy
- Internal storage for app data
- Organized screenshot folders by date
- Efficient database schema with foreign keys
- Automatic cleanup of orphaned files

## Security & Privacy

### Permission Handling
- Runtime permission requests
- Clear permission explanations
- Graceful degradation when permissions denied
- Settings deep-linking for manual permission grants

### Data Protection
- Local-only storage (no cloud sync initially)
- Sensitive data filtering in clipboard
- Secure file storage in app-private directories
- Export with user consent only

## Testing Strategy

### Unit Tests
- Repository layer testing
- Use case testing
- Export functionality testing
- Color extraction utility testing

### Integration Tests
- Database operations
- Service lifecycle testing
- Permission flow testing

### UI Tests
- Compose UI testing
- Floating widget interaction testing
- Main app navigation testing

## Build Configuration

### Gradle Setup
- Minimum SDK 26 (Android 8.0)
- Target SDK 36 (latest)
- Hilt dependency injection
- Room database with KTX extensions
- Compose BOM for version alignment

### ProGuard Rules
- Keep Room entities and DAOs
- Preserve Hilt-generated classes
- Maintain service entry points
- Optimize release builds

This design provides a solid foundation for building Ominous with clean architecture, proper separation of concerns, and scalable component structure.