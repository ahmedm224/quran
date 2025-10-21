# Goal
Build a focused Android media player for Quran recitation that also works on Android Auto, with **exceptional, precise seeking** for long surahs and ayah-level navigation.

---

## 1) Product Scope & UX
**Core users:** listeners who want precise control (students, memorizers, teachers), often while commuting.

**Primary use cases**
- Play any surah by any selected reciter, with ayah-level timestamps.
- Surgical seeking: jump to an ayah, scrub smoothly within an ayah, nudge by small increments (e.g., 250 ms / 1 s), repeat-loop a verse.
- Bookmarks: verse-level bookmarks and loop ranges for memorization.
- Android Auto: safe, minimal controls—browse reciters/surah, resume, skip to next/previous ayah.
- Offline playback with selective downloads (reciter → surah or ayah ranges).

**Information architecture (phone)**
- Home: Continue Listening, Reciters, Surahs, Bookmarks, Downloads.
- Player screen: Wave/seek bar (if enabled), ayah list, loop controls (A↔B), speed, pitch lock, sleep timer.
- Search: Arabic, transliteration, translation, surah/ayah, reciter.

**Android Auto UX**
- Media Browser hierarchy: **Reciters → Surahs → Ayahs** (browsable nodes).
- Templates: Now Playing + Browse; actions limited to Play/Pause, Next/Previous Ayah, “Go to Ayah…”, Bookmarks (last 5), Loop toggle.
- Voice: “Play Surah Al-Baqarah by Al-Afasy”, “Skip to ayah 255”.

---

## 2) Content & Data
**Audio sources**
- Support both **streaming** and **offline** assets per reciter.
- Allow user-imported files (local MP3/FLAC) with sidecar timing when available.

**Ayah timing index**
- Store for each (reciter, surah): list of ayah start/end timestamps (ms).
- Format options: JSON or SQLite table (`ayah_index`), versioned by reciter.
- Fallback strategies when timing is missing: (a) disable ayah jumps; (b) heuristic segmentation (optional, not default) using silence detection or provided cue sheets.

**Metadata model (Room)**
- `reciter(id, name, style, source, version)`
- `surah(id, number, name_ar, name_en, ayah_count)`
- `audio_variant(id, reciter_id, surah_id, bitrate, url, local_path, duration_ms, hash)`
- `ayah_index(reciter_id, surah_id, ayah_number, start_ms, end_ms)`
- `bookmark(id, reciter_id, surah_id, ayah_number, position_ms, label, loop_end_ms?)`
- `download_task(id, status, progress, bytes_total, bytes_done)`
- Settings via **Proto DataStore** (e.g., seek increments, speed, gapless, auto-resume).

**Translations (optional phase 2)**
- Lightweight text overlay with configurable translation; lazy-load per surah.

---

## 3) Playback & Seeking (Precision First)
**Engine**: **ExoPlayer** (audio-only) for modern APIs, stable buffering, and Android Auto integration.

**Surgical seeking design**
- **Exact seek semantics** for audio: configure player seek to *exact* (not closest keyframe) and maintain tight buffer control.
- **Two-stage scrubbing**
  1. **Coarse scrub** across the surah (fast, inertial). Snap stops to nearest ayah boundary when user releases.
  2. **Fine scrub** within an ayah: high-resolution slider with acceleration curve (log-scale) and haptic ticks at 100–250 ms.
- **Seek nudges**: configurable ±250 ms / ±1 s buttons; press-and-hold repeats.
- **Ayah snap & preview**: when hovering, show ayah number, start time, and (optional) transliteration snippet.
- **Looping**: A↔B loop between verse or arbitrary positions; ensure buffer pre-roll on loop boundaries to eliminate pops.
- **Gapless playback**: enforce continuous audio between ayahs (playlist or single-file with indices). Pre-prepare next segment.
- **Auto-replay-on-seek**: microfade-in (5–10 ms) after seeks to mask artifacts.

**Playlist strategy**
- Option A (preferred): single track per surah + ayah indices for jumps.
- Option B: concatenated ayah clips via concatenating media source; ensure gapless & consistent loudness.

**Buffering strategy**
- Increase back buffer within ayah, moderate forward buffer; switch to aggressive prefetch near loop boundaries.
- For streaming, allow **short buffer** for responsive seeks; dynamic rebuffer policy based on network.

**Audio DSP (optional)**
- Speed control (0.5×–1.5×) with pitch preservation.
- Loudness normalization per reciter (EBU R128-like analysis offline if assets permit).

---

## 4) Android Auto & Media Integration
**Key components**
- `MediaBrowserServiceCompat` exposed to Android Auto / Assistant.
- `MediaSession` + `MediaStyle` notification (phone) and `PlaybackState` with actions: PLAY/PAUSE, SKIP_TO_NEXT/PREVIOUS (ayah), SEEK_TO, SET_SHUFFLE_MODE (off), SET_REPEAT_MODE (repeat one / loop range).
- Content tree via `MediaBrowserService` with nodes for Reciters → Surahs → Ayahs; queue items encode (reciter, surah, ayah_start_ms).

**Voice & search**
- Implement `onPlayFromSearch` to parse utterances: surah names (Arabic and English), numbers, “ayah 5”, reciter name.
- Provide phonetic aliases and transliteration dictionary.

