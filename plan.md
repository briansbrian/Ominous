# Ominous App Development Checklist

## Phase 1: Project Setup & Dependencies

### 1.1 Gradle Configuration
- [x] Update `gradle/libs.versions.toml` with required dependencies:
  - Room database (room-runtime, room-compiler, room-ktx)
  - Hilt dependency injection (hilt-android, hilt-compiler)
  - ViewModel and LiveData (lifecycle-viewmodel-compose)
  - Navigation Compose
  - Permissions handling (accompanist-permissions)
  - Coroutines (kotlinx-coroutines-android)
  - Material Icons Extended
  - WindowManager for floating overlay

- [x] Update `app/build.gradle.kts`:
  - Add Hilt plugin (`dagger.hilt.android.plugin`)
  - Add Room annotation processor
  - Add kapt plugin for annotation processing
  - Configure buildFeatures for viewBinding if needed
  - Add required permissions in manifest

### 1.2 Android Manifest Setup
- [x] Add required permissions:
  - `SYSTEM_ALERT_WINDOW` (overlay permission)
  - `FOREGROUND_SERVICE`
  - `FOREGROUND_SERVICE_MEDIA_PROJECTION` (for screenshots)
  - `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`
  - `POST_NOTIFICATIONS`

- [x] Declare services:
  - `FloatingWidgetService` (foreground service)
  - `ClipboardMonitorService`
  - `ScreenshotCaptureService`

- [x] Add service declarations and intent filters

## Phase 2: Data Layer Implementation

### 2.1 Database Entities
- [x] Create `data/database/entities/Note.kt`
- [x] Create `data/database/entities/Screenshot.kt`
- [x] Implement type converters for List<String> in `TypeConverters.kt`

### 2.2 Database DAOs
- [x] Create `data/database/dao/NoteDao.kt` with CRUD operations
- [x] Create `data/database/dao/ScreenshotDao.kt` with CRUD operations
- [x] Add queries for note-screenshot relationships

### 2.3 Database Setup
- [x] Create `data/database/OminousDatabase.kt` (Room database)
- [x] Configure database version and migration strategy
- [x] Set up database module in Hilt

### 2.4 Repository Layer
- [x] Create `domain/repository/INoteRepository.kt` interface
- [x] Implement `data/repository/NoteRepository.kt`
- [x] Add repository to Hilt modules

## Phase 3: Domain Layer Implementation

### 3.1 Domain Models
- [x] Create `domain/model/NoteWithScreenshots.kt`
- [x] Create `domain/model/ExportFormat.kt`
- [x] Create `domain/model/ExportOptions.kt`

### 3.2 Use Cases
- [x] Create `domain/usecase/CreateNoteUseCase.kt`
- [x] Create `domain/usecase/CaptureScreenshotUseCase.kt`
- [x] Create `domain/usecase/ExportNotesUseCase.kt`
- [x] Create `domain/usecase/ClipboardMonitorUseCase.kt`

## Phase 4: Core Services Implementation

### 4.1 Floating Widget Service
- [x] Create `data/services/FloatingWidgetService.kt`
- [x] Implement WindowManager integration
- [x] Handle overlay permissions
- [x] Implement floating window lifecycle management
- [x] Add notification for foreground service
- [ ] Handle window positioning and state persistence

### 4.2 Screenshot Capture Service
- [x] Create `data/services/ScreenshotCaptureService.kt`
- [x] Implement MediaProjection API integration
- [x] Handle UI hiding during capture
- [x] Implement image compression and storage
- [x] Generate thumbnails for screenshots

### 4.3 Clipboard Monitor Service
- [x] Create `data/services/ClipboardMonitorService.kt`
- [x] Implement background clipboard monitoring
- [x] Add content filtering for sensitive data
- [x] Integrate with active note system

## Phase 5: UI Theme & Design System

### 5.1 Theme Setup
- [x] Create `presentation/theme/Color.kt` with maroon-gold color scheme
- [x] Create `presentation/theme/Theme.kt` with light/dark themes
- [x] Create `presentation/theme/Type.kt` with typography definitions
- [x] Implement adaptive colors for floating widget

### 5.2 Common UI Components
- [ ] Create reusable Compose components
- [ ] Implement color extraction utilities
- [ ] Create screenshot thumbnail component

## Phase 6: Main App UI Implementation

### 6.1 Main Screen
- [x] Create `presentation/main/MainViewModel.kt`
- [x] Create `presentation/main/MainScreen.kt`
- [x] Implement note list with pinned notes at top
- [x] Add multi-selection functionality
- [ ] Implement search and filtering

