# Android Auto - Real Car Troubleshooting

## Issue: Works in Virtual Android Auto but NOT in Real Car

This is a common problem. Virtual Android Auto (DHU) is less strict than actual car implementations.

---

## Critical Fixes Applied

### 1. Session Token (FIXED)
```kotlin
// QuranMediaBrowserService.kt
sessionToken = mediaSession?.sessionToken
```

### 2. Manifest Metadata (FIXED)
```xml
<service android:name=".media.auto.QuranMediaBrowserService">
    <meta-data
        android:name="android.media.session"
        android:value="true" />
</service>
```

---

## Why Real Cars Are More Strict

| Aspect | Virtual (DHU) | Real Car |
|--------|---------------|----------|
| **Session Token** | Sometimes optional | REQUIRED |
| **Metadata** | Lenient | Strict validation |
| **Package Signing** | Any signature | Must match |
| **Unknown Sources** | Easy to enable | May be disabled |
| **Car Compatibility** | All features work | Varies by manufacturer |

---

## Debugging Steps for Real Car

### Step 1: Enable USB Debugging on Phone
```
Settings → Developer Options → USB Debugging → ON
```

### Step 2: Connect Phone to Computer (Not Car Yet)
```bash
# Check device is connected
adb devices

# Should show:
# List of devices attached
# ABC123XYZ    device
```

### Step 3: Enable Logging While Connected to Car

Create this script to capture logs while in car:

**`start_logging.bat`:**
```batch
@echo off
adb logcat -c
adb logcat > android_auto_log.txt
```

Run this BEFORE connecting phone to car, then:
1. Keep computer with phone via WiFi ADB (or use long USB cable)
2. Connect phone to car
3. Try to find app in Android Auto
4. Stop logging (Ctrl+C)
5. Check `android_auto_log.txt`

### Step 4: Analyze Logs

**What to look for:**

**✅ SUCCESS Indicators:**
```
QuranMediaBrowserService: QuranMediaBrowserService created with session token
QuranMediaBrowserService: onGetRoot: com.google.android.projection.gearhead
MediaBrowserServiceCompat: onLoadChildren: root
```

**❌ FAILURE Indicators:**
```
# Service not starting:
AndroidRuntime: java.lang.RuntimeException: Unable to create service

# Session token missing:
MediaBrowserServiceCompat: Session token should be set

# Connection refused:
MediaBrowserServiceCompat: Connection refused

# Not exported:
AndroidManifest.xml: Service not exported
```

---

## Additional Requirements for Real Cars

### 1. App Must Be Installed from Play Store OR Developer Mode Enabled

**Option A: Play Store (Recommended for production)**
- Upload APK to Play Store (can be internal testing)
- Install from Play Store on phone
- App will be trusted by Android Auto

**Option B: Developer Mode (Testing only)**
```
Phone Android Auto App:
1. Tap version number 10 times
2. Developer settings appears
3. Enable "Unknown sources"
4. Restart Android Auto app
5. Reconnect to car
```

### 2. Database Must Be Populated

Real cars won't show empty apps. Ensure:
```bash
adb logcat | findstr "QuranDataPopulatorWorker"
```

Look for:
```
QuranDataPopulatorWorker: Quran data population complete: 6236 ayahs
ReciterDataPopulatorWorker: Loaded X reciters
```

If not populated:
```bash
# Clear app data to trigger workers
adb shell pm clear com.quranmedia.player

# Launch app
adb shell am start -n com.quranmedia.player/.presentation.MainActivity

# Wait 30 seconds for workers to complete
# Check logs again
```

### 3. Correct Package Name

The app package must match AndroidManifest:
```xml
<manifest package="com.quranmedia.player">
```

And build.gradle:
```kotlin
applicationId = "com.quranmedia.player"
```

### 4. Proper Signing

Real cars check app signatures. Ensure:
- Debug builds use debug keystore
- Release builds use release keystore
- Same keystore for updates

---

## Car-Specific Issues

