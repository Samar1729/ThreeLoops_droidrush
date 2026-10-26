# Mirror Archive (Team ThreeLoops • Droidrush 2026)

Mirror Archive is a 100% on-device Android gallery organizer built with bundled on-device machine learning, perceptual hashing, and encrypted local storage with strictly zero network permissions.

---

## Team Ownership

| Member | Focus | Responsibilities |
| :--- | :--- | :--- |
| **Aditya** | **Machine Learning & UI** | Image Analysis, Smart Albums, Search, Face Quality & Jetpack Compose UI |
| **Rishav** | Storage & Indexing | MediaStore Source, Room & SQLCipher DAOs, Background Indexing Worker, Keystore Security |
| **Samar** | Hashing & Deduplication | Perceptual Hasher (dHash/pHash), Duplicate Bucketing, Purge Flow, Burst Shot Selection |

---

## Native Android Application (`/android`)

The native Jetpack Compose implementation is located under `android/`:

- **Design System:** Apple-inspired monochrome palette, SF Pro-style typography, high-contrast badges, fluid animations.
- **Smart Albums:** Automatic categorization for Receipts, Screenshots, Documents, Food, and People.
- **Instant Search:** Tag-based semantic matching and fast on-device text OCR lookup with substring highlighting.
- **People & Faces:** On-device face detection with eyes-open, smile detection, and sharpness scoring.
- **Zero Internet:** Built with strictly 0 network permissions (`android.permission.INTERNET` omitted from Manifest).

---

## Architecture

Mirror Archive is designed with an offline-first, strictly on-device architecture adhering to Clean Architecture principles:

- **Presentation Layer (`ui/`):** Jetpack Compose with Material 3, Navigation Compose, unidirectional data flow (UDF) backed by StateFlow/Flow. Injected using Hilt.
- **Domain & Contracts (`contracts/`):** Pure Kotlin interfaces and models defining repository, analyzer, hasher, and duplicate contracts. Free of Android platform dependencies where possible.
- **Data Layer (`data/`):**
  - **`data/mediastore/`:** `MediaStoreDataSource` and `BitmapLoader` querying system media strictly on `Dispatchers.IO`.
  - **`data/repo/`:** `ImageRepository` orchestrating local database cache and MediaStore data.
  - **`data/db/`:** Encrypted local database utilizing SQLCipher and Android Keystore.
- **Machine Learning (`ml/`):** Bundled on-device Google ML Kit models (Image Labeling, Face Detection, Text Recognition OCR).
- **Hashing & Deduplication (`hash/`, `dupes/`):** Perceptual hashing (dHash/pHash) and Hamming distance bucket clustering for duplicate detection.
- **Background Work (`work/`):** WorkManager foreground services for intensive batch indexing and media scanning.
- **Security & Permissions (`permissions/`):** Granular permission manager for API 33+ (READ_MEDIA_IMAGES) and API 34+ partial photo picker (READ_MEDIA_VISUAL_USER_SELECTED).

---

## Privacy: no INTERNET permission

- **Zero Network Permission:** `android.permission.INTERNET` is **never** declared in `AndroidManifest.xml`. The OS guarantees network isolation.
- **100% On-Device Processing:** All computer vision, OCR, indexing, and duplicate detection operations execute locally on the device CPU/GPU/NPU.
- **Bundled ML Models:** Employs bundled Google ML Kit artifacts (`com.google.mlkit:image-labeling`, `face-detection`, `text-recognition`) without Play Services network model download dependencies.
- **Local Encryption:** Encrypted storage with SQLCipher ensures all indexed image metadata and face metrics remain secure on-device.
- **Backup Disabled:** `android:allowBackup="false"` prevents extraction via ADB or cloud device backups.

## Scan state machine

`ScanController` uses WorkManager's unique scan names and the persisted Room checkpoint to report progress across process restarts. `IDLE` means no scan has run; `QUEUED` is accepted work waiting to start; `SCANNING` is active processing; `PAUSED` is retrying, stopped, cancelled, or interrupted before the checkpoint reached the total; `SYNCING` is MediaStore reconciliation; `COMPLETE` means the scan reached its total or WorkManager succeeded; and `FAILED` means WorkManager reported a terminal failure. A full rescan resets the checkpoint and processed count but leaves indexed rows available until refreshed.

```
android/app/src/main/java/com/threeloops/mirrorarchive/
├── ml/
│   └── ImageAnalyzer.kt             # Bundled ML Kit Image Labeling, Face Detection, OCR
├── albums/
│   ├── AlbumClassifier.kt           # Classification rules and tag matching
│   └── AlbumRules.kt                # Keywords and categorization thresholds
├── search/
│   └── SearchViewModel.kt           # Search filtering with highlight ranges
├── people/
│   └── PeopleViewModel.kt           # Face quality scoring (smile and eyes-open metrics)
├── ui/
│   ├── theme/                       # Clean monochrome theme and typography
│   ├── navigation/                  # Screen routes and bottom navigation dock
│   ├── albums/                      # AlbumsScreen & AlbumDetailScreen
│   ├── search/                      # SearchScreen (Tag and Text segmented toggle)
│   ├── people/                      # PeopleScreen
│   └── imagedetail/                 # ImageDetailScreen (Face boxes and OCR inspector)
└── data/
    ├── model/                       # ImageItem, AlbumSummary, FaceInfo, LabelResult
    └── repo/ImageRepository.kt      # Local data repository
```

---

## Interactive Web Preview (`/web`)

An interactive monochrome frontend preview is available in `web/` for visual exploration:
- Built with React, TypeScript, and Tailwind CSS.
- Mirrors the Android app screens and layout.
- Run with:
  ```bash
  cd web
  npm install
  npm run dev
  ```

---

## Getting Started: Android Development Setup

To build and run the Android app locally:

### 1. Install JDK 17
```bash
brew install openjdk@17
```
Add to `~/.zshrc`:
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="$JAVA_HOME/bin:$PATH"
```

### 2. Install Android Studio
- Download **Android Studio** from [developer.android.com/studio](https://developer.android.com/studio).
- Install the Android SDK (API 34/35) and an Android Virtual Device (AVD).

### 3. Open and Run
1. Open Android Studio.
2. Select **Open** and choose the `android/` directory.
3. Wait for Gradle sync to complete.
4. Select a virtual device or connected phone and click **Run**.