### 6.2 Main Screen Components
- [x] Create `presentation/main/components/NoteCard.kt`
- [x] Create `presentation/main/components/PinnedNoteCard.kt`
- [x] Create `presentation/main/components/ExportDialog.kt`
- [x] Implement note preview with screenshot thumbnails

### 6.3 MainActivity
- [x] Update `MainActivity.kt` with Compose integration
- [x] Handle permission requests
- [ ] Implement service launch logic
- [ ] Add navigation setup

## Phase 7: Floating Widget UI Implementation

### 7.1 Floating Widget Core
- [x] Create `presentation/floating/FloatingWidgetViewModel.kt`
- [x] Create `presentation/floating/FloatingWidgetCompose.kt`
- [x] Implement floating window Compose integration
- [x] Handle window state management

### 7.2 Floating Widget Components
- [x] Create `presentation/floating/components/FloatingIcon.kt` (minimized state)
- [x] Create `presentation/floating/components/ExpandedWidget.kt`
- [x] Create `presentation/floating/components/ScreenshotThumbnail.kt`
- [x] Implement toolbar with screenshot and note buttons

## Phase 8: Utility Classes & Helpers

### 8.1 Utility Classes
- [x] Create `utils/PermissionHelper.kt` for permission management
- [x] Create `utils/ColorExtractor.kt` for adaptive theming
- [x] Create `utils/ExportHelper.kt` for note export functionality
- [x] Create `utils/Constants.kt` for app constants

### 8.2 Export System
- [x] Implement markdown export
- [x] Implement HTML export
- [x] Implement plain text export
- [x] Add ZIP creation for multi-file exports
- [x] Handle image embedding/referencing

## Phase 9: Dependency Injection Setup

### 9.1 Hilt Modules
- [x] Create `di/DatabaseModule.kt`
- [x] Create `di/RepositoryModule.kt`
- [x] Create `di/ServiceModule.kt`
- [x] Configure application-level Hilt setup

### 9.2 Application Class
- [x] Create `OminousApplication.kt` with Hilt annotation
- [x] Update manifest to reference custom application class

## Phase 10: Permissions & Security

### 10.1 Permission Handling
- [ ] Implement runtime permission requests
- [ ] Add overlay permission request flow
- [ ] Handle permission denial gracefully
- [ ] Add settings deep-linking for manual permissions

### 10.2 Security Features
- [ ] Implement sensitive data filtering
- [ ] Add secure file storage
- [ ] Ensure privacy compliance

## Phase 11: Testing Implementation

### 11.1 Unit Tests
- [ ] Write tests for repository layer
- [ ] Write tests for use cases
- [ ] Write tests for export functionality
- [ ] Write tests for utility classes

### 11.2 Integration Tests
- [ ] Test database operations
- [ ] Test service lifecycle
- [ ] Test permission flows

### 11.3 UI Tests
- [ ] Test main screen functionality
- [ ] Test floating widget interactions
- [ ] Test navigation flows

## Phase 12: Build Configuration & Optimization

### 12.1 ProGuard Setup
- [ ] Configure `proguard-rules.pro`
- [ ] Keep Room entities and DAOs
- [ ] Preserve Hilt-generated classes
- [ ] Maintain service entry points

### 12.2 Build Optimization
- [ ] Configure release build settings
- [ ] Optimize APK size
- [ ] Test release build functionality

## Phase 13: Final Integration & Testing

### 13.1 End-to-End Testing
- [ ] Test complete note creation flow
- [ ] Test screenshot capture integration
- [ ] Test floating widget lifecycle
- [ ] Test export functionality
- [ ] Test clipboard monitoring

### 13.2 Performance Testing
- [ ] Test memory usage
- [ ] Test battery impact
- [ ] Test overlay performance
- [ ] Optimize based on results

### 13.3 Device Testing
- [ ] Test on different Android versions (API 26+)
- [ ] Test on different screen sizes
- [ ] Test edge cases and error scenarios

## Phase 14: Final Polish

### 14.1 UI/UX Refinement
- [ ] Polish animations and transitions
- [ ] Ensure accessibility compliance
- [ ] Test user experience flows
- [ ] Implement feedback mechanisms

### 14.2 Documentation
- [ ] Update README with setup instructions
- [ ] Document API and architecture
- [ ] Create user guide
- [ ] Document known issues and limitations