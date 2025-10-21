# Android Auto Playback Fix

## Issue: "Gathering your selection..." Stuck

The app appeared in Android Auto and browsing worked, but selecting a surah got stuck with "Gathering your selection..." message.

---

## Root Cause

The `MediaBrowserCompat.MediaItem` items returned by `getReciterSurahsItems()` were missing the **`mediaUri`** property.

Without `mediaUri`, Android Auto doesn't know where the audio file is located, so it can't initiate playback.

---

## Fix Applied

### Added `mediaUri` to Media Items

**File**: `QuranMediaBrowserService.kt`

```kotlin
private suspend fun getReciterSurahsItems(reciterId: String): List<MediaBrowserCompat.MediaItem> {
    val surahs = quranRepository.getAllSurahs().first()
    val reciter = quranRepository.getReciterById(reciterId)

    return surahs.map { surah ->
        // Get audio URL from database or build fallback
        val audioVariant = quranRepository.getAudioVariant(reciterId, surah.number)
        val audioUrl = audioVariant?.url ?: buildAudioUrl(reciterId, surah.number)

        MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId("$MEDIA_RECITER_PREFIX$reciterId:$MEDIA_SURAH_PREFIX${surah.number}")
                .setTitle(surah.nameEnglish)
                .setSubtitle("${surah.nameArabic} - ${reciter?.name ?: ""}")
                .setMediaUri(Uri.parse(audioUrl))  // ← THIS IS CRITICAL
                .build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }
}

private fun buildAudioUrl(reciterId: String, surahNumber: Int): String {
    return "https://cdn.islamic.network/quran/audio-surah/128/$reciterId/$surahNumber.mp3"
}
```

---

## How Android Auto Playback Works

### Step 1: User Browses
```
Android Auto → QuranMediaBrowserService.onLoadChildren()
Returns: List of MediaItems with FLAG_PLAYABLE
```

### Step 2: User Selects Item
```
Android Auto reads mediaUri from MediaDescriptionCompat
Sends mediaUri to phone's media player
```

### Step 3: Playback Starts
```
QuranMediaService receives play command
ExoPlayer loads audio from mediaUri
Audio plays through car speakers
```

### Step 4: Controls Appear
```
Once playback actually starts:
- Now Playing screen shows
- Play/Pause/Skip controls appear
- Metadata displays (title, artist, album art)
```

---

## Why Controls Don't Show Initially

The playback controls won't appear until audio **actually starts playing**. This is normal behavior.

**Expected Flow:**
1. Select surah → "Gathering your selection..." (1-3 seconds)
2. Audio starts loading → "Loading..." or buffering indicator
3. First audio plays → Now Playing screen appears
4. Controls become active

**If stuck at "Gathering...":**
- `mediaUri` was missing (NOW FIXED ✅)
- Audio URL is invalid
- Network connection issue
- INTERNET permission missing

---

## Testing Checklist

After applying this fix:

- [ ] Rebuild app: `gradlew.bat assembleDebug`
- [ ] Install on phone
- [ ] Connect to Android Auto (car or DHU)
- [ ] Browse: Reciters → Select Reciter → Surahs list appears
- [ ] Select a surah
- [ ] Should see: "Gathering your selection..." (brief)
- [ ] Then: Audio should start playing
- [ ] Now Playing screen should appear
- [ ] Controls (play/pause/skip) should be active

---

## Debugging Playback Issues

### Check Logs for Audio URLs

```bash
adb logcat | findstr "QuranMediaBrowserService\|Built audio URL\|Surah"
```

Expected output:
```
QuranMediaBrowserService: Loading surahs for reciter: ar.abdulbasitmurattal
QuranMediaBrowserService: Surah 1: https://cdn.islamic.network/quran/audio-surah/128/ar.abdulbasitmurattal/1.mp3
QuranMediaBrowserService: Surah 2: https://cdn.islamic.network/quran/audio-surah/128/ar.abdulbasitmurattal/2.mp3
...
QuranMediaBrowserService: Built audio URL: https://cdn.islamic.network/quran/audio-surah/128/ar.abdulbasitmurattal/1.mp3
```

### Test Audio URL Directly

Copy an audio URL from logs and test in browser:
```
https://cdn.islamic.network/quran/audio-surah/128/ar.abdulbasitmurattal/1.mp3
```

Should download/play an MP3 file.

### Check ExoPlayer Logs

```bash
adb logcat | findstr "ExoPlayer\|QuranPlayer"
```

Look for:
- ✅ "ExoPlayer created with HTTP streaming support"
- ✅ "Loading audio: https://..."
- ❌ "Source error" (means audio URL failed)
- ❌ "Unable to connect" (network issue)

---

## Common Audio URL Issues

### Issue: Wrong Reciter ID Format

Some reciters have different ID formats:

```kotlin
// Database may have: "Abdul Basit Murattal"
// CDN expects: "ar.abdulbasitmurattal"
```

**Fix**: Ensure reciter IDs match CDN format.

### Issue: Surah Number Formatting

```kotlin
// Correct: "1.mp3", "2.mp3", "114.mp3"
// Wrong: "001.mp3", "002.mp3"
```

Current implementation uses `$surahNumber` which is correct.

### Issue: Different CDN or Bitrate

If default CDN doesn't work, try:
- Different bitrate: `/64/` or `/192/` instead of `/128/`
- Different CDN endpoint
- Check ReciterDataPopulatorWorker for actual URLs

---

## Next Steps if Still Not Playing

### 1. Verify Internet Permission

`AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
✅ Already present

### 2. Check Network Connectivity

In car:
- Phone may use car's data connection
- Some cars block internet for apps
- Test with phone's own data (not WiFi)

### 3. Test with Known Working URL

Temporarily hardcode a known working URL:

```kotlin
private fun buildAudioUrl(reciterId: String, surahNumber: Int): String {
    // Test with a known working URL first
    return "https://server8.mp3quran.net/afs/001.mp3"
}
```

If this plays → issue is with CDN URL format
If this doesn't play → issue is with network/ExoPlayer setup

### 4. Check AudioVariant Database

```bash
adb shell
run-as com.quranmedia.player
sqlite3 databases/quran_database
SELECT * FROM audio_variants LIMIT 5;
.quit
exit
```

Check if audio URLs are stored in database.

---

## Status After Fix

| Component | Status |
|-----------|--------|
| App visibility in Android Auto | ✅ WORKING |
| Browse hierarchy (Reciters/Surahs) | ✅ WORKING |
| Media items with mediaUri | ✅ FIXED |
| Audio URL construction | ✅ IMPLEMENTED |
| Logging for debugging | ✅ ADDED |

**Next**: Test playback in real car with this fix.

---

## Expected Behavior After Fix

### Successful Playback:

1. **Select Reciter**: "Abdul Basit Murattal"
2. **See Surahs**: List of 114 surahs loads
3. **Select Surah**: "Al-Fatihah"
4. **Brief Loading**: "Gathering your selection..." (1-3 sec)
5. **Audio Starts**: Quran recitation begins
6. **Now Playing**: Full screen with controls
7. **Can Control**: Play/pause/skip/seek
8. **Car Controls Work**: Steering wheel buttons functional

### If Playback Fails:

1. Check logs for "Built audio URL"
2. Copy URL and test in browser
3. Check ExoPlayer error logs
4. Verify network connection
5. Try hardcoded test URL

---

**The critical fix**: Adding `.setMediaUri(Uri.parse(audioUrl))` to the MediaDescriptionCompat builder. Without this, Android Auto has no idea where the audio file is!
