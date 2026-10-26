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
