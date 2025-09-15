# Ominous Floating Overlay Testing Guide

## Overview
This guide will help you test the floating overlay functionality with proper Android security permission handling.

## Expected Behavior (Per PRD + Security)
- **Activation:** Overlay launched from main app via + button with permission checks
- **Permission Handling:** Automatic overlay permission verification and request flow
- **Draggable:** Window can be moved anywhere on screen
- **Minimizable:** Can minimize to small bubble, tap to expand
- **Persistent:** Stays on top of all apps (when permission granted)
- **State Saving:** Position, size, and state saved across sessions
- **Toolbar:** Contains screenshot, note, and close controls

## Test Steps

### 1. Initial Setup
1. Install and launch the Ominous app
2. Grant all required permissions:
   - ✅ Notification permission
   - ✅ Storage permission
   - ⚠️ Overlay permission (will be tested separately)
3. Verify you see the main screen with notes list
4. Check that the floating service starts (notification should appear)

### 2. First-Time Overlay Permission Flow
1. **Action:** Tap the floating action button (+ button) in bottom-right
2. **Expected Result (if overlay permission NOT granted):**
   - Permission explanation dialog appears
   - Dialog explains why overlay permission is needed
   - Options: "Grant Permission" or "Cancel"

3. **Action:** Tap "Grant Permission" in dialog
4. **Expected Result:**
   - Android settings opens to "Display over other apps" 
   - Toast message guides user: "Please enable 'Display over other apps' for Ominous"
   - Ominous app is highlighted in the list

5. **Action:** Enable "Allow display over other apps" for Ominous
6. **Action:** Return to Ominous app (back button or app switcher)
7. **Action:** Tap + button again
8. **Expected Result:**
   - Floating overlay window appears immediately
   - No permission dialog shown this time

### 3. Test Overlay Display (Permission Granted)
1. **Action:** Tap + button with overlay permission granted
2. **Expected Result:** 
   - Floating overlay window appears on screen
   - Window shows "Ominous" title with minimize button
   - Three action buttons visible: "📷 Screenshot", "📝 New Note", "✕ Close"
   - Window has maroon/semi-transparent background

### 4. Test Permission Denial Handling
1. **Action:** Revoke overlay permission in Android settings
2. **Action:** Tap + button in app
3. **Expected Result:**
   - Permission dialog appears again
   - If "Cancel" pressed, nothing happens
   - If "Grant Permission" pressed, settings opens again

### 5. Test Notification-Based Widget Launch
1. **Action:** Pull down notification panel
2. **Action:** Find "Ominous Floating Widget" notification
3. **Action:** Tap "Show Widget" button in notification
4. **Expected Result:**
   - If permission granted: Widget appears
   - If permission denied: Settings opens with toast guidance

### 6. Test Dragging Functionality
1. **Action:** Touch and drag the overlay window
2. **Expected Result:**
   - Window follows finger movement
   - Can be positioned anywhere on screen
   - Position updates smoothly during drag
   - Position is saved when drag ends

### 7. Test Minimization/Expansion
1. **Action:** Tap the "−" (minimize) button in the overlay
2. **Expected Result:**
   - Window collapses to small circular bubble/icon
   - Bubble shows note-like icon with maroon color
   - Bubble remains draggable
   - State is saved as minimized

3. **Action:** Tap the minimized bubble
4. **Expected Result:**
   - Bubble expands back to full overlay window
   - All toolbar buttons are visible again
   - Window appears at saved position

### 8. Test Overlay Persistence Over Apps
1. **Action:** With overlay visible, press Home button and open another app
2. **Expected Result:**
   - Overlay remains visible over the other app
   - All functionality remains available
   - Can still drag, minimize, and interact with overlay

### 9. Test Action Buttons

#### Screenshot Button (📷)
1. **Action:** Tap "📷 Screenshot" button
2. **Expected Result:**
   - Log message: "Screenshot clicked"
   - (Actual screenshot functionality to be implemented later)

#### New Note Button (📝)
1. **Action:** Tap "📝 New Note" button
2. **Expected Result:**
   - Main Ominous app opens/comes to foreground
   - Log message: "Note clicked - opening main app"

#### Close Button (✕)
1. **Action:** Tap "✕ Close" button
2. **Expected Result:**
   - Overlay disappears completely
   - Widget state is saved

### 10. Test Error Scenarios

#### Permission Revoked While Widget Visible
1. **Action:** With widget visible, revoke overlay permission in settings
2. **Action:** Try to interact with widget
3. **Expected Result:** System may force-remove widget

#### System Resource Pressure
1. **Action:** Open many apps to stress system memory
2. **Expected Result:** Widget should remain stable or gracefully handle cleanup

## Success Criteria
- [ ] ✅ Permission dialog explains overlay need clearly
- [ ] ✅ Permission request opens correct Android settings
- [ ] ✅ Toast guidance helps user find permission setting
- [ ] ✅ Widget only appears when permission granted
- [ ] ✅ Overlay launches from + button when permitted
- [ ] ✅ Window is draggable to any screen position
- [ ] ✅ Can minimize to bubble and expand back
- [ ] ✅ Stays on top of all other applications
- [ ] ✅ Position and state persist across app restarts
- [ ] ✅ All toolbar buttons are functional
- [ ] ✅ Notification provides alternative launch method
- [ ] ✅ Proper error handling for permission denial
- [ ] ✅ No crashes or security exceptions

## Troubleshooting

### Permission Dialog Doesn't Appear
- Check that overlay permission is actually denied in system settings
- Verify dialog logic in MainScreen.kt

### Settings Don't Open Correctly
- Check if PermissionHelper.requestOverlayPermission() is working
- Verify package name in intent

### Widget Appears Without Permission
- This would be a security bug - report immediately
- Check hasOverlayPermission() logic

### Widget Disappears Randomly
- Check system logs for SecurityException
- Verify foreground service is maintaining permission
- Check if system is killing service due to memory pressure

### Toast Messages Don't Show
- Check if service context can show toasts
- Verify toast text is not being truncated

## Permission Testing Matrix

| Scenario | Permission State | Expected Behavior |
|----------|-----------------|-------------------|
| First launch | Not granted | Show permission dialog |
| After granting | Granted | Show widget immediately |
| After denying | Not granted | Stay on main screen |
| Permission revoked | Not granted | Request permission again |
| System reboot | Granted (persistent) | Widget available immediately |

## Logs to Monitor
- "FloatingWidget" tag for all widget interactions
- "Overlay permission not granted" warnings
- "Requesting overlay permission" info
- SecurityException for permission violations
- WindowManager exceptions for overlay issues