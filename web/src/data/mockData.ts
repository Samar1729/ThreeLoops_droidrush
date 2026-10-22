import type { ImageItem, AlbumSummary, DuplicateGroup, MetricsSnapshot } from '../types'
import { AlbumClassifier } from '../services/albumClassifier'

const rawItems: Omit<ImageItem, 'albums'>[] = [
  // 1. RECEIPTS
  {
    id: 1,
    mediaId: 1001,
    uri: 'https://images.unsplash.com/photo-1554415707-9e4466755490?auto=format&fit=crop&w=800&q=80',
    displayName: 'IMG_20261019_194210.jpg',
    relativePath: 'DCIM/Camera/',
    dateAdded: 1729348930,
    dateTaken: 1729348929000,
    sizeBytes: 2451920,
    width: 3024,
    height: 4032,
    blurScore: 245.8,
    faceCount: 0,
    labels: [
      { text: 'receipt', confidence: 0.98 },
      { text: 'paper', confidence: 0.95 },
      { text: 'text', confidence: 0.93 },
      { text: 'font', confidence: 0.88 }
    ],
    faces: [],
    ocrText: 'BLUE BOTTLE COFFEE ROASTERY\n142 MAIN ST, STE 100\nTAX INVOICE #89421\nDATE: 19-OCT-2026 19:42\nQTY ITEM AMOUNT\n1x CAPPUCCINO OAT $5.50\n1x CROISSANT ALMOND $4.25\nSUBTOTAL: $9.75\nGST (10%): $0.98\nTOTAL AMOUNT: $10.73\nPAID VIA APPLE PAY (CARD ****4821)\nTHANK YOU FOR VISITING!',
    dHash: '0x1A2B3C4D5E6F7A8B',
    pHash: '0x8B7A6F5E4D3C2B1A'
  },
  {
    id: 2,
    mediaId: 1002,
    uri: 'https://images.unsplash.com/photo-1556742049-0a67c5574f73?auto=format&fit=crop&w=800&q=80',
    displayName: 'Store_Receipt_Downtown.jpg',
    relativePath: 'Pictures/Receipts/',
    dateAdded: 1729262530,
    dateTaken: 1729262528000,
    sizeBytes: 1892400,
    width: 2400,
    height: 3200,
    blurScore: 198.4,
    faceCount: 0,
    labels: [
      { text: 'receipt', confidence: 0.97 },
      { text: 'invoice', confidence: 0.92 },
      { text: 'document', confidence: 0.89 },
      { text: 'bill', confidence: 0.84 }
    ],
    faces: [],
    ocrText: 'METRO MARKET ORGANIC FOODS\nSTORE #402 BILL OF SALE\nINVOICE: INV-2026-9921\nCASHIER: SARAH T.\n1x ORGANIC MILK 1L $3.99\n2x AVOCADO HASS $4.50\n1x SOURDOUGH BREAD $5.20\nSUBTOTAL $13.69\nSTATE TAX (8.25%) $1.13\nTOTAL $14.82\nREFUND POLICY: 30 DAYS WITH RECEIPT',
    dHash: '0x3C4D5E6F7A8B1A2B',
    pHash: '0x7A8B1A2B3C4D5E6F'
  },

  // 2. SCREENSHOTS
  {
    id: 3,
    mediaId: 1003,
    uri: 'https://images.unsplash.com/photo-1616469829941-c7200edec809?auto=format&fit=crop&w=800&q=80',
    displayName: 'Screenshot_20261021-143022_Settings.png',
    relativePath: 'Pictures/Screenshots/',
    dateAdded: 1729509022,
    dateTaken: 1729509022000,
    sizeBytes: 742110,
    width: 1080,
    height: 2400,
    blurScore: 420.1,
    faceCount: 0,
    labels: [
      { text: 'screenshot', confidence: 0.99 },
      { text: 'operating system', confidence: 0.94 },
      { text: 'user interface', confidence: 0.91 }
    ],
    faces: [],
    ocrText: '14:30 5G 94%\nSettings\nWi-Fi MirrorArchive_Private\nBluetooth Connected\nNotifications On\nStorage 128 GB of 256 GB used',
    dHash: '0x9F8E7D6C5B4A3928',
    pHash: '0x28394A5B6C7D8E9F'
  },
  {
    id: 4,
    mediaId: 1004,
    uri: 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=800&q=80',
    displayName: 'Screenshot_Metrics_Dashboard.png',
    relativePath: 'Pictures/Screenshots/',
    dateAdded: 1729482100,
    dateTaken: 1729482100000,
    sizeBytes: 984500,
    width: 1440,
    height: 2960,
    blurScore: 380.5,
    faceCount: 0,
    labels: [
      { text: 'screenshot', confidence: 0.98 },
      { text: 'diagram', confidence: 0.92 },
      { text: 'chart', confidence: 0.89 }
    ],
    faces: [],
    ocrText: 'ANALYTICS LIVE MONITOR\nTotal Processed: 5,420 items\nThroughput: 64 img/sec\nRAM: 84 MB allocated\nZero Network Egress Verified',
    dHash: '0x5A6B7C8D9E0F1A2B',
    pHash: '0x1A2B5A6B7C8D9E0F'
  },

  // 3. DOCUMENTS
  {
    id: 5,
    mediaId: 1005,
    uri: 'https://images.unsplash.com/photo-1450133064473-71024230f91b?auto=format&fit=crop&w=800&q=80',
    displayName: 'Contract_Agreement_Signed.jpg',
    relativePath: 'Documents/Scans/',
    dateAdded: 1729176000,
    dateTaken: 1729175900000,
    sizeBytes: 3120400,
    width: 2480,
    height: 3508,
    blurScore: 310.2,
    faceCount: 0,
    labels: [
      { text: 'document', confidence: 0.96 },
      { text: 'paper', confidence: 0.94 },
      { text: 'text', confidence: 0.93 },
      { text: 'handwriting', confidence: 0.82 }
    ],
    faces: [],
    ocrText: 'CONFIDENTIAL NON-DISCLOSURE AGREEMENT\nTHIS AGREEMENT is entered into this 18th day of October 2026 by and between ThreeLoops Inc. and Partner Labs. Both parties hereby mutually agree to protect proprietary source code, algorithms, and structural designs without transmission over external telecommunication networks or unauthorized duplication. Term shall remain in effect for 24 months from signature execution.',
    dHash: '0x4D5E6F7A8B1A2B3C',
    pHash: '0x2B3C4D5E6F7A8B1A'
  },
  {
    id: 6,
    mediaId: 1006,
    uri: 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=800&q=80',
    displayName: 'Architecture_Spec_Doc.jpg',
    relativePath: 'Documents/Specs/',
    dateAdded: 1729112000,
    dateTaken: 1729111900000,
    sizeBytes: 2854000,
    width: 2480,
    height: 3508,
    blurScore: 295.7,
    faceCount: 0,
    labels: [
      { text: 'document', confidence: 0.97 },
      { text: 'text', confidence: 0.95 },
      { text: 'page', confidence: 0.91 }
    ],
    faces: [],
    ocrText: 'ENGINEERING ARCHITECTURE MANUAL\nChapter 3: Local SQLite Encryption via SQLCipher SupportOpenHelperFactory. All database transactions must write both the image entity and the scan state checkpoint synchronously to preserve atomic state in the event of worker termination by OS low-memory kill signals.',
    dHash: '0x7F8A9B0C1D2E3F4A',
    pHash: '0x3F4A7F8A9B0C1D2E'
  },

  // 4. FOOD
  {
    id: 7,
    mediaId: 1007,
    uri: 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=800&q=80',
    displayName: 'DSC_0492_Dinner.jpg',
    relativePath: 'DCIM/Camera/',
    dateAdded: 1729443200,
    dateTaken: 1729443198000,
    sizeBytes: 4210900,
    width: 4000,
    height: 3000,
    blurScore: 312.0,
    faceCount: 0,
    labels: [
      { text: 'food', confidence: 0.99 },
      { text: 'dish', confidence: 0.96 },
      { text: 'cuisine', confidence: 0.94 },
      { text: 'pasta', confidence: 0.91 },
      { text: 'meal', confidence: 0.89 }
    ],
    faces: [],
    ocrText: 'TRATTORIA BELLA NOTTE\nMENU DELLA SERA',
    dHash: '0x8899AABBCCDDEEFF',
    pHash: '0xFFEEDDCCBBAA9988'
  },
  {
    id: 8,
    mediaId: 1008,
    uri: 'https://images.unsplash.com/photo-1509722747041-616f39b57569?auto=format&fit=crop&w=800&q=80',
    displayName: 'Morning_Espresso_Art.jpg',
    relativePath: 'DCIM/Camera/',
    dateAdded: 1729321000,
    dateTaken: 1729320980000,
    sizeBytes: 3105000,
    width: 3024,
    height: 4032,
    blurScore: 350.4,
    faceCount: 0,
    labels: [
      { text: 'coffee', confidence: 0.99 },
      { text: 'latte', confidence: 0.95 },
      { text: 'drink', confidence: 0.93 },
      { text: 'food', confidence: 0.91 }
    ],
    faces: [],
    ocrText: 'ARTISAN COFFEE\nROASTED FRESH',
    dHash: '0x1234567890ABCDEF',
    pHash: '0xFEDCBA0987654321'
  },
  {
    id: 9,
    mediaId: 1009,
    uri: 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=800&q=80',
    displayName: 'Burger_Craft_Studio.jpg',
    relativePath: 'DCIM/Camera/',
    dateAdded: 1729294000,
    dateTaken: 1729293980000,
    sizeBytes: 3680200,
    width: 3840,
    height: 2160,
    blurScore: 288.9,
    faceCount: 0,
    labels: [
      { text: 'food', confidence: 0.98 },
      { text: 'burger', confidence: 0.96 },
      { text: 'fast food', confidence: 0.92 },
      { text: 'dish', confidence: 0.90 }
    ],
    faces: [],
    ocrText: '',
    dHash: '0xABCDEF1234567890',
    pHash: '0x9087654321ABCDEF'
  },

  // 5. PEOPLE
  {
    id: 10,
    mediaId: 1010,
    uri: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80',
    displayName: 'Portrait_Evelyn_Studio.jpg',
    relativePath: 'DCIM/Portraits/',
    dateAdded: 1729490000,
    dateTaken: 1729489990000,
    sizeBytes: 4890000,
    width: 3456,
    height: 5184,
    blurScore: 412.3,
    faceCount: 1,
    labels: [
      { text: 'person', confidence: 0.99 },
      { text: 'face', confidence: 0.98 },
      { text: 'portrait', confidence: 0.95 },
      { text: 'smile', confidence: 0.92 }
    ],
    faces: [
      {
        id: 'face-10-1',
        left: 28,
        top: 18,
        width: 44,
        height: 48,
        smileProb: 0.94,
        eyesOpenProb: 0.98
      }
    ],
    ocrText: '',
    dHash: '0xEEFFAA1122334455',
    pHash: '0x5544332211AAFFEE'
  },
  {
    id: 11,
    mediaId: 1011,
    uri: 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80',
    displayName: 'Street_Portrait_Marcus.jpg',
    relativePath: 'DCIM/Portraits/',
    dateAdded: 1729412000,
    dateTaken: 1729411980000,
    sizeBytes: 3950000,
    width: 3200,
    height: 4800,
    blurScore: 365.1,
    faceCount: 1,
    labels: [
      { text: 'person', confidence: 0.99 },
      { text: 'human', confidence: 0.96 },
      { text: 'man', confidence: 0.94 },
      { text: 'fashion', confidence: 0.88 }
    ],
    faces: [
      {
        id: 'face-11-1',
        left: 32,
        top: 22,
        width: 38,
        height: 42,
        smileProb: 0.81,
        eyesOpenProb: 0.96
      }
    ],
    ocrText: '',
    dHash: '0x3344556677889900',
    pHash: '0x0099887766554433'
  },
  {
    id: 12,
    mediaId: 1012,
    uri: 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=800&q=80',
    displayName: 'Friends_Outdoor_Gathering.jpg',
    relativePath: 'DCIM/Camera/',
    dateAdded: 1729350000,
    dateTaken: 1729349980000,
    sizeBytes: 5240000,
    width: 4200,
    height: 2800,
    blurScore: 298.6,
    faceCount: 4,
    labels: [
      { text: 'person', confidence: 0.99 },
      { text: 'crowd', confidence: 0.95 },
      { text: 'friendship', confidence: 0.93 },
      { text: 'smile', confidence: 0.90 }
    ],
    faces: [
      { id: 'f-1', left: 16, top: 28, width: 18, height: 26, smileProb: 0.92, eyesOpenProb: 0.95 },
      { id: 'f-2', left: 38, top: 25, width: 19, height: 28, smileProb: 0.88, eyesOpenProb: 0.97 },
      { id: 'f-3', left: 62, top: 29, width: 17, height: 25, smileProb: 0.95, eyesOpenProb: 0.94 },
      { id: 'f-4', left: 81, top: 32, width: 16, height: 24, smileProb: 0.79, eyesOpenProb: 0.91 }
    ],
    ocrText: '',
    dHash: '0x4455667788990011',
    pHash: '0x1100998877665544'
  },

  // 6. BURST / DUPLICATES (For Samar's Best-Shot & Aditya's Face Quality Scoring)
  {
    id: 13,
    mediaId: 1013,
    uri: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80',
    displayName: 'BURST_20261021_001.jpg',
    relativePath: 'DCIM/Camera/',
    dateAdded: 1729500001,
    dateTaken: 1729500001200,
    sizeBytes: 4120000,
    width: 3000,
    height: 4000,
    blurScore: 112.4, // Slight blur
    faceCount: 1,
    labels: [
      { text: 'person', confidence: 0.99 },
      { text: 'woman', confidence: 0.95 },
      { text: 'portrait', confidence: 0.92 }
    ],
    faces: [
      { id: 'f-13', left: 30, top: 20, width: 40, height: 45, smileProb: 0.45, eyesOpenProb: 0.60 }
    ],
    ocrText: '',
    dHash: '0x1122334455667788',
    pHash: '0x8877665544332211'
  },
  {
    id: 14,
    mediaId: 1014,
    uri: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80',
    displayName: 'BURST_20261021_002_BEST.jpg',
    relativePath: 'DCIM/Camera/',
    dateAdded: 1729500002,
    dateTaken: 1729500002400,
    sizeBytes: 4350000,
    width: 3000,
    height: 4000,
    blurScore: 388.9, // Sharpest!
    faceCount: 1,
    labels: [
      { text: 'person', confidence: 0.99 },
      { text: 'woman', confidence: 0.95 },
      { text: 'portrait', confidence: 0.92 },
      { text: 'smile', confidence: 0.94 }
    ],
    faces: [
      { id: 'f-14', left: 30, top: 20, width: 40, height: 45, smileProb: 0.96, eyesOpenProb: 0.99 } // High smile + open eyes
    ],
    ocrText: '',
    dHash: '0x1122334455667788',
    pHash: '0x8877665544332212' // Hamming dist = 1
  },
  {
    id: 15,
    mediaId: 1015,
    uri: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80',
    displayName: 'BURST_20261021_003_BLURRY.jpg',
    relativePath: 'DCIM/Camera/',
    dateAdded: 1729500003,
    dateTaken: 1729500003600,
    sizeBytes: 3980000,
    width: 3000,
    height: 4000,
    blurScore: 68.2, // Very blurry
    faceCount: 1,
    labels: [
      { text: 'person', confidence: 0.98 },
      { text: 'woman', confidence: 0.92 }
    ],
    faces: [
      { id: 'f-15', left: 30, top: 20, width: 40, height: 45, smileProb: 0.70, eyesOpenProb: 0.20 } // Closed eyes
    ],
    ocrText: '',
    dHash: '0x1122334455667789',
    pHash: '0x8877665544332213'
  },
  // 7. MULTI-ALBUM: Receipt Screenshot (tests multi-album classification from Section 3.8!)
  {
    id: 16,
    mediaId: 1016,
    uri: 'https://images.unsplash.com/photo-1580048915913-4f8f5cb481c4?auto=format&fit=crop&w=800&q=80',
    displayName: 'Screenshot_Uber_Receipt_Trip.png',
    relativePath: 'Pictures/Screenshots/',
    dateAdded: 1729465000,
    dateTaken: 1729464990000,
    sizeBytes: 1120000,
    width: 1170,
    height: 2532,
    blurScore: 395.2,
    faceCount: 0,
    labels: [
      { text: 'screenshot', confidence: 0.99 },
      { text: 'receipt', confidence: 0.96 },
      { text: 'document', confidence: 0.91 },
      { text: 'text', confidence: 0.94 }
    ],
    faces: [],
    ocrText: 'Uber Technologies Inc.\nTRIP RECEIPT #UB-78214\nTotal Amount: $28.40\nSubtotal: $24.00\nCity Tax & Surcharge: $4.40\nPayment Method: Apple Card\nDriver: David K. (Toyota Camry)',
    dHash: '0x778899AABBCCDDEE',
    pHash: '0xEEDDCCBBAA998877'
  }
]

