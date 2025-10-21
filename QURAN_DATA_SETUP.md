# Quran Data Setup Guide

This guide explains how to populate your app with Quran data (all 114 Surahs with Ayah-level indexing) from the Al-Quran Cloud API.

## Overview

The app now includes:
- **Database Schema**: Room entities for storing Surahs and Ayahs with full text content
- **API Integration**: Retrofit service for Al-Quran Cloud API
- **Background Worker**: Automated data population using WorkManager
- **Testing Script**: Standalone Python script for testing and verification

## What Data Is Included?

For each of the 114 Surahs, we store:
- **Surah Metadata**: Name (Arabic & English), number of Ayahs, revelation type (Meccan/Medinan)
- **Ayah Data**:
  - Arabic text
  - Ayah number (within Surah and global)
  - Juz, Manzil, Page, Ruku, Hizb Quarter
  - Sajda (prostration) indicator

**Total**: 6,236 Ayahs across 114 Surahs

## Database Schema

### New Tables

#### `ayahs` Table
```kotlin
@Entity(tableName = "ayahs")
data class AyahEntity(
    val surahNumber: Int,
    val ayahNumber: Int,
    val globalAyahNumber: Int,  // 1-6236
    val textArabic: String,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    val sajda: Boolean
)
```

#### Updated `surahs` Table
The existing `SurahEntity` now gets populated with complete metadata.

## How to Use

### Option 1: Automatic Population (Recommended)

The app can automatically download and populate Quran data in the background.

#### In Your Application/Activity:

```kotlin
@Inject
lateinit var quranDataRepository: QuranDataRepository

// Start population
fun populateQuranData() {
    lifecycleScope.launch {
        // Check if already populated
        if (!quranDataRepository.isQuranDataPopulated()) {
            // Start background download
            quranDataRepository.startQuranDataPopulation()

            // Observe progress
            quranDataRepository.getPopulationWorkStatus()
                .collect { workInfo ->
                    when (workInfo?.state) {
                        WorkInfo.State.RUNNING -> {
                            // Show loading indicator
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            // Data ready!
                        }
                        WorkInfo.State.FAILED -> {
                            // Handle error
                        }
                        else -> {}
                    }
                }
        }
    }
}
```

#### In Your ViewModel:

```kotlin
@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranDataRepository: QuranDataRepository
) : ViewModel() {

    val surahs: Flow<List<SurahEntity>> =
        quranDataRepository.getAllSurahs()

    fun getAyahsForSurah(surahNumber: Int): Flow<List<AyahEntity>> =
        quranDataRepository.getAyahsForSurah(surahNumber)

    suspend fun getAyah(surahNumber: Int, ayahNumber: Int): AyahEntity? =
        quranDataRepository.getAyah(surahNumber, ayahNumber)
}
```

### Option 2: Manual Testing with Python Script

For testing the API outside the app:

```bash
cd D:\Dev\Apps\quran_media
python scripts/download_quran_data.py
```

This will:
1. Download all 114 Surahs from the API
2. Save to `quran_data/complete_quran.json`
3. Create individual files in `quran_data/surahs/`
4. Generate CSV file with all Ayahs
5. Display statistics

**Output:**
```
quran_data/
├── complete_quran.json      # All data in one file
├── statistics.json           # Statistics summary
├── all_ayahs.csv            # CSV format
└── surahs/
    ├── surah_001.json
    ├── surah_002.json
    └── ...
```

## API Endpoints Used

- **Base URL**: `http://api.alquran.cloud/v1/`
- **Get all Surahs metadata**: `GET /surah`
- **Get specific Surah**: `GET /surah/{number}`
- **Documentation**: https://alquran.cloud/api

## Key Components

### 1. Database Entities

- `app/src/main/java/com/quranmedia/player/data/database/entity/AyahEntity.kt`
- `app/src/main/java/com/quranmedia/player/domain/model/Ayah.kt`

### 2. Database Access

- `app/src/main/java/com/quranmedia/player/data/database/dao/AyahDao.kt`

### 3. API Service

- `app/src/main/java/com/quranmedia/player/data/api/AlQuranCloudApi.kt`
- `app/src/main/java/com/quranmedia/player/data/api/model/QuranApiResponse.kt`

### 4. Background Worker

- `app/src/main/java/com/quranmedia/player/data/worker/QuranDataPopulatorWorker.kt`

### 5. Repository

- `app/src/main/java/com/quranmedia/player/data/repository/QuranDataRepository.kt`

## Database Migration

The database version has been updated from `1` to `2` to include the new `ayahs` table.

**Important**: If you have an existing database, you'll need to:
- Uninstall and reinstall the app, OR
- Implement a migration strategy in `QuranDatabase`

## Example Queries

### Get all Ayahs for Surah Al-Fatiha (1):
```kotlin
quranDataRepository.getAyahsForSurah(1).collect { ayahs ->
    ayahs.forEach { ayah ->
        println("${ayah.ayahNumber}: ${ayah.textArabic}")
    }
}
```

### Get Ayahs by page:
```kotlin
quranDataRepository.getAyahsByPage(1).collect { ayahs ->
    // Display all ayahs on page 1
}
```

### Get all Sajda Ayahs:
```kotlin
quranDataRepository.getSajdaAyahs().collect { sajdaAyahs ->
    // Display all ayahs requiring prostration
}
```

### Get Ayahs by Juz:
```kotlin
quranDataRepository.getAyahsByJuz(30).collect { ayahs ->
    // Display all ayahs in Juz 30
}
```

## Performance Considerations

- **First Time Download**: Takes ~2-3 minutes to download all 114 Surahs
- **Storage**: ~5-10 MB for all text data
- **Network**: Requires internet connection only for initial download
- **Caching**: All data is cached locally in Room database
- **API Rate Limiting**: Worker includes 100ms delay between requests

## Troubleshooting

### Data Not Downloading?

1. Check internet connection
2. Check WorkManager status:
   ```kotlin
   quranDataRepository.getPopulationWorkStatus()
   ```

### Database Version Conflict?

Clear app data or uninstall/reinstall the app.

### Want to Re-download Data?

```kotlin
lifecycleScope.launch {
    ayahDao.deleteAllAyahs()
    surahDao.deleteAllSurahs()
    quranDataRepository.startQuranDataPopulation()
}
```

## Credits

- **API**: [Al-Quran Cloud](https://alquran.cloud/)
- **Edition**: Uthmani script (quran-uthmani)

## License

The Quran text is public domain. API usage follows Al-Quran Cloud's terms of service.