### Toyota/Lexus
- Very strict about "Unknown sources"
- May need Play Store installation
- Some models don't show all media apps

### Honda/Acura
- Generally good compatibility
- Check car firmware is up to date

### Ford
- Some models require Android Auto app update
- Check if other media apps (Spotify) work

### Volkswagen/Audi
- Usually very compatible
- Check MIB system version

### General Motors (Chevrolet/GMC/Buick)
- May have delayed app discovery
- Try disconnecting and reconnecting

---

## Checklist for Real Car Testing

- [ ] Compile error fixed (no "Unresolved reference: Ok")
- [ ] Build succeeds: `gradlew.bat assembleDebug`
- [ ] Install on phone: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- [ ] Clear app data: `adb shell pm clear com.quranmedia.player`
- [ ] Launch app on phone to populate database
- [ ] Wait for workers to complete (check logs)
- [ ] Enable "Unknown sources" in Android Auto app
- [ ] Restart Android Auto app
- [ ] Connect phone to car via USB (use GOOD cable)
- [ ] Wait 10-15 seconds for car to recognize phone
- [ ] Open media apps in car display
- [ ] Look for "Quran Media Player"

---

## If Still Not Showing

### Try These Steps in Order:

**1. Verify Other Apps Work**
- Install Spotify or YouTube Music
- If they don't show either → car/phone compatibility issue
- If they show → issue with Quran Media Player specifically

**2. Check Android Auto App Version**
```
Play Store → Android Auto → Update
```
Minimum version: 8.0+

**3. Try Different USB Cable**
- Use original manufacturer cable
- Try different USB port in car
- Some cars have specific ports for Android Auto

**4. Restart Everything**
```
1. Force stop Android Auto app
2. Clear Android Auto app cache
3. Unplug phone from car
4. Turn off car completely
5. Wait 30 seconds
6. Turn on car
7. Connect phone
```

**5. Check Car Settings**
```
Car Menu → Settings → Android Auto
- Ensure Android Auto is enabled
- Check if app filtering is enabled
- Look for "Allow unknown apps" setting
```

**6. Factory Reset Android Auto**
```
Phone Settings → Apps → Android Auto → Storage → Clear Data
```
This resets all Android Auto settings. You'll need to set up again.

---

## Logs to Capture

When reporting issues, capture these:

**1. Service Creation:**
```bash
adb logcat | findstr "QuranMediaBrowserService"
```

**2. Android Auto Connection:**
```bash
adb logcat | findstr "projection.gearhead"
```

**3. MediaSession:**
```bash
adb logcat | findstr "MediaSession"
```

**4. Database Population:**
```bash
adb logcat | findstr "Worker"
```

**Full Log Capture:**
```bash
adb logcat > full_log.txt
# Let it run for 1 minute while testing
# Ctrl+C to stop
# Send full_log.txt for analysis
```

---

## Expected Timeline

When phone connects to car:

| Time | What Should Happen |
|------|-------------------|
| 0s | Phone connected via USB |
| 2s | Android Auto launches on car display |
| 5s | MediaBrowserService.onCreate() called |
| 7s | onGetRoot() called by Android Auto |
| 10s | App appears in media apps list |
| 15s | Fully loaded and browsable |

If app doesn't appear after 30 seconds, it won't appear at all (troubleshoot).

---

## Success Criteria

You know it's working when:
1. ✅ App builds without errors
2. ✅ App appears in Android Auto's media section
3. ✅ Can browse "Browse by Reciter" and "Browse by Surah"
4. ✅ Selecting a surah shows metadata
5. ✅ Audio plays through car speakers
6. ✅ Car steering wheel controls work (play/pause/skip)

---

## Current Status After Fix

- ✅ Typo fixed: "Ok sessionToken" → "sessionToken"
- ✅ Session metadata added to manifest
- ✅ MediaSessionCompat created in service
- ✅ Session token set properly
- 📱 **Ready for real car testing**

**Next**: Build, install, and test in actual car using checklist above.
