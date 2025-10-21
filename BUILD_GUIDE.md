# Quran Media Player - Build & Run Guide

## Project Overview

A complete Android media player for Quran recitation with precise ayah-level seeking, Android Auto support, and offline playback capabilities.

## System Requirements

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 or higher
- **Min SDK**: 27 (Android 8.1)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.22
- **Gradle**: 8.2

## Quick Start

### 1. Clone and Open Project

```bash
cd D:\Dev\Apps\quran_media
# Open in Android Studio
```

### 2. Sync Gradle

Android Studio should automatically prompt you to sync Gradle. If not:
- Click: **File → Sync Project with Gradle Files**

### 3. Build the Project

```bash
# Debug build
./gradlew assembleDebug

# Release build (with ProGuard)
./gradlew assembleRelease
```

### 4. Run on Device/Emulator

- Click the **Run** button in Android Studio, or
- Use command line:

```bash
./gradlew installDebug
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/quranmedia/player/
│   │   ├── data/
│   │   │   ├── database/       # Room database (entities, DAOs)
│   │   │   ├── datastore/      # Proto DataStore for settings
│   │   │   └── repository/     # Data layer implementations
│   │   ├── di/                 # Hilt dependency injection modules
│   │   ├── domain/
│   │   │   ├── model/          # Domain models
│   │   │   ├── repository/     # Repository interfaces
│   │   │   └── util/           # Utilities (Resource wrapper)
│   │   ├── download/           # WorkManager download system
│   │   ├── media/
│   │   │   ├── auto/           # Android Auto MediaBrowserService
│   │   │   ├── controller/     # PlaybackController
│   │   │   ├── model/          # Playback state models
│   │   │   ├── player/         # QuranPlayer (ExoPlayer wrapper)
│   │   │   └── service/        # MediaSessionService
│   │   └── presentation/
│   │       ├── components/     # Accessible UI components
│   │       ├── navigation/     # Navigation graph
│   │       ├── screens/        # UI screens (Home, Player, etc.)
│   │       ├── search/         # Search functionality
│   │       └── theme/          # Material3 theming
│   ├── proto/                   # Protocol buffer definitions
│   └── res/                     # Android resources
│       ├── values/              # Strings, themes (English)
│       └── values-ar/           # Arabic translations
├── build.gradle.kts
└── proguard-rules.pro
```

## Key Features Implemented

### ✅ 1. Foundation & Architecture
- Modern Android architecture (MVVM + Clean Architecture)
- Hilt dependency injection
- Room database for local storage
- Proto DataStore for settings
- Kotlin Coroutines & Flows for async operations

### ✅ 2. Media Playback
- **ExoPlayer** integration with exact seeking (`SeekParameters.EXACT`)
- MediaSession for system integration
- Precise ayah-level navigation
- Playback speed control (0.5x - 1.5x)
- Nudge controls (±250ms, ±1s)
- A-B loop functionality
- Gapless playback support

### ✅ 3. User Interface
- Jetpack Compose with Material Design 3
- RTL-first design with Arabic support
- Dark/Light themes with dynamic colors
- Home, Reciters, Surahs, and Player screens
- Accessible components with haptic feedback
- Navigation with Compose Navigation

### ✅ 4. Android Auto Integration
- MediaBrowserServiceCompat for car display
- Browse by Reciter → Surah hierarchy
- Voice search support
- Safe, minimal controls for automotive use

### ✅ 5. Offline & Downloads
- WorkManager-based download system
- WiFi-only download option
- Progress tracking
- File integrity verification
- Scoped storage compliant

### ✅ 6. Accessibility
- TalkBack support
- Large text mode
- High contrast themes
- Semantic descriptions for all UI elements
- Haptic feedback
- RTL layout support

### ✅ 7. Search
- Full-text search for surahs and reciters
- Arabic, English, and transliteration support
- Real-time search results

## Database Schema

### Tables
1. **reciters** - Quran reciter information
2. **surahs** - All 114 surahs with metadata
3. **audio_variants** - Audio files (different bitrates/formats)
4. **ayah_index** - Millisecond-precise ayah timestamps
5. **bookmarks** - User's saved positions with loop ranges
6. **download_tasks** - Download queue and progress

## Configuration

### Settings (Proto DataStore)
Located at: `app/src/main/proto/settings.proto`

Configurable settings include:
- Playback speed and pitch lock
- Seek increments (small: 250ms, large: 1s)
- Audio normalization
- Loop settings
- UI preferences (waveform, translations)
- Download preferences
- Accessibility options

### Build Variants

**Debug**:
- Logging enabled
- No obfuscation
- Fast build times

**Release**:
- ProGuard minification
- Resource shrinking
- Optimized for distribution

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

## Troubleshooting

### Common Issues

**1. Gradle Sync Fails**
- Ensure JDK 17 is configured
- Check internet connection for dependency downloads
- Try: **File → Invalidate Caches → Invalidate and Restart**

**2. ExoPlayer Errors**
- Verify media URLs are accessible
- Check network permissions in AndroidManifest
- Ensure ayah timing data is available

**3. Room Database Errors**
- Clear app data: **Settings → Apps → Quran Media Player → Clear Data**
- Rebuild: **Build → Clean Project → Rebuild Project**

**4. Hilt Injection Errors**
- Ensure `@HiltAndroidApp` is on Application class
- Verify `@AndroidEntryPoint` on Activities/Fragments
- Check module bindings in `di/` package

## Next Steps

### Required Before First Run

1. **Populate Database**
   - Add surah metadata to database
   - Add reciter information
   - Provide ayah timing indices

2. **Configure Audio Sources**
   - Set up streaming URLs
   - Configure CDN endpoints
   - Add fallback sources

3. **Add Icons & Assets**
   - App icon (ic_launcher)
   - Notification icons
   - Reciter images

4. **API Integration**
   - Update `NetworkModule.kt` with actual API base URL
   - Implement audio URL fetching
   - Add timing index download logic

### Recommended Enhancements

- Add waveform visualization
- Implement cloud backup for bookmarks
- Add memorization mode with spaced repetition
- Support for multiple translations
- Analytics integration
- Crash reporting (Firebase Crashlytics)

## Build for Production

```bash
# Generate signed APK
./gradlew assembleRelease

# Generate AAB (recommended for Play Store)
./gradlew bundleRelease
```

Output locations:
- APK: `app/build/outputs/apk/release/`
- AAB: `app/build/outputs/bundle/release/`

## Contributing

Follow Android & Kotlin coding standards:
- Use Kotlin coding conventions
- Write meaningful commit messages
- Add tests for new features
- Update documentation

## License

Copyright © 2025. All rights reserved.

## Support

For issues or questions:
- Check logs: `adb logcat | grep QuranMedia`
- Review Timber logs in debug builds
- Submit issues with full stack traces

---

Built with ❤️ for the Muslim community
