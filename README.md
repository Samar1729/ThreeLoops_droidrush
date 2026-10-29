# Mirror Archive (Team ThreeLoops • Droidrush 2026)

[![Zero Internet Permission](https://img.shields.io/badge/Network-0%20Internet%20Permissions-00C853.svg)](#privacy-guarantee-zero-internet-permission)
[![Platform](https://img.shields.io/badge/Platform-Android%2014%2B%20%7C%20API%2034%2B-blue.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room%20%2B%20SQLCipher%20256--bit-FF6F00.svg)](https://www.zetetic.net/sqlcipher/)
[![Tests](https://img.shields.io/badge/Unit%20Tests-61%20Passed-brightgreen.svg)](#testing)

**Mirror Archive** is an ultra-private, 100% on-device intelligent gallery manager and offline archive for Android. Built strictly with **zero internet permissions**, Mirror Archive brings enterprise-grade computer vision, perceptual hashing, duplicate clustering, receipt expense tracking, and storage optimization directly to your smartphone CPU/NPU without a single byte ever leaving your device.

---

## Team Ownership & Architecture

| Member | Focus | Responsibilities & Implemented Modules |
| :--- | :--- | :--- |
| **Samar** | **Deduplication, Benchmark & Storage Saver** | **S-1** Runtime Permissions Manager (API 30–34+), **S-2** Index WorkManager Pipeline, **S-3** 64-bit dHash Duplicate Clustering & Burst Selector, **S-4** Storage Saver (Laplacian Blur 10th-percentile), **S-5** Zero-Permission Sandbox & Reset, **S-6** Incremental ContentObserver Sync, **S-7** dHash Throughput Benchmark (Module 3.19). |
| **Rishav** | **Storage, MediaStore & Telemetry** | **R-1** Room Database & Encrypted SQLCipher Migrations, **R-2** MediaStore Ingestion Scanner, **R-3** Batch Purge Plan & MediaStore Intent Sender, **R-4** Zip Archive Export Engine, **R-5** Telemetry Metrics Inbox & Analytics, **R-6** Heuristic Rule-based Album Classifier. |
| **Aditya** | **Machine Learning & Jetpack Compose UI** | Bundled on-device Google ML Kit (Image Labeling, Face Detection, OCR), Face Quality Scoring (Smile & Eyes-Open probability), Receipt OCR Parser (*Raseed Ledger*), and Jetpack Compose Glassmorphism Design System. |

---

## Key Features

### 1. Smart Albums & Classification
- **Automatic Rule-Based Categorization:** Instant classification into **Screenshots**, **Documents**, **Food**, **Receipts**, **People**, and **Places**.
- **Edge ML Labeling:** Offline semantic image recognition powered by bundled Google ML Kit.
- **Batch ZIP Export:** Export entire albums or selected photos into an uncompressed or compressed ZIP archive with deterministic file naming.

### 2. Duplicate Detection & Burst Selection
- **64-bit Grayscale dHash:** High-efficiency difference hashing comparing adjacent gradients.
- **Hamming Distance Bucketing:** Sub-quadratic clustering identifying duplicate groups with distance $\le 10$.
- **Smart Burst Selector:** Automatically preserves the best frame (highest resolution, least blurry, highest face score) while designating candidates for reclaimable space.
- **Scoped Storage Purge:** Direct integration with Android's system `createDeleteRequest` intent sender for secure MediaStore deletion.

### 3. Storage Saver & Blur Analysis
- **Laplacian Variance Blur Scoring:** Evaluates sharpness and identifies blurred or degraded photos.
- **Dynamic 10th-Percentile Thresholding:** Calculates intelligent suggestions based on the distribution of your local photo library.
- **Reclaimable Space Calculation:** Reactive computation of potential storage savings with one-click purge.

### 4. Raseed Ledger (Receipt Expense Scanner)
- **Offline OCR Pipeline:** Bundled Google ML Kit Text Recognition scans physical receipts and invoices.
- **Autonomous Raseed Parser:** Extracts vendor name, date, itemized rows, tax, payment method, and currency totals (INR `₹`, USD `$`, EUR `€`, GBP `£`).
- **Dynamic Expense Analytics:** Aggregates spend totals across categories (Groceries, Dining, Utilities, Retail).

### 5. People & Face Quality Intelligence
- **On-Device Face Detector:** Tracks face count and bounding boxes.
- **Quality Scoring:** Ranks portrait photos using combined smile probability and eyes-open metrics to prioritize your best memories.

### 6. Hardware Benchmarking & Telemetry
- **Hash Throughput Benchmark (S-7):** Benchmarks dHash generation throughput in images per second directly on your phone's processor.
- **Live Metrics Dashboard:** Monitors total scan duration, database file size, search latencies, and total reclaimed storage.
- **Now-Indexing Progress Bar:** Real-time foreground WorkManager progress updates with phase transitions (`QUEUED`, `SCANNING`, `SYNCING`, `PAUSED`, `COMPLETE`).

---

## Privacy Guarantee: Zero Internet Permission

- **No `android.permission.INTERNET`:** The Android OS mathematically guarantees that Mirror Archive cannot open sockets, transmit telemetry, or make HTTP/DNS requests.
- **Bundled Machine Learning Models:** All ML models are packaged directly within the APK (`com.google.mlkit:image-labeling`, `face-detection`, `text-recognition`)—eliminating any runtime model downloads from Google Play Services.
- **Encrypted Local Storage:** Database tables and metadata are secured via **SQLCipher** and the **Android Keystore**.
- **Backup Exclusion:** `android:allowBackup="false"` prevents cloud backup extraction via Google Drive or ADB backups.

---

## Project Structure

```
android/app/src/main/java/com/threeloops/mirrorarchive/
├── albums/                          # Rule-based album classifier & categories
├── contracts/                       # Frozen domain contracts & immutable data models
├── data/
│   ├── db/                          # Room Database, DAOs & SQLCipher security
│   ├── export/                      # ZIP Archive Export Worker & ViewModel
│   ├── mediastore/                  # MediaStore scanner, queries & ContentObserver
│   └── repo/                        # RoomImageRepository implementation
├── dupes/                           # 64-bit dHash, Burst selector, Purge ViewModel
├── metrics/                         # Benchmark (S-7) & MetricsInbox telemetry
├── ml/                              # Bundled ML Kit Image Labeling, Face & OCR Analyzers
├── people/                          # Face quality scoring & PeopleViewModel
├── permissions/                     # PermissionManager (API 30–34+ reactive flow)
├── search/                          # Search engine with text highlight ranges
├── security/                        # Keystore & database reset recovery notifier
├── storage/                         # StorageSaverViewModel & Laplacian blur thresholding
├── ui/                              # Jetpack Compose presentation layer
│   ├── albums/                      # AlbumsScreen & AlbumDetailScreen
│   ├── auth/                        # Biometric / PIN Vault unlock screen
│   ├── dupes/                       # DuplicatesScreen & DuplicateGroupDetailScreen
│   ├── export/                      # ExportScreen (ZIP progress & destination)
│   ├── imagedetail/                 # ImageDetailScreen (Face boxes & OCR inspector)
│   ├── ledger/                      # RaseedLedgerScreen (Receipt expense breakdown)
│   ├── metrics/                     # MetricsScreen (Benchmark trigger & telemetry)
│   ├── navigation/                  # MainViewModel, NowIndexingBar, Routes, BottomBar
│   ├── onboarding/                  # First-launch onboarding gate
│   ├── people/                      # PeopleScreen (Best portraits)
│   ├── permission/                  # PermissionDeniedScreen & partial access gate
│   ├── search/                      # Instant Search screen
│   ├── settings/                    # SettingsScreen (Zero-Internet badge & Wipe/Rescan)
│   ├── storage/                     # StorageSaverScreen (Blurry photo purge)
│   └── theme/                       # Glassmorphic dark theme
└── work/                            # WorkManager IndexWorker & IncrementalSyncWorker
```

---

## Getting Started

### Prerequisites
- **JDK 17 or JDK 21**
- **Android Studio Ladybug (2024.2+)** or later
- **Android SDK:** API 34 / 35
- Physical device or Emulator running **Android 11+ (API 30+)**

### Setting Up Local Environment

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Samar1729/ThreeLoops_droidrush.git
   cd ThreeLoops_droidrush
   ```

2. **Configure SDK Path:**
   Create `android/local.properties`:
   ```properties
   sdk.dir=/Users/<username>/Library/Android/sdk
   ```

3. **Build the Debug APK:**
   ```bash
   cd android
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ```

5. **Install on Connected Device via ADB:**
   ```bash
   ./gradlew installDebug
   adb shell am start -n com.threeloops.mirrorarchive/.MainActivity
   ```

---

## Screen Mirroring on Mac

To view and control the app directly from your Mac:

- **Option A (Android Studio Built-in):** In Android Studio, open the **Running Devices** tab on the right side $\rightarrow$ click **`+`** $\rightarrow$ select your connected phone. (Click the gear icon $\rightarrow$ **View Mode** $\rightarrow$ **Window** to pop it out).
- **Option B (Standalone `scrcpy`):**
  ```bash
  brew install scrcpy
  scrcpy
  ```

---

## Testing

The project includes unit and integration tests covering coroutine lifecycles, hashing benchmarks, burst selection, duplicate clustering, and storage thresholds:

```bash
cd android
./gradlew testDebugUnitTest
```
*Current test suite: **61 passed, 0 failed**.*

---

## License

Developed by **Team ThreeLoops** for Droidrush 2026.
