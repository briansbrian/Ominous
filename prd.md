# Product Requirements Document: Ominous

## 1. Overview

**Product Name:** Ominous

**Description:** Ominous is an Android application designed to provide a seamless and non-intrusive way for users to take notes and capture screenshots. It operates as a persistent floating overlay that remains accessible on top of other applications, eliminating the need to switch apps and disrupt workflow.

**Problem Statement:** In a mobile environment, users frequently need to capture information—be it a quick thought, a piece of data from a webpage, or a visual confirmation—while actively engaged in another application. The standard process of navigating away from the current app to a dedicated notes or screenshot tool is cumbersome, inefficient, and breaks concentration.

**Target Audience:** Ominous is for any Android user who values productivity and efficient multitasking. This includes students, researchers, developers, journalists, and general users who need to capture information on the fly without leaving their current context.

## 2. Core Features

### 2.1. Floating Overlay

The core of Ominous is a floating window, managed by a background service, that provides access to the app's features from anywhere in the OS.

*   **Activation:** The overlay is launched from the main application. A persistent notification will be shown while the service is active, allowing the user to stop it from there.
*   **Behavior:**
    *   The overlay is a movable window that can be dragged and positioned anywhere on the screen.
    *   It remains on top of all other applications.
    *   When not in active use, it can be minimized into a small, unobtrusive bubble/icon. Tapping the bubble re-expands the window.
    *   The expanded window can be resized by the user.
    *   The window's position, size, and state (minimized/expanded) are saved and restored across sessions.
*   **UI:** The window contains a simple toolbar with controls for taking a screenshot, creating a new note, and closing the overlay.

### 2.2. Note-Taking

*   **Functionality:**
    *   Users can create, view, and edit text notes directly within the floating window.
    *   Content is automatically saved as the user types to prevent data loss.
    *   The window will display the content of the currently selected note.
*   **Management:** While the overlay is for quick capture, the main app interface allows for more robust management, including viewing a list of all notes, deleting notes, and searching.

### 2.3. Screenshot Capture

*   **Trigger:** A dedicated "Screenshot" button in the overlay's toolbar.
*   **Process:**
    1.  User taps the screenshot button.
    2.  The floating overlay temporarily hides itself to avoid appearing in the capture.
    3.  The device's screen is captured.
    4.  The captured image is automatically saved and associated with the currently active note. If no note is active, a new one is created.
    5.  The overlay reappears.
*   **Storage:** Screenshots are stored in the application's private storage to ensure data privacy. The file path is linked to the note record in the database.

### 2.4. Data Persistence & Management

*   **Storage:** All notes and metadata are stored locally in a Room database.
*   **Schema:** The database schema for a note (`NoteEntry`) will include:
    *   `id`: (Primary Key, String) - A unique UUID.
    *   `title`: (Text) - The title of the note.
    *   `textContent`: (Text) - The body of the note.
    *   `screenshotPaths`: (List of Strings) - A list of local file paths to associated screenshots.
    *   `lastModified`: (Timestamp) - The timestamp of the last modification.
*   **Main App UI:** The main application screen (when opened directly) will display a list of all saved notes, showing a preview of the text and a thumbnail of the screenshot if one exists. From here, users can open, delete, or share notes.

## 3. Technical Architecture

*   **Platform:** Android
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Core Components:**
    *   **`FloatingNoteService`**: A foreground `Service` responsible for managing the lifecycle of the floating overlay using `WindowManager`.
    *   **`MainActivity`**: The main entry point of the app, displaying the list of notes and settings.
    *   **Room Database (`AppDatabase`, `NoteDao`, `NoteEntry`)**: For all local data persistence.
    *   **`NoteContentProvider`**: (As defined in schema) To provide secure, controlled access to the note data for potential future integrations or other apps.
*   **Permissions:**
    *   `SYSTEM_ALERT_WINDOW`: Required to draw the overlay on top of other applications. The user must grant this permission manually via app settings.
    *   `FOREGROUND_SERVICE`: To ensure the overlay service remains active and is not killed by the OS.

## 4. Non-Functional Requirements

*   **Performance:** The overlay must be lightweight, with minimal CPU and memory usage to avoid impacting the performance of the foreground application.
*   **Reliability:** The service must be stable and handle various device states and edge cases (e.g., screen rotation, low memory) gracefully.
*   **Usability:** The interface should be clean, intuitive, and non-intrusive, providing a seamless user experience.

## 5. Future Roadmap

*   **Rich Text Editing:** Support for bold, italics, lists, and other formatting within notes.
*   **Note Organization:** Ability to organize notes into folders or apply tags.
*   **Cloud Sync:** Option to back up and sync notes across devices using services like Google Drive.
*   **Customization:** Allow users to change the theme, color, and transparency of the floating overlay.
*   **OCR Integration:** Extract text from screenshots and add it to the note.
*   **Sharing:** Directly share a note and/or its associated screenshot to other apps from the overlay.
*   **Clipboard:** Copy clipboard content and images to app 