**Automotive OS (future)**
- For AAOS, add **Android for Cars App Library** `Media` template; share same MediaSession.

**Compliance**
- Adhere to Google’s **Android Auto app quality**: no distractions, minimal text, no arbitrary seeking gestures beyond templates, safe actions only.

---

## 5) Offline & Downloads
**Downloader**: `WorkManager` foreground tasks with OS-resilient retries, pause/resume, and Wi‑Fi-only option.

**Granularity**
- Whole surah or selected ayah range; automatically fetch matching timing index.

**Storage**
- Scoped Storage compliant; cache directory for streamed chunks; migration helper for legacy paths.

**Integrity**
- Verify file hash when available; fallback to file size/duration checks.

---

## 6) Internationalization & Accessibility
- **RTL layout** first-class; Arabic fonts with diacritics rendering where text appears.
- **TalkBack**: all controls labeled; announce ayah number on focus/seek.
- **Large text** modes; high-contrast theme.
- **Transliteration input** for search; fuzzy matching.

---

## 7) Telemetry & Quality
**Metrics**
- Seek latency (tap-to-audio), rebuffer counts, loop usage, crashes, ANR.
- Playback continuity on Android Auto sessions.

**Diagnostics**
- Opt-in local log export for support (no PII).

**Testing**
- Unit tests: timing index, snap logic, loop boundaries.
- Instrumentation: seek latency under network profiles; car projection tests.
- Compatibility: Android 8.1+ (minSdk 27 suggested) up to latest.

---

## 8) Data Pipeline for Ayah Timings
**Goal**: maintain a reliable, versioned repository of ayah timestamps per reciter.

**Inputs**
- Vendor-provided timings, community datasets, or generated alignments.

**Validation**
- Ensure monotonicity and coverage (ayah_count matches surah schema).
- Audible QA: spot-check 3–5 ayahs per surah; boundary crossfade test.

**Packaging**
- Per-reciter JSON bundles; CDN-hosted with semantic version (e.g., `alafasy-timings-v3.json`).
- App downloads only the needed surah’s subset to minimize data.

---

## 9) Security, Privacy, Licensing
- No ads; no tracking of religious content choices beyond anonymous metrics (opt-in).
- Respect recitation licensing; store license text per reciter; show in About.
- HTTPS-only for content delivery.

---

## 10) Release Plan & Milestones
**M1 — Foundation (3–4 weeks)**
- App shell, Room + DataStore, ExoPlayer playback of local sample.
- Basic MediaSession + notification, background playback.

**M2 — Quran Model & Browsing (3–4 weeks)**
- Surah/reciter catalog; search (Arabic + transliteration).
- Timing index storage + loading; ayah list UI.

**M3 — Precision Seeking (4–5 weeks)**
- Exact seek, two-stage scrubber, nudge buttons, snap-to-ayah.
- Loop A↔B; microfade; gapless across ayahs.

**M4 — Android Auto Integration (2–3 weeks)**
- MediaBrowserService tree; browse/play; voice queries; QA against templates.

**M5 — Offline & Downloads (2–3 weeks)**
- WorkManager download, progress UI, storage policies, integrity checks.

**M6 — Polish & Accessibility (2–3 weeks)**
- RTL audit, TalkBack, large text, theming; telemetry and diagnostics.

**Beta**: limited rollout, collect feedback on seeking responsiveness and timings accuracy.

---

## 11) Risks & Mitigations
- **Timing dataset gaps** → ship with best-known reciters first; clear labels; allow users to supply sidecar.
- **Seek latency on low-end devices** → keep buffer small; exact seek + prefetch; reduce waveform rendering.
- **Android Auto rejections** → strict adherence to templates; keep actions minimal; thorough testing on projection.
- **Licensing fragmentation** → central license registry per reciter.

---

## 12) Nice-to-Haves (Post v1)
- Verse waveform preview (audio thumbnail) with peak cache.
- Smart repeat (auto-extend loop to silence boundaries).
- Memorization mode: spaced repetition on verse ranges.
- Cloud sync of bookmarks across devices.
- AAOS native app variant.

---

## 13) Acceptance Criteria (v1)
- Start playback of any surah by selected reciter; Android Auto browse/play works.
- Seek to any ayah with ≤150 ms median latency after touch-up (on mid‑range device).
- Fine scrubbing within ayah with haptic ticks and visual cursor.
- Loop A↔B is sample-accurate (≤20 ms boundary error) with no audible click.
- Offline download/playback works; integrity verified.
- Accessibility passes TalkBack and large-text checks; RTL fully supported.

---

## 14) Team & Tools
- **Android Engineer(s):** app, playback, AA integration.
- **Data Engineer / QA:** timing datasets, validation, tooling.
- **Designer:** mobile + Android Auto templates, RTL.
- **PM**: scope & launches.

**Tech stack**: Kotlin, ExoPlayer, Room, DataStore (Proto), WorkManager, Hilt, Coroutines/Flows, Jetpack Compose (phone) + Car app templates, Firebase Crashlytics/Analytics (opt-in), Gradle CI.

---

## 15) Next Steps (You)
1) Confirm initial reciter list (whose timings you can source reliably).
2) Decide on minimum OS version and whether waveform is part of v1.
3) Pick the exact seek nudge increments you prefer (e.g., 250 ms and 1 s).
4) Provide any branding preferences (name, icon, theme).

