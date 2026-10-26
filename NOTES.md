# R-1 to R-4 implementation and verification notes

## Implemented

- **R-1 — MediaStore contracts and source:** `MediaRow` represents image `_ID`, URI, display/path metadata, dates, generation, dimensions, size, and MIME type. `MediaStoreDataSource` queries the external images collection, maps cursor rows, counts images, and creates row URIs. Queries handle denied access and other failures without crashing.
- **R-2 — MediaStore paging and sync support:** `query(afterId, limit)` uses `_ID > ?`, `_ID ASC`, and a limit; `changedSince(generation)` filters `GENERATION_MODIFIED > ?`; `allIds()` returns ascending IDs; and `currentGeneration()` reads `MediaStore.getGeneration` for `VOLUME_EXTERNAL`.
- **R-3 — Paging/count proof:** Albums shows `Photos on device: N` when the app is in its permission-granted flow. In debug builds, **Load first 50** logs two pages of IDs, page sizes, strict ascending order, and overlap status with Logcat tag `MirrorMediaStore`. Page 2 starts after page 1's last ID.
- **R-4 — Thumbnail loading:** The singleton Hilt-injected `BitmapLoader` loads only MediaStore thumbnails, converts hardware bitmaps to software bitmaps, offers a 1024-pixel OCR load and square luminance extraction, and provides null-safe recycling. Debug Albums UI has **Test loader**. Instrumented tests cover an invalid URI and a real image inserted into MediaStore for the test.

## Assumptions

- `IndexedImage` retains its existing required base fields (`mediaId`, `uri`, `labels`, `ocrText`, `dHash`, `pHash`, `blurScore`, and `faceCount`). Added descriptive metadata, face/albums details, and `pipelineVersion` have defaults so existing construction sites remain source-compatible.
- MediaStore access uses the external images collection and the app's existing full or partial image permission flow. A partial grant counts and pages only the images visible to the app.
- `_id` keyset paging is the proof path; IDs are expected to be strictly increasing within the visible result set.
- Instrumented loader tests insert and remove a small temporary PNG in MediaStore rather than depending on the test device having personal photos.
- BitmapLoader callers run off the main thread and recycle returned bitmaps in `finally`; long-lived state contains IDs/hashes, not bitmap collections.

## Manual verification steps by completion check

1. **R-1 source/count:** Install and launch on Android 11 or newer, grant image access, open Albums, and confirm `Photos on device: N` is a non-placeholder count. Revoke image access in Settings and confirm the app remains stable when revisiting its permission flow.
2. **R-2 pagination/sync methods:** With image access, tap **Load first 50** in a debug build. Filter Logcat by `MirrorMediaStore`; verify each page has at most 50 IDs, IDs are strictly ascending across both pages, and `noOverlap=true`. Page 2 is queried with page 1's last ID as `afterId`. The generation helpers are exercised by compiling and by MediaStore permission/runtime integration; they are not exposed as UI actions.
3. **R-3 count and paging proof:** Confirm the displayed count matches the image count visible to the app. Repeat paging with fewer than 51 visible images and with 51 or more; both page sizes must be at most 50, with strict ascending IDs and no overlap. Empty pages are valid for a small library.
4. **R-4 loader proof:** In Albums on a debug build, tap **Test loader** with at least one visible photo. Confirm the result reports the first photo's width × height and `<=512: true`, then `bad URI returned null: true`. The app must remain open. With no visible photos, confirm the UI reports that and still shows the bad-URI result. Run `./gradlew :app:connectedDebugAndroidTest` on a connected device/emulator to verify the invalid-URI and inserted-MediaStore-image instrumentation cases.
5. **Offline manifest:** Run `./gradlew :app:assembleDebug`, then inspect `app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml` (or the merged manifest output for the installed AGP) and verify there is no `android.permission.INTERNET` entry.

## Gradle, SDK, and library versions

- Gradle wrapper: **9.8.2** (`android/gradle/wrapper/gradle-wrapper.properties`).
- Android Gradle Plugin: **8.7.2**; compile SDK **35**, min SDK **30**, target SDK **35**; Java/Kotlin bytecode target **17**.
- Verification JDK: Homebrew OpenJDK **17.0.21.0.2**.
- Kotlin: **2.0.21**; KSP: **2.0.21-1.0.28**.
- Hilt: **2.51.1**; Hilt Navigation Compose: **1.2.0**.
- AndroidX Core KTX: **1.15.0**; Lifecycle: **2.8.7**; Activity Compose: **1.10.0**; Compose BOM: **2024.12.01** (Compose UI **1.7.6**, Material3 **1.3.1**); Navigation Compose: **2.8.5**; Biometric: **1.2.0-alpha05**.
- Coil Compose: **2.7.0**; Room: **2.6.1**; WorkManager: **2.10.0**; DataStore Preferences: **1.1.2**.
- SQLCipher Android: **4.6.1**; AndroidX SQLite: **2.4.0**.
- Bundled ML Kit Image Labeling: **17.0.9**; Face Detection: **16.1.7**; Text Recognition: **16.0.1**; Coroutines Play Services: **1.9.0**.
- JUnit: **4.13.2**; AndroidX JUnit: **1.2.1**; Espresso: **3.6.1**; Coroutines Test: **1.9.0**.

## Contract change requests

No contract change requests identified. Frozen signatures remain unchanged; any future proposals should be listed here before changing them.

## Verification run for R-4b

- `./gradlew assembleDebug` — **PASS**.
- `./gradlew testDebugUnitTest` — **PASS**.
- `./gradlew compileDebugAndroidTestKotlin` — **PASS**; no connected device was available to execute the instrumentation tests.
- The assembled debug merged manifest at `android/app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml` has no `android.permission.INTERNET` permission element. The app manifest removes the dependency-added permission to preserve the offline requirement.
- The machine's ignored Gradle JVM criteria pointed to a missing VS Code JRE `jlink`. Verification ran with the installed JDK 17 while temporarily moving that ignored criteria file; it was restored after each command.

