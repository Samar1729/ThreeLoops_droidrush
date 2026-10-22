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