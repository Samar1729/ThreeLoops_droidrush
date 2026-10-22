export type AlbumType = 'RECEIPTS' | 'SCREENSHOTS' | 'DOCUMENTS' | 'FOOD' | 'PEOPLE'

export interface LabelResult {
  text: string
  confidence: number
}

export interface FaceInfo {
  id: string
  left: number // percentage 0-100
  top: number // percentage 0-100
  width: number // percentage 0-100
  height: number // percentage 0-100
  smileProb?: number
  eyesOpenProb?: number
}

export interface ImageItem {
  id: number
  mediaId: number
  uri: string
  displayName: string
  relativePath: string
  dateAdded: number
  dateTaken?: number
  sizeBytes: number
  width: number
  height: number
  blurScore: number
  faceCount: number
  labels: LabelResult[]
  faces: FaceInfo[]
  ocrText: string
  albums: AlbumType[]
  dHash: string
  pHash: string
}

export interface AlbumSummary {
  type: AlbumType
  title: string
  count: number
  coverUri: string
  description: string
  detectionRule: string
}

export type SearchMode = 'TAGS' | 'TEXT'

export interface SearchUiState {
  status: 'idle' | 'loading' | 'results' | 'empty'
  query: string
  mode: SearchMode
  results: ImageItem[]
  latencyMs: number
}

export interface DuplicateGroup {
  id: number
  imageIds: number[]
  bestId: number
  maxDistance: number
  reason: string
}

export interface MetricsSnapshot {
  imagesIndexed: number
  avgMsPerImage: number
  perStageMs: {
    decode: number
    label: number
    face: number
    ocr: number
    hash: number
    db: number
  }
  throughputImgPerSec: number
  comparisonsNaive: number
  comparisonsBucketed: number
  reclaimableMb: number
  searchLatencyMsP50: number
  peakHeapMb: number
  dbSizeMb: number
  networkPermission: string
}