// Dynamically run Aditya's AlbumClassifier across all images to ensure 100% contract compliance!
export const INITIAL_IMAGES: ImageItem[] = rawItems.map(item => {
  const classifiedAlbums = AlbumClassifier.classify(
    item.labels,
    `${item.relativePath}${item.displayName}`,
    item.ocrText,
    item.faceCount
  )
  return {
    ...item,
    albums: Array.from(classifiedAlbums)
  }
})

export const ALBUM_SUMMARIES: AlbumSummary[] = [
  {
    type: 'RECEIPTS',
    title: 'Receipts',
    count: INITIAL_IMAGES.filter(img => img.albums.includes('RECEIPTS')).length,
    coverUri: 'https://images.unsplash.com/photo-1554415707-9e4466755490?auto=format&fit=crop&w=800&q=80',
    description: 'Financial tokens, invoices, bills, and tax summaries',
    detectionRule: 'Text-like labels AND 2+ financial tokens (total, subtotal, invoice, gst, tax, etc.)'
  },
  {
    type: 'SCREENSHOTS',
    title: 'Screenshots',
    count: INITIAL_IMAGES.filter(img => img.albums.includes('SCREENSHOTS')).length,
    coverUri: 'https://images.unsplash.com/photo-1616469829941-c7200edec809?auto=format&fit=crop&w=800&q=80',
    description: 'Screen captures and interface recordings',
    detectionRule: 'relativePath contains "Screenshots" OR displayName starts with "Screenshot_"'
  },
  {
    type: 'DOCUMENTS',
    title: 'Documents',
    count: INITIAL_IMAGES.filter(img => img.albums.includes('DOCUMENTS')).length,
    coverUri: 'https://images.unsplash.com/photo-1450133064473-71024230f91b?auto=format&fit=crop&w=800&q=80',
    description: 'Signed agreements, contracts, technical specifications',
    detectionRule: 'Text-like labels AND OCR length >= 80 characters AND not Receipts'
  },
  {
    type: 'FOOD',
    title: 'Food & Dining',
    count: INITIAL_IMAGES.filter(img => img.albums.includes('FOOD')).length,
    coverUri: 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=800&q=80',
    description: 'Dishes, beverages, culinary art, and dining',
    detectionRule: 'ML Kit label in [food, dish, cuisine, meal, fruit, vegetable, dessert, coffee...]'
  },
  {
    type: 'PEOPLE',
    title: 'People & Faces',
    count: INITIAL_IMAGES.filter(img => img.albums.includes('PEOPLE')).length,
    coverUri: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80',
    description: 'Portraits, group photos, and detected human faces',
    detectionRule: 'ML Kit faceCount > 0 OR label in [person, face, human, smile, portrait...]'
  }
]

export const MOCK_DUPLICATE_GROUPS: DuplicateGroup[] = [
  {
    id: 1,
    imageIds: [13, 14, 15],
    bestId: 14, // BURST_20261021_002_BEST.jpg has highest blurScore + smile + eyes open!
    maxDistance: 2,
    reason: 'Burst sequence (3 shots in 2.4s) • Hamming Distance: 1 bit'
  }
]

export const MOCK_METRICS: MetricsSnapshot = {
  imagesIndexed: 5420,
  avgMsPerImage: 184,
  perStageMs: {
    decode: 22,
    label: 48,
    face: 36,
    ocr: 58,
    hash: 8,
    db: 12
  },
  throughputImgPerSec: 68.4,
  comparisonsNaive: 14685790,
  comparisonsBucketed: 1284,
  reclaimableMb: 382.4,
  searchLatencyMsP50: 16.2,
  peakHeapMb: 82.5,
  dbSizeMb: 14.8,
  networkPermission: 'NONE (0 Internet Access)'
}