## R-5 to R-7 implementation notes

### Implemented

- **R-5 — Plain Room schema and data access:** Added images, labels, album entries, faces, hash buckets, scan state, skipped-image, and pipeline-stat entities. The database enables foreign keys, exports schema JSON, and uses destructive migration fallback during development. DAOs save image/child batches atomically, preserve generated image IDs, query album/search/face/blur data, and expose checkpoint and skip state. Instrumented DAO tests cover 1,000 rows over batches, reopen persistence, prefix search, cascade deletion, album summaries, updates by MediaStore ID, and idempotent skips.
- **R-6 — Room repository:** Replaced the runtime fake repository binding with `RoomImageRepository`. It maps indexed records to Room entities, lowercases and de-duplicates labels, writes eight dHash byte buckets, tracks pipeline version and checkpoints, expands tag synonyms with AND across query tokens, and exposes real Room-backed flows for albums, OCR, suggestions, faces, blur, and duplicates. Repository instrumentation tests cover two-token search, synonyms, and live flow emissions.
- **R-7 — Resumable scan and UI:** Added `ScanController` unique work scheduling and persisted progress, plus a Hilt `IndexWorker` that scans MediaStore in ascending `_id` pages of 50 with up to three images processed concurrently. Each completed page advances the checkpoint even when some or all rows are skipped. The worker writes source-row progress, pauses on cancellation, reports revoked media access as FAILED, and uses a `dataSync` foreground notification. The app configures WorkManager with Hilt's worker factory. The main scaffold shows live queued/scanning/paused/failed/complete states; Albums exposes **Scan now** and **Cancel**, and launches an initial or resumed scan after media access is ready. Albums, Search, and People observe the Room repository flows. A testable `MediaStoreSource` boundary supports the fake-source worker instrumentation tests for completion, restart without repeated IDs, and corrupt-image skip/checkpoint behavior.

### Assumptions

- The existing `IndexedImage` base fields remain the frozen contract; optional descriptive metadata, face/albums data, and pipeline version use the existing defaults.
- MediaStore `_id` is the durable keyset checkpoint. A checkpoint advances only after the whole page has been processed and persisted; cancellation during a page safely repeats that page on restart.
- Progress counts source rows, including rows skipped by the image pipeline. Persisted image count may therefore be lower than scan progress.
- A partial Android 14+ photo grant scans only the images visible to the app. Notification permission is optional; WorkManager foreground execution continues if notifications are denied.
- R-7 uses the stub analyzer and hasher bindings while the pipeline is integrated. The MediaStore/thumbnail/worker paths are real; photo analysis quality is not represented by the stubs.
- The fake-source worker tests exercise WorkManager with a real `IndexWorker`, repository fixture, pipeline, skip recording, and scan-state behavior. They do not replace on-device validation against a personal gallery or simulate Android process death at the OS level.

### Manual verification steps for R-5 to R-7

1. **R-5 schema and DAOs:** Run `./gradlew :app:connectedDebugAndroidTest` on an emulator/device. Confirm the Room DAO tests pass, including the 1,000-row on-disk close/reopen checkpoint case, child cascades, label prefix cases, album summary cover, mediaId update, and repeated skipped insert.
2. **R-6 live repository:** Run the connected instrumented tests and confirm two-token AND search, synonym expansion, and a new tag appearing in a collector without restarting the flow. The Albums, tag search, and People screens now observe repository flows. Current R-7 stub analyzer returns no labels/faces, so those result screens can be empty until a real analyzer is selected.
3. **R-7 completion and progress:** Grant image access, open Albums, and confirm the initial scan starts. Confirm the top banner advances through `Indexing N / total` to complete. For a controlled 1,000-photo run, use an emulator/device with 1,000 generated MediaStore images and confirm the run reaches `1,000 / 1,000` without a crash.
4. **R-7 restart:** Start a scan over a large gallery, force-stop the app after progress advances, relaunch, and confirm the work resumes from its persisted `_id` checkpoint without repeating saved pages.
5. **R-7 cancellation and manual controls:** Tap **Scan now**, verify the unconstrained scan begins, then tap **Cancel** and confirm the banner reports paused while the checkpoint remains. Start again and verify it resumes.
6. **R-7 corrupt media and permission revoke:** Include an unreadable/corrupt URI, confirm it is recorded as skipped and the checkpoint passes its ID. During a separate long run, revoke photo permission in system Settings; confirm the run stops in FAILED and the app remains usable. Restore permission and reopen to resume the incomplete initial scan.

### Additional R-7 dependencies

- AndroidX Hilt Work: **1.2.0**.
- AndroidX Work testing: **2.10.0** (same version as WorkManager runtime).

## Verification run for R-7d

- `./gradlew assembleDebug` — **PASS**.
- `./gradlew testDebugUnitTest` — **PASS**.
- `./gradlew compileDebugAndroidTestKotlin` — **PASS**; worker and prior DAO/repository/loader instrumentation sources compile.
- `./gradlew connectedDebugAndroidTest` — **NOT RUN**: Gradle built the instrumentation APK, then reported `No connected devices!` at `:app:connectedDebugAndroidTest`. The three worker integration cases and existing instrumentation tests still need execution on an emulator/device.
- The fake-source completion test scans 1,000 rows; cancellation/resume and corrupt-row checkpoint cases are included in `IndexWorkerInstrumentedTest` but are not claimed as passing until run on a device.
- Manual 1,000-photo gallery, force-stop/resume, and mid-scan permission-revoke checks remain device validation items.
