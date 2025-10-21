# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Quran Media Player - An Android app for Quran recitation with precise ayah-level seeking, Android Auto support, and offline playback. Built with modern Android architecture (MVVM + Clean Architecture) using Kotlin, Jetpack Compose, and ExoPlayer.

## Build Commands

### Standard Build
```bash
# Windows (recommended)
cmd.exe /c "gradlew.bat assembleDebug"

# Debug build (faster, with logging)
gradlew.bat assembleDebug

# Release build (ProGuard enabled)
gradlew.bat assembleRelease
```

### Testing
```bash
# Unit tests
gradlew.bat test

# Instrumentation tests (requires emulator/device)
gradlew.bat connectedAndroidTest
```

### Installation
```bash
# Install debug APK to connected device
gradlew.bat installDebug
```

### Cleaning
```bash
# Clean build artifacts
gradlew.bat clean

# Full rebuild
gradlew.bat clean assembleDebug
```

## Architecture

### Clean Architecture Layers

**Domain Layer** (`domain/`)
- Pure Kotlin models with no Android dependencies
- Repository interfaces define contracts
- `Resource<T>` wrapper for operation results (Success/Error/Loading)

**Data Layer** (`data/`)
- `database/`: Room entities, DAOs, and QuranDatabase
- `repository/`: Repository implementations with domain model mappers
- `api/`: Retrofit API interfaces (AlQuranCloudApi)
- `datastore/`: Proto DataStore for type-safe settings
- `worker/`: WorkManager background tasks

**Presentation Layer** (`presentation/`)
- Jetpack Compose UI with Material Design 3
- ViewModels use StateFlow for reactive state management
- Navigation via Compose Navigation with type-safe Screen routes

### Dependency Injection (Hilt)

All modules in `di/` package:
- `AppModule`: Core dependencies (Context, Dispatchers, Timber)
- `DatabaseModule`: Room database and all DAOs
- `DataStoreModule`: Proto DataStore with custom serializer
- `NetworkModule`: Retrofit, OkHttp, API interfaces
- `RepositoryModule`: Repository interface bindings
- `MediaModule`: MediaController for Media3 integration
- `WorkManagerModule`: HiltWorkerFactory configuration

## Key Components

### Media Playback System

**QuranPlayer** (`media/player/QuranPlayer.kt`)
- Singleton wrapper around ExoPlayer with exact seeking enabled
- Critical: Uses `SeekParameters.EXACT` for ayah-level precision
- Supports HTTP streaming via DefaultHttpDataSource
- Features: nudge controls (±250ms, ±1s), A-B loop, playback speed, gapless playback

**QuranMediaService** (`media/service/QuranMediaService.kt`)
- MediaSessionService for system integration and notifications
- Handles MediaSession callbacks and playback commands
- Creates MediaStyle notifications on playback channel

**PlaybackController** (`media/controller/PlaybackController.kt`)
- High-level playback orchestration
- Coordinates between QuranPlayer and UI state
- Manages ayah navigation, loop state, and position tracking

### Android Auto Integration

**QuranMediaBrowserService** (`media/auto/QuranMediaBrowserService.kt`)
- Provides browse hierarchy: Root → Reciters/Surahs/Bookmarks → Media items
- All media items must have `isPlayable = true` for Auto compatibility
- Uses `MediaDescriptionCompat` for metadata

**VoiceSearchHandler** (`media/auto/VoiceSearchHandler.kt`)
- Parses voice queries like "Play Surah Al-Baqarah by Al-Afasy"
- Searches by surah number, Arabic name, English name, or transliteration

### Database Schema

**QuranDatabase** (Room, version 2)

Tables:
1. `reciters` - Reciter metadata (name, nameArabic, style, imageUrl)
2. `surahs` - All 114 surahs (nameArabic, nameEnglish, nameTransliteration, ayahCount, revelationType)
3. `ayahs` - Full Quran text (6236 ayahs with Arabic text, juz, page, manzil, ruku, hizbQuarter)
4. `audio_variants` - Audio files (reciterId, surahNumber, bitrate, format, url, localPath, duration, fileSize, hash)
5. `ayah_index` - Millisecond-precise timestamps (reciterId, surahNumber, ayahNumber, startMs, endMs) - composite PK
6. `bookmarks` - User saved positions (reciterId, surahNumber, ayahNumber, position, label, loopStart, loopEnd)
7. `download_tasks` - Download queue (variantId, status, progress, bytesDownloaded, error, timestamps)

All DAOs use Flow<T> for reactive updates.

### Data Population

**QuranDataPopulatorWorker** (`data/worker/QuranDataPopulatorWorker.kt`)
- Runs once on app startup via WorkManager
- Downloads all 114 surahs with ayah text from Al-Quran Cloud API
- Populates `surahs` and `ayahs` tables
- Skips if database already contains 6236+ ayahs

**ReciterDataPopulatorWorker** (`data/worker/ReciterDataPopulatorWorker.kt`)
- Fetches available audio editions (reciters) from API
- Uses REPLACE policy to refresh reciter list on app startup

### Settings Management

**Proto DataStore** (`proto/settings.proto`)
- Type-safe settings storage with protobuf
- Categories: playback (speed, pitch), seeking (increments), audio (normalization), loop, UI, downloads, accessibility
- `SettingsRepository` provides suspend functions for updates
- All settings have sensible defaults in `SettingsSerializer`

## Critical Implementation Details

### ExoPlayer Seeking Precision

The app's core feature is exact ayah seeking:
```kotlin
// In QuranPlayer.kt
setSeekParameters(SeekParameters.EXACT)  // NEVER change to approximate seeking
```

Ayah navigation requires:
1. Populated `ayah_index` table with millisecond timestamps
2. ExoPlayer configured with exact seek parameters
3. Audio files that support precise seeking (preferably constant bitrate)

### Audio URL Pattern

Audio streaming expects this URL structure:
```
https://cdn.islamic.network/quran/audio-surah/{bitrate}/{reciter_identifier}/{surah_number}.mp3
```

Example: `https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/1.mp3`

Update `AudioVariantEntity.url` field with actual streaming URLs.

### Android Auto Requirements

For Android Auto compatibility:
- All media items MUST have `isPlayable = true`
- Metadata should include title, subtitle, and mediaUri for playable items
- Browse hierarchy max depth: 4 levels recommended
- The app requires `automotive_app_desc.xml` metadata file in `res/xml/`
- Service must extend `MediaBrowserServiceCompat` and implement Media3's `MediaSession`
- Session token MUST be set: `sessionToken = mediaSession?.sessionCompatToken`
- Test with Android Auto DHU (Desktop Head Unit) or real car connection

**Critical Android Auto Setup:**
1. `automotive_app_desc.xml` must declare `<uses name="media" />`
2. Manifest must include `com.google.android.gms.car.application` metadata
3. Service must handle both MediaBrowserService browsing AND MediaSession playback
4. All playable items must have valid audio URIs in MediaDescription

### RTL Support

The app is RTL-first:
- All Compose layouts use BiasAlignment for RTL mirror
- Arabic strings in `res/values-ar/strings.xml`
- Text direction automatically handled by `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`

## Common Development Tasks

### Adding a New Reciter

1. Add entry to `reciters` table via DAO or API worker
2. Create `AudioVariant` entries for each surah (1-114)
3. Optionally populate `ayah_index` for precise seeking support

### Adding a New Screen

1. Create Screen object in `presentation/navigation/Screen.kt`
2. Add ViewModel in `presentation/screens/[feature]/[Feature]ViewModel.kt`
3. Create Composable in `presentation/screens/[feature]/[Feature]Screen.kt`
4. Add route to `QuranNavGraph.kt`

### Modifying Database Schema

1. Update entity in `data/database/entity/`
2. Increment `QuranDatabase` version number
3. Provide migration in `DatabaseModule.kt`:
```kotlin
.addMigrations(object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Migration SQL
    }
})
```

### Working with Proto DataStore

Settings are strongly typed. To add a new setting:
1. Update `proto/settings.proto`
2. Rebuild to regenerate Kotlin classes
3. Update default in `SettingsSerializer.kt`
4. Add accessor/mutator in `SettingsRepository.kt`

### Testing Audio Playback

Use logcat filtering to debug media issues:
```bash
adb logcat | grep -E "QuranPlayer|ExoPlayer|MediaSession"
```

Key logs:
- "ExoPlayer created with HTTP streaming support" - player initialized
- "Seeking to ayah X at Yms" - ayah navigation
- "Player error" - playback failures (check network, URL validity)

### Testing Android Auto

**Using Android Auto Desktop Head Unit (DHU):**
```bash
# 1. Enable Developer Mode in Android Auto app on phone
# 2. Forward DHU port
adb forward tcp:5277 tcp:5277

# 3. Run DHU (download from Android developer site)
desktop-head-unit.exe

# 4. Check MediaBrowser logs
adb logcat | grep -E "MediaBrowser|QuranMediaService|onGetRoot|onLoadChildren"
```

**Key Android Auto logs:**
- "onGetRoot: clientPackage=com.google.android.projection.gearhead" - Auto connected
- "Loading X reciters for Android Auto" - browse data loaded
- "Added X media items to queue" - playback started

**Common Android Auto Issues:**
- App not visible: Check automotive_app_desc.xml and manifest metadata
- Can't browse: Verify sessionToken is set in onCreate()
- No playback: Ensure mediaUri is set in MediaDescriptionCompat for playable items
- Crash on play: Check audio URL is valid and accessible

## Known Constraints

### API Rate Limiting
- Al-Quran Cloud API has rate limits
- `QuranDataPopulatorWorker` includes 100ms delay between requests
- If you get 429 errors, increase delay in worker

### Audio Hosting
- Audio files must support HTTP range requests for seeking
- CDN URLs should use HTTPS (HTTP auto-upgraded by data source)
- Verify URLs return proper Content-Length headers

### Proto DataStore Build Order
- Protobuf files must generate before KSP runs
- Build configuration includes task dependencies (see `app/build.gradle.kts:157-168`)
- If you get "Unresolved reference: Settings", run `gradlew generateDebugProto`

## Accessibility

The app supports:
- TalkBack screen reader (all UI components have content descriptions)
- Large text (defined in `AccessibleComponents.kt`)
- High contrast themes
- Haptic feedback on button presses
- RTL layout for Arabic users

When adding UI components, always set `semantics { contentDescription = "..." }`.

## Distribution

### Debug Build
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build
- ProGuard rules in `app/proguard-rules.pro`
- Signing config required (not in repo)
- Output: `app/build/outputs/apk/release/app-release.apk`

### Play Store Bundle
```bash
gradlew.bat bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab`

## Troubleshooting

### Build Fails with "Unresolved reference: Settings"
Run: `cmd.exe /c "gradlew.bat generateDebugProto"`

### ExoPlayer "Source error" on playback
- Check audio URL is valid (test in browser)
- Verify internet permission in AndroidManifest.xml
- Check logcat for HTTP response codes

### Room "Cannot find implementation for QuranDatabase"
- Rebuild project (Clean + Build)
- Ensure all DAOs are abstract functions
- Check KSP annotation processor is running

### Hilt "Missing @Inject constructor"
- Verify @Singleton or @ViewModelScoped on class
- Check @Provides method exists in appropriate module
- Ensure @HiltAndroidApp on Application class

### Android Auto not showing content
- Verify automotive declaration in AndroidManifest.xml
- Check MediaBrowserService filters in manifest
- Use `adb logcat | grep MediaBrowser` to debug
- Test with `adb forward tcp:5277 tcp:5277` and DHU

## File Locations Reference

- Application class: `app/src/main/java/com/quranmedia/player/QuranMediaApplication.kt`
- Database: `app/src/main/java/com/quranmedia/player/data/database/QuranDatabase.kt`
- Main Activity: `app/src/main/java/com/quranmedia/player/presentation/MainActivity.kt`
- Navigation: `app/src/main/java/com/quranmedia/player/presentation/navigation/QuranNavGraph.kt`
- Proto settings: `app/src/main/proto/settings.proto`
- API interface: `app/src/main/java/com/quranmedia/player/data/api/AlQuranCloudApi.kt`
