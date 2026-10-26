# Darpan (दर्पण) / Vault: Comprehensive Architecture & Implementation Plan

> **Role**: Staff Android & On-Device ML Architect (Aditya — Member B: Brain & Presentation)  
> **Target Problem**: Problem 06 — Mirror Archive / Darpan (100% Offline AI Photo Archive)  
> **Team ThreeLoops**: Aditya Singh (UI/ML), Rishav Dev (Engine/Storage), Samar Anand (Twin-Finder/Hasher)  
> **Core Constraint**: `android.permission.INTERNET` is strictly forbidden. 100% On-Device ML.

---

## Executive Summary

Darpan (दर्पण) is a consumer-grade, zero-cloud on-device intelligent photo archive. All media indexing, label detection, facial landmark extraction, Latin text OCR, and duplicate detection execute locally without transmitting a single byte over the network. 

This document defines the complete architectural blueprint for **Member B (Aditya)**'s scope, integrating the backend engine completed up to **R-7** by Rishav, adopting a **Spotify-inspired dark glassmorphism design system**, implementing **Core ML Modules A-1 to A-8**, and delivering **4 Mentor-Differentiating Features** to win the room during final judging.

---

## 1. Design System: Spotify-Sleek Dark Glassmorphism

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Dynamic Ambient Mesh Glow                       │
│             (Radial 140px blur extracted from active album/photo)      │
├────────────────────────────────────────────────────────────────────────┤
│  Top Bar: [Logo / Title]  [Status Badges: 0 INTERNET]  [Niti Sandbox] │
├────────────────────────────────────────────────────────────────────────┤
│                                                │                       │
│                                                │  Right Slide-Over     │
│             Fluid Bento/Masonry Grid           │  Glass Drawer         │
│          - Edge-to-edge photo cards            │  "Drishti X-Ray"      │
│          - Instant pill filters at top         │  - Live ML Telemetry  │
│          - Smooth hover / tap zoom             │  - Face Landmark Box  │
│                                                │  - Pipeline Gate      │
│                                                │    Trace (Decode->OCR)│
│                                                │                       │
├────────────────────────────────────────────────────────────────────────┤
│      Bottom Floating "Now-Indexing" Player Bar (done/total · img/s)    │
├────────────────────────────────────────────────────────────────────────┤
│     Frosted Navigation Dock (Home · Albums · Search · People · Raseed) │
└────────────────────────────────────────────────────────────────────────┘
```

### 1.1 Strict Philosophy: Zero "AI Slop"
- **No generic purple/cyan neon AI gradients**.
- **No cheesy robot icons or AI buzzword text**.
- **No harsh drop shadows or oversaturated cards**.
- Pure, refined consumer-grade experience inspired by Apple and Spotify.

### 1.2 Surface Elevation & Color Tokens
- **Canvas Base**: Obsidian Pitch (`#09090B` to `#121212`).
- **Glass Surfaces**: Frosted translucent charcoal (`rgba(24, 24, 27, 0.65)` / `0x9918181B`) with hardware-accelerated background blur.
- **Specular Refraction**: 1px linear gradient border (`Color(0x1AFFFFFF)` to `Color(0x05FFFFFF)`) on the top and left edges.
- **Dynamic Ambient Mesh (`AmbientMesh`)**: Extracts the dominant warm color from the active album cover or selected photo; renders a subtle radial mesh glow (`140.dp` blur at `12%` opacity) behind headers.
- **Typography & Accents**:
  - Primary text: `#FAFAFA` (Crisp White).
  - Secondary metadata: `#A1A1AA` (Muted Zinc).
  - Accents: Emerald (`#10B981`) for verified privacy & best-shot; Amber (`#F59E0B`) for OCR match highlights.
  - Telemetry: Monospace (`FontFamily.Monospace`) strictly for latency badges (`24ms`), bounding box coords `[x, y, w, h]`, and GST currency figures (`₹1,420.00`).

---

## 2. Frozen R-7 Backend Integration

The UI layer strictly consumes the existing R-7 backend contracts without modifying their signatures:

```kotlin
// Data Contracts
data class ImageItem(
    val id: Long,
    val mediaId: Long,
    val uri: String,
    val dateAdded: Long,
    val dateTaken: Long?,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val blurScore: Double,
    val faceCount: Int
)

data class AlbumSummary(
    val type: AlbumType,
    val count: Int,
    val coverUri: String?
)

data class ScanProgress(
    val phase: ScanPhase,
    val done: Int,
    val total: Int
)

// Injected Repository Flows
interface ImageRepository {
    fun searchByTag(q: String): Flow<List<ImageItem>>
    fun searchByText(q: String): Flow<List<ImageItem>>
    fun album(type: AlbumType): Flow<List<ImageItem>>
    fun albumSummaries(): Flow<List<AlbumSummary>>
    fun suggestions(prefix: String): Flow<List<String>>
    fun peopleImages(): Flow<List<ImageItem>>
    fun scanState(): Flow<ScanState>
    fun facesFor(imageId: Long): List<FaceInfo>
    fun lowBlur(threshold: Double): Flow<List<ImageItem>>
    fun duplicateCandidates(): List<HashRow>
}

// WorkManager Scan Controller
class ScanController {
    fun progress(): Flow<ScanProgress>
    fun start(ignoreConstraints: Boolean = false)
    fun cancel()
    fun rescan(full: Boolean)
}
```

---

## 3. Core ML Modules (Aditya A-1 to A-8)

### 3.1 A-1: `ImageAnalyzer` (Section 3.7)
- **Implementation**: `com.threeloops.mirrorarchive.ml.MlKitImageAnalyzer`
- **Labeling**: Bundled ML Kit Image Labeling model (`ImageLabelerOptions.DEFAULT_OPTIONS`), confidence threshold `0.60f`, lowercased and deduplicated.
- **Faces**: `FaceDetectorOptions` with `PERFORMANCE_MODE_FAST`, `LANDMARK_MODE_ALL`, `CLASSIFICATION_MODE_ALL` extracting `smileProb` and `eyesOpenProb`.
- **Text Recognition**: ML Kit Text Recognition v2 (Latin).
- **Gatekeeper Optimization (`isTextLike`)**:
  - OCR only runs on 1024px bitmaps when labels intersect text-like triggers (`text`, `paper`, `document`, `receipt`, `poster`, `menu`, `screen`, `handwriting`, `ticket`, `invoice`).
  - Avoids expensive OCR on landscape/nature/portrait images, cutting batch pipeline time by ~70%.
- **Lifecycle & Warm-up**: Pre-warms ML Kit models at startup inside `MirrorArchiveApp.onCreate()` without blocking UI.

### 3.2 A-2: `AlbumClassifier` & `AlbumRules` (Section 3.8)
- **Implementation**: `com.threeloops.mirrorarchive.albums.AlbumClassifier`
- **Deterministic 3-Signal Classification**:
  - `SCREENSHOTS`: `relativePath.contains("Screenshots", true)` OR `displayName.startsWith("Screenshot_", true)`.
  - `RECEIPTS`: `isTextLike(labels)` AND `ocrText` hits `2+` keywords from `["total", "subtotal", "invoice", "gst", "gstin", "tax", "amount", "bill", "receipt", "qty", "upi", "card"]`.
  - `DOCUMENTS`: `isTextLike(labels)` AND `ocrText.length > 80` AND NOT `RECEIPTS`.
  - `FOOD`: `labels` intersect `["food", "dish", "cuisine", "meal", "fruit", "vegetable", "dessert", "beverage", "coffee", "pizza", "burger"]`.
  - `PEOPLE`: `faceCount > 0` OR `labels` intersect `["person", "face", "smile", "selfie", "portrait"]`.
- **Explainability Engine (`explain`)**:
  - Generates audit trail strings: `"RECEIPTS: OCR matched [gstin, total, amount] + label 'paper' (0.89)"`.
- **Synonym Expansion (`AlbumRules.synonyms`)**:
  - Provides rich mappings (`"dog" -> ["puppy", "canine", "pet"]`, `"bill" -> ["receipt", "invoice", "tax"]`, `"car" -> ["automobile", "vehicle"]`).

### 3.3 A-3 & A-5: `SearchViewModel` (Section 3.9)
- **Implementation**: `com.threeloops.mirrorarchive.search.SearchViewModel`
- **Features**:
  - Debounced reactive flow (`250ms`) with `distinctUntilChanged().flatMapLatest`.
  - Segmented pill toggle: `SearchMode.TAGS` vs `SearchMode.TEXT` (Runic OCR).
  - Multi-word `AND` tokenization across labels and OCR.
  - `highlightRanges(text, query)`: Computes sub-string start/end ranges for highlighting OCR matches in yellow/emerald frosted pills.
  - `RecentSearches`: Persists the last 10 successful search queries.

