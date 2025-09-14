# Project Schema: Ominous

This document outlines the structure of the Ominous Android application, a tool for taking notes and screenshots as an overlay while using other apps.

## Project Structure

The main application code is located in `/home/brian/AndroidStudioProjects/Ominous/app/src/main/java/com/example/Ominous`.

### Core Components

*   **`MainActivity.kt`**: The main entry point of the application. It likely handles the main UI and the initiation of the floating note service.
*   **`FloatingNoteService.kt`**: This is the core of the overlay feature. It runs as a background service to display a floating window over other applications. This service will manage the note-taking and screenshot functionalities.
*   **`Note.kt`**: A data class or entity representing a single note.

### Data Persistence (`/data`)

*   **`AppDatabase.kt`**: The Room database class that defines the database configuration and provides access to the DAOs.
*   **`NoteDao.kt`**: Data Access Object for the `Note` entity. It defines the methods for interacting with the notes table in the database (e.g., insert, update, delete, query).
*   **`NoteEntry.kt`**: This is likely the Room entity class that defines the schema for the notes table in the database.
*   **`TypeConverters.kt`**: Contains type converters for the Room database, for example, to handle custom data types that Room doesn't natively support.

### Content Provider (`/ui`)

*   **`NoteContentProvider.kt`**: A content provider to manage access to the notes data, allowing other applications to query and modify notes securely.

### UI Components (`/ui`)

*   This directory likely contains Jetpack Compose UI elements for the application, such as the appearance of the floating note and the main application screen.

### Resources (`/res`)

*   This directory contains all non-code assets, such as layouts (if any XML layouts are used), drawable resources (icons, images), and values (strings, colors, themes).

### Android Manifest

*   **`AndroidManifest.xml`**: Declares the application's components, permissions, and other essential information for the Android operating system. This file will include the declaration of the `FloatingNoteService`.

## Features

*   **Floating Note Overlay**: A persistent, movable window that appears on top of other applications.
*   **Note Taking**: The ability to create, edit, and save text notes within the floating overlay.
*   **Screenshot Capture**: A feature to capture the screen content, possibly saving it with a note.
*   **Data Persistence**: Notes are saved locally using a Room database.

This schema provides a high-level overview of the Ominous application's architecture and key components.
