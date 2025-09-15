# Android 14+ Foreground Service Fix

## Problem
The app was crashing with a `SecurityException` when trying to start the `FloatingWidgetService` as a foreground service. This is due to Android 14+ (API 34+) requirements for foreground service types.

## Root Cause
- **Android 14+ Requirement**: Apps targeting API 34+ must declare specific foreground service types
- **Missing Declaration**: The service was incorrectly declared with `mediaProjection` type
- **Permission Mismatch**: FloatingWidgetService doesn't actually perform media projection

## Error Details
```
java.lang.SecurityException: Starting FGS with type mediaProjection 
callerApp=ProcessRecord{...} targetSDK=35 requires permissions: all of the permissions 
allOf=true [android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION]
```

## Solution Applied

### 1. Updated Service Type
**Before:**
```xml
<service
    android:name=".data.services.FloatingWidgetService"
    android:foregroundServiceType="mediaProjection" />
```

**After:**
```xml
<service
    android:name=".data.services.FloatingWidgetService"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="floating_overlay" />
</service>
```

### 2. Added Required Permissions
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
```

### 3. Service Architecture
- **FloatingWidgetService**: Uses `specialUse` type for floating overlay functionality
- **ScreenshotCaptureService**: Uses `mediaProjection` type for screen capture functionality

## Why This Fix Works

1. **Correct Service Type**: `specialUse` is appropriate for floating overlays that provide unique functionality
2. **Proper Declaration**: The `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` clearly identifies the service purpose
3. **Separation of Concerns**: Media projection is handled by a separate service with proper permissions
4. **Compliance**: Meets Android 14+ security requirements

## Testing
After this fix:
- ✅ Service starts without SecurityException
- ✅ Floating overlay appears when permissions granted
- ✅ App complies with Android 14+ foreground service policies
- ✅ Screenshot functionality can be implemented in separate service

## Related Android Documentation
- [Foreground Service Types](https://developer.android.com/about/versions/14/changes/fgs-types-required)
- [Special Use Cases](https://developer.android.com/develop/background-work/services/fg-service-types#special-use)
- [Media Projection Services](https://developer.android.com/develop/background-work/services/fg-service-types#media-projection)

This fix ensures the app works correctly on Android 14+ devices while maintaining the intended floating overlay functionality.