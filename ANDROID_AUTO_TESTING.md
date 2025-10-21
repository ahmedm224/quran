# Android Auto Testing Guide

## Critical Fix Applied

The app wasn't showing in Android Auto because `QuranMediaBrowserService` was missing a **session token**. This has now been fixed.

### What Was Fixed:

```kotlin
// In QuranMediaBrowserService.onCreate():
mediaSession = MediaSessionCompat(this, "QuranMediaBrowserService").apply {
    setFlags(
        MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
    )
    isActive = true
}

// CRITICAL: This line makes the app visible to Android Auto
sessionToken = mediaSession?.sessionCompatToken
```

Without `sessionToken`, Android Auto cannot discover the service, even if all other configurations are correct.

---

## How to Test in Real Car

### Prerequisites:
1. Android phone with the app installed
2. USB cable
3. Car with Android Auto support
4. Android Auto app installed on phone

### Steps:

1. **Build and Install the App:**
   ```bash
   gradlew.bat assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Enable Developer Settings in Android Auto:**
   - Open Android Auto app on phone
   - Tap version number 10 times
   - Go to Settings → Developer settings
   - Enable "Unknown sources"

3. **Connect Phone to Car:**
   - Connect phone via USB cable
   - Android Auto should launch automatically
   - If not, start your car's Android Auto system

4. **Find the App:**
   - Tap the media icon/button in car display
   - Look for "Quran Media Player" in the app list
   - It should appear alongside Spotify, YouTube Music, etc.

5. **Test Browsing:**
   - Tap on Quran Media Player
   - You should see:
     - Browse by Reciter
     - Browse by Surah
     - Bookmarks
   - Navigate through the menus

6. **Test Playback:**
   - Select a reciter (if database is populated)
   - Select a surah
   - Audio should start playing
   - Use car controls (play/pause/skip)

---

## Troubleshooting

### Issue: App Doesn't Appear in Android Auto

**Check 1: Session Token**
```bash
adb logcat | findstr "QuranMediaBrowserService"
```
Look for: `"QuranMediaBrowserService created with session token for Android Auto"`

If missing, the session token wasn't set.

**Check 2: Manifest Configuration**
Verify `AndroidManifest.xml` has:
- `automotive_app_desc.xml` metadata
- MediaBrowserService intent filter
- Correct service export settings

**Check 3: Developer Mode**
- Unknown sources must be enabled in Android Auto app
- Developer settings must be accessible

**Check 4: USB Connection**
- Try different USB ports
- Use original manufacturer cable
- Check if Android Auto launches at all

### Issue: App Appears but Shows Empty Lists

This means:
- ✅ Android Auto connection works
- ❌ Database not populated with reciters/surahs

**Fix:**
Check if `QuranDataPopulatorWorker` and `ReciterDataPopulatorWorker` ran successfully:

```bash
adb logcat | findstr "QuranDataPopulatorWorker\|ReciterDataPopulatorWorker"
```

Look for:
- "Quran data population complete: X ayahs in database"
- "Loading X reciters for Android Auto"

If workers didn't run or failed, ensure:
- Internet connection available
- Al-Quran Cloud API accessible
- Database permissions granted

### Issue: App Crashes on Selection

```bash
adb logcat | findstr "FATAL\|QuranMedia"
```

Common causes:
- Null reciter ID or surah number
- Missing audio URL
- Database query error
- Repository injection failure

### Issue: Audio Doesn't Play

Check:
1. **Audio URL validity:**
   ```kotlin
   // Default format: https://cdn.islamic.network/quran/audio-surah/128/{reciter}/{surah}.mp3
   ```

2. **Network permission:**
   Verify `INTERNET` permission in manifest

3. **ExoPlayer logs:**
   ```bash
   adb logcat | findstr "ExoPlayer\|QuranPlayer"
   ```

4. **MediaSession connection:**
   The MediaBrowserService session is separate from the main playback service session

---

## Expected Logs (Success)

When Android Auto connects successfully:

```
QuranMediaBrowserService: QuranMediaBrowserService created with session token for Android Auto
QuranMediaBrowserService: onGetRoot: com.google.android.projection.gearhead
QuranMediaBrowserService: onLoadChildren: root
QuranMediaBrowserService: onLoadChildren: reciters
QuranDataPopulatorWorker: Quran data population complete: 6236 ayahs in database
ReciterDataPopulatorWorker: Loaded X reciters from API
```

---

## Debugging with Desktop Head Unit (DHU)

If you don't have access to a car, use DHU:

### Setup:
1. Download DHU from: https://developer.android.com/training/cars/testing
2. Extract to a folder
3. Enable USB debugging on phone
4. Install Android Auto app on phone
5. Enable Developer Mode in Android Auto

### Run DHU:
```bash
# Forward port
adb forward tcp:5277 tcp:5277

# Run DHU (Windows)
desktop-head-unit.exe --usb

# Monitor logs
adb logcat | findstr "MediaBrowser\|QuranMedia"
```

### DHU Controls:
- Click with mouse to navigate
- Use keyboard for shortcuts
- Day/Night mode toggle for theme testing
- Screen size options

---

## Android Auto Requirements Checklist

✅ **Manifest:**
- [ ] `automotive_app_desc.xml` exists in `res/xml/`
- [ ] Metadata: `com.google.android.gms.car.application`
- [ ] Service exported: `android:exported="true"`
- [ ] Intent filter: `android.media.browse.MediaBrowserService`

✅ **Service Implementation:**
- [ ] Extends `MediaBrowserServiceCompat`
- [ ] Creates `MediaSessionCompat` in `onCreate()`
- [ ] Sets `sessionToken` property
- [ ] Implements `onGetRoot()` returning non-null `BrowserRoot`
- [ ] Implements `onLoadChildren()` with actual data

✅ **Browse Hierarchy:**
- [ ] Root returns browsable items
- [ ] Reciters/Surahs return playable items with `FLAG_PLAYABLE`
- [ ] All items have title and subtitle
- [ ] Playable items include `mediaUri` in description

✅ **Data Population:**
- [ ] Database contains surahs (114 entries)
- [ ] Database contains reciters (at least 1)
- [ ] Audio URLs are valid and accessible
- [ ] Workers run successfully on first launch

---

## Post-Fix Testing Checklist

After applying the session token fix:

1. [ ] Rebuild app: `gradlew.bat assembleDebug`
2. [ ] Install on device: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. [ ] Clear app data: Settings → Apps → Quran Media Player → Clear data
4. [ ] Launch app on phone to trigger workers
5. [ ] Wait for database population (check logs)
6. [ ] Connect to Android Auto (car or DHU)
7. [ ] Verify app appears in media app list
8. [ ] Navigate browse hierarchy
9. [ ] Select and play a surah
10. [ ] Test playback controls

---

## Key Differences: Before vs After Fix

| Aspect | Before Fix | After Fix |
|--------|-----------|-----------|
| Session Token | ❌ Not set | ✅ Set in onCreate() |
| Android Auto Visibility | ❌ Hidden | ✅ Visible |
| Browse Capability | ❌ Can't browse | ✅ Can browse |
| MediaSession | ❌ Null | ✅ Active |
| Transport Controls | ❌ Not registered | ✅ Registered |

---

## Next Steps if Still Not Working

1. **Check car compatibility:** Not all cars support all Android Auto apps
2. **Try DHU first:** Eliminates car-specific variables
3. **Test with known working app:** Install Spotify/YouTube Music to verify Android Auto works
4. **Update Android Auto app:** Ensure latest version on phone
5. **Check car firmware:** Some cars need updates for Android Auto
6. **Factory reset Android Auto:** Settings → Apps → Android Auto → Clear data

---

## Success Indicators

You know it's working when:
- ✅ App appears in Android Auto media apps list
- ✅ Can browse reciters and surahs
- ✅ Selecting a surah shows loading indicator
- ✅ Audio plays through car speakers
- ✅ Car controls (play/pause) work
- ✅ Now Playing shows correct metadata

---

**Status**: Session token fix applied ✅
**Next**: Build, install, and test in car/DHU