### 3.4 A-4 & A-8: `PeopleViewModel` & `FaceQuality` (Section 3.10)
- **Implementation**: `com.threeloops.mirrorarchive.people.PeopleViewModel`
- **Features**:
  - `faceQuality(imageId: Long): Float`: Biometric score combining `0.6f * eyesOpenProb + 0.4f * smileProb` normalized to `0..1`.
  - `PersonClusterer.cluster(embeddings)`: Greedy cosine-similarity clustering with an interactive UI threshold slider (`0.70` to `0.95`).

### 3.5 A-7: `SearchTimer` (Section 3.19)
- **Implementation**: `com.threeloops.mirrorarchive.search.SearchTimer`
- **Features**:
  - Wraps query execution to measure P50 and latest search latency in milliseconds (`ms`).
  - Surfaces latency badges in the search bar and the Performance Benchmark card.

---

## 4. Four Mentor-Differentiating Features ("WOW" Factor)

```
┌────────────────────────────────────────────────────────────────────────┐
│                        4 MENTOR-DIFFERENTIATING FEATURES               │
├────────────────────────┬───────────────────────────────────────────────┤
│ 1. Drishti X-Ray       │ Explainable AI slide-over drawer with live   │
│    (Inspect ML Inside) │ bounding boxes, smile %, & pipeline trace.   │
├────────────────────────┼───────────────────────────────────────────────┤
│ 2. Raseed Ledger       │ 100% Offline FinTech receipt parser: GSTIN,   │
│    (Offline FinTech)   │ Merchant, Date, Total ₹ Spent, CSV export.    │
├────────────────────────┼───────────────────────────────────────────────┤
│ 3. Pravah Smart Stacks │ Burst photo selector with composite quality:  │
│    (Burst Best-Shot)   │ 0.55*Sharpness + 0.45*FaceQuality + Purge.   │
├────────────────────────┼───────────────────────────────────────────────┤
│ 4. Niti Sandbox        │ Live judge playground to adjust confidence    │
│    (Live Model Tuning) │ thresholds and test classification rules.     │
└────────────────────────┴───────────────────────────────────────────────┘
```

### Feature A: "Drishti X-Ray" — Explainable AI Right Drawer
- **Trigger**: Tap any photo in the grid.
- **Components**:
  - Sliding frosted drawer with high-resolution preview.
  - **ML X-Ray Overlay Toggle**: Visualizes exact face bounding boxes with floating metric pills (`Eyes Open: 98%`, `Smile: 84%`) and OCR text boxes.
  - **Live Pipeline Trace**:
    $$\text{Decode (3ms)} \longrightarrow \text{ML Kit Labels (14ms)} \longrightarrow \text{Gate: isTextLike} \longrightarrow \text{OCR (38ms)} \longrightarrow \text{Album: RECEIPTS}$$
  - Shows `AlbumClassifier.explain()` output directly so judges see the logic behind categorization.

### Feature B: "Raseed Ledger" — Zero-Cloud GST & Expense Extractor
- **Trigger**: 5th tab on the navigation dock (`Raseed Ledger`).
- **Engine**:
  - Regex parser running over `ocrText` of images classified under `RECEIPTS`.
  - Extracts:
    - **Merchant Name**: First non-numeric header lines.
    - **Date**: Formats matching `dd/mm/yyyy`, `dd-mm-yyyy`, `dd MMM yyyy`.
    - **GSTIN**: Matches standard 15-character GST pattern `\d{2}[A-Z]{5}\d{4}[A-Z]{1}[A-Z\d]{1}[Z]{1}[A-Z\d]{1}`.
    - **Total Amount**: Numeric values preceded by `Total`, `Grand Total`, `Amount Paid`, `Net Payable`, or `₹`.
- **UI**:
  - Spotify-Wrapped style monthly expense card.
  - Grouped transactions with merchant icons, dates, and amounts.
  - 1-click **Export CSV / JSON** (saved locally to Downloads without network access).

### Feature C: "Pravah Smart Stacks" — Interactive Burst Best-Shot Arena
- **Trigger**: Integrated within the `People & Bursts` view.
- **Engine**:
  - Groups images taken within 3 seconds having near-identical perceptual hashes (`hamming(dHash) <= 10`).
  - Evaluates composite score:
    $$\text{Score} = 0.55 \times \text{Sharpness}(\text{blurScore}) + 0.45 \times \text{FaceQuality}(\text{eyesOpen}, \text{smile})$$
- **UI**:
  - Overlapping glass card stack.
  - Crowned "Best Shot" with an emerald badge (`Score: 94.2`).
  - Rejection reasons on runner-up shots (`"Eyes closed: 0.12"` or `"Motion blur: 44.1"`).
  - 1-click **"Keep Best & Purge Rest"** calling `MediaStore.createDeleteRequest`.

### Feature D: "Niti Sandbox" — Live Synonym & Confidence Tuning Drawer
- **Trigger**: Top header action icon (`Niti Sandbox`).
- **Engine**:
  - Interactive testbed allowing mentors/judges to drag confidence cutoff sliders (`0.40` to `0.90`) and enter test queries or raw OCR text.
  - Triggers live re-evaluation of `AlbumClassifier.classify()` and `explain()` in memory without database mutation.
  - Proves deterministic predictability of the on-device rules engine to examiners.

---

## 5. Navigation Shell & Component Hierarchy

```
MirrorArchiveApp (Single Activity + Navigation Compose)
│
├── AuthScreen (PIN & Biometric Lock)
│
└── MainNavigationApp (Scaffold + Glass Ambient Mesh)
    │
    ├── Frosted Bottom Dock (5 Destinations)
    │   ├── 1. Home Grid (Masonry Bento Photo Grid + Pill Filters)
    │   ├── 2. Smart Albums (Receipts, Screenshots, Documents, Food, People)
    │   ├── 3. Runic Search (Tags vs OCR Text + Latency Timer)
    │   ├── 4. People & Best-Shot (Portrait Clusters + Pravah Burst Arena)
    │   └── 5. Raseed Ledger (FinTech Expense Extractor on Receipts)
    │
    ├── Bottom Floating "Now-Indexing" Player Bar
    │   └── Live ScanProgress (done / total · phase · throughput · INTERNET: NONE)
    │
    ├── Right Slide-Over Glass Drawer: Drishti X-Ray (Explainable AI)
    │
    └── Top Action Drawer: Niti Sandbox (Rule & Confidence Tuning)
```

---

## 6. Implementation Stages & Verification Checklist

| Stage | Target Files | Key Deliverables | Verification Metric |
|---|---|---|---|
| **Phase 1** | `ml/`, `albums/`, `search/`, `people/` | Core ML logic: `MlKitImageAnalyzer`, `AlbumClassifier`, `SearchViewModel`, `PeopleViewModel`. | Unit tests pass for classification, synonyms, gatekeeper, and biometric scoring. |
| **Phase 2** | `ui/theme/`, `ui/navigation/` | Spotify-sleek glass design system: `Glassmorphism.kt`, `AmbientMesh.kt`, `NowIndexingBar.kt`, `NavigationDock.kt`. | 60fps Compose scroll performance; zero layout jank; verified dark glass aesthetics. |
| **Phase 3** | `ui/albums/`, `ui/search/`, `ui/people/` | Primary screen refactors consuming R-7 Room repository flows with debounced search and bento grid. | Search returns in `< 150ms`; albums populate dynamically from Room DB. |
| **Phase 4** | `ui/xray/`, `ui/ledger/`, `ui/burst/`, `ui/sandbox/` | 4 Mentor Differentiators: Drishti X-Ray, Raseed Ledger, Pravah Smart Stacks, Niti Sandbox. | Face bounding boxes render accurately; GST parser extracts invoice totals; burst crowns best shot. |
| **Phase 5** | `app/`, `AndroidManifest.xml` | Offline hardening, crash-prevention ImageLoader interceptor, emulator/device demo dry-run. | Manifest strictly has zero `INTERNET` permission; zero crashes during demo run. |

---

## 7. Judge Demo Script (3-Minute Winning Walkthrough)

- **0:00 - 0:25 (The Privacy Hook)**: Open app, show zero network permission in Settings, show live biometric unlock.
- **0:25 - 0:55 (Now-Indexing Bar & Throughput)**: Point to bottom floating player showing live `IndexWorker` processing `50 img/s` completely on-device.
- **0:55 - 1:30 (Smart Albums & Runic Search)**: Search for a word found inside a receipt (`"GST"` or `"Coffee"`), demonstrate `< 25ms` search latency and highlight pills.
- **1:30 - 2:05 (Drishti X-Ray & Niti Sandbox)**: Click photo, open Drishti X-Ray, show live face bounding boxes, smile probability, and the deterministic pipeline gate trace. Open Niti Sandbox to tune confidence live.
- **2:05 - 2:40 (Raseed Ledger FinTech Layer)**: Switch to Raseed tab, show automatic extraction of merchant, date, and ₹ totals from raw receipt images.
- **2:40 - 3:00 (Pravah Smart Stacks)**: Show burst grouping, demonstrate composite best-shot selection (`0.55*Sharpness + 0.45*FaceQuality`), and trigger 1-click purge.
