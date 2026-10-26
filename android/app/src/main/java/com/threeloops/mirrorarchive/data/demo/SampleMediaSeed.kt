package com.threeloops.mirrorarchive.data.demo

import android.graphics.Rect
import android.graphics.RectF
import com.threeloops.mirrorarchive.R
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.LabelResult

data class DisplayPhoto(
    val id: Long,
    val imageUrl: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val ocrSnippet: String?,
    val faceBox: RectF?,
    val score: Float,
    val drawableRes: Int = 0,
    val category: String = "",
    val albumType: AlbumType = AlbumType.SCREENSHOTS,
    val gstin: String? = null,
    val totalAmount: String? = null,
    val rawOcrText: String? = null,
    val smileProb: Float? = null,
    val eyesOpenProb: Float? = null,
    val blurScore: Double = 100.0,
    val isWinner: Boolean = false,
    val rejectionReason: String? = null,
    val personClusterId: Int? = null,
    val labels: List<LabelResult> = emptyList(),
    val faces: List<FaceInfo> = emptyList()
) {
    val ocrText: String? get() = rawOcrText
    val primaryLabel: String get() = tag.substringBefore(" ")
    val primaryConfidence: Float get() = score
    val dateFormatted: String get() = subtitle
    val faceQuality: Float get() = ((eyesOpenProb ?: 0.5f) * 0.5f + (smileProb ?: 0.5f) * 0.5f)
    val burstGroupId: Int? get() = if (category == "BURSTS") 101 else null
    val merchant: String? get() = if (category == "RECEIPTS") title else null
    val totalDouble: Double? get() = totalAmount?.replace("₹", "")?.replace(",", "")?.trim()?.toDoubleOrNull()
}

typealias DemoPhoto = DisplayPhoto

object SampleMediaSeed {

    val photos: List<DisplayPhoto> = listOf(
        // =========================================================================
        // 4 REAL RECEIPTS / INVOICES
        // =========================================================================
        DisplayPhoto(
            id = 1L,
            imageUrl = "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?w=700&q=80",
            drawableRes = R.drawable.sample_receipt_starbucks,
            title = "Starbucks Coffee India",
            subtitle = "24 Oct 2026 • Indiranagar",
            tag = "receipt 96%",
            ocrSnippet = "TOTAL: ₹ 472.50 | GSTIN: 29AAACB2184A1Z5",
            faceBox = null,
            score = 0.96f,
            category = "RECEIPTS",
            albumType = AlbumType.RECEIPTS,
            gstin = "29AAACB2184A1Z5",
            totalAmount = "₹ 472.50",
            rawOcrText = """
                STARBUCKS COFFEE INDIA
                STORE #4812, 100 FT ROAD, INDIRANAGAR
                GSTIN: 29AAACB2184A1Z5
                DATE: 24/10/2026  TIME: 09:42 AM
                ----------------------------------------
                FLAT WHITE TALL           280.00
                BUTTER CROISSANT          170.00
                ----------------------------------------
                SUBTOTAL:                 450.00
                CGST 2.5%:                 11.25
                SGST 2.5%:                 11.25
                ----------------------------------------
                TOTAL PAYABLE:          ₹ 472.50
                MODE: UPI / PHONEPE
            """.trimIndent(),
            labels = listOf(
                LabelResult("receipt", 0.96f),
                LabelResult("paper", 0.94f),
                LabelResult("coffee", 0.89f),
                LabelResult("text", 0.88f)
            ),
            blurScore = 142.5
        ),

        DisplayPhoto(
            id = 2L,
            imageUrl = "https://images.unsplash.com/photo-1580048915913-4f8f5cb481c4?w=700&q=80",
            drawableRes = R.drawable.sample_receipt_shell,
            title = "Shell Highway Fuel Station",
            subtitle = "22 Oct 2026 • NH-44 Toll Plaza",
            tag = "receipt 95%",
            ocrSnippet = "TOTAL: ₹ 2,500.00 | GSTIN: 27AAECS1490F1ZM",
            faceBox = null,
            score = 0.95f,
            category = "RECEIPTS",
            albumType = AlbumType.RECEIPTS,
            gstin = "27AAECS1490F1ZM",
            totalAmount = "₹ 2,500.00",
            rawOcrText = """
                SHELL INDIA MARKETS PVT LTD
                RETAIL OUTLET #109, NH-44 HIGHWAY
                GSTIN: 27AAECS1490F1ZM
                DATE: 22/10/2026  TIME: 18:15 PM
                PUMP: 04   NOZZLE: 02
                PRODUCT: POWER PETROL / FUEL
                RATE: ₹ 104.16 / LTR
                VOLUME: 24.00 LTR
                ----------------------------------------
                TOTAL AMOUNT:         ₹ 2,500.00
                TAX INVOICE NUMBER: SL-88192
            """.trimIndent(),
            labels = listOf(
                LabelResult("receipt", 0.95f),
                LabelResult("paper", 0.92f),
                LabelResult("document", 0.89f),
                LabelResult("text", 0.87f)
            ),
            blurScore = 128.0
        ),

        DisplayPhoto(
            id = 3L,
            imageUrl = "https://images.unsplash.com/photo-1583258292688-d0213dc5a3a8?w=700&q=80",
            drawableRes = R.drawable.sample_receipt_grocery,
            title = "Reliance Fresh Supermarket",
            subtitle = "19 Oct 2026 • Koramangala",
            tag = "invoice 93%",
            ocrSnippet = "TOTAL: ₹ 1,820.50 | GSTIN: 27AAACR7194N1ZX",
            faceBox = null,
            score = 0.93f,
            category = "RECEIPTS",
            albumType = AlbumType.RECEIPTS,
            gstin = "27AAACR7194N1ZX",
            totalAmount = "₹ 1,820.50",
            rawOcrText = """
                RELIANCE RETAIL LIMITED
                SMART BAZAAR, KORAMANGALA 5TH BLOCK
                GSTIN: 27AAACR7194N1ZX
                DATE: 19/10/2026  CASHIER: PRIYA
                ----------------------------------------
                ORGANIC ALMONDS 500G      480.00
                EXTRA VIRGIN OLIVE OIL    650.00
                FARM EGGS (PACK OF 12)    110.00
                ROASTED COFFEE BEANS      420.00
                ORGANIC BROWN BREAD        60.50
                ----------------------------------------
                NET AMOUNT:            ₹ 1,720.50
                GST TAX @ 5%:             ₹ 100.00
                TOTAL:                 ₹ 1,820.50
            """.trimIndent(),
            labels = listOf(
                LabelResult("invoice", 0.93f),
                LabelResult("receipt", 0.91f),
                LabelResult("groceries", 0.87f),
                LabelResult("paper", 0.85f)
            ),
            blurScore = 115.2
        ),

        DisplayPhoto(
            id = 4L,
            imageUrl = "https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=700&q=80",
            drawableRes = R.drawable.sample_receipt_pharmacy,
            title = "Apollo Pharmacy Ltd",
            subtitle = "15 Oct 2026 • MG Road",
            tag = "receipt 92%",
            ocrSnippet = "TOTAL: ₹ 640.00 | GSTIN: 33AAACA0890B1Z8",
            faceBox = null,
            score = 0.92f,
            category = "RECEIPTS",
            albumType = AlbumType.RECEIPTS,
            gstin = "33AAACA0890B1Z8",
            totalAmount = "₹ 640.00",
            rawOcrText = """
                APOLLO PHARMACY LIMITED
                BRANCH 204, MG ROAD, BENGALURU
                GSTIN: 33AAACA0890B1Z8
                DL NO: KA-B2-19284 / KA-B2-19285
                DATE: 15/10/2026  TIME: 14:22 PM
                ----------------------------------------
                AMITRIPTYLINE 10MG TAB     180.00
                VITAMIN D3 60K CAPSULES    320.00
                BAND-AID PACK OF 10        140.00
                ----------------------------------------
                TOTAL INVOICE VALUE:     ₹ 640.00
                DOCTOR: DR. RAMESH RAO
            """.trimIndent(),
            labels = listOf(
                LabelResult("receipt", 0.92f),
                LabelResult("prescription", 0.89f),
                LabelResult("document", 0.88f),
                LabelResult("medicine", 0.84f)
            ),
            blurScore = 138.4
        ),

        // =========================================================================
        // 4 REAL PORTRAITS
        // =========================================================================
        DisplayPhoto(
            id = 5L,
            imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=700&q=80",
            drawableRes = R.drawable.sample_portrait_ananya,
            title = "Ananya - Campus Golden Hour",
            subtitle = "Yesterday • Eyes 98% • Smile 94%",
            tag = "portrait 97%",
            ocrSnippet = null,
            faceBox = RectF(0.28f, 0.18f, 0.72f, 0.68f),
            score = 0.97f,
            category = "PEOPLE",
            albumType = AlbumType.PEOPLE,
            smileProb = 0.94f,
            eyesOpenProb = 0.98f,
            blurScore = 156.4,
            personClusterId = 1,
            labels = listOf(
                LabelResult("portrait", 0.97f),
                LabelResult("person", 0.95f),
                LabelResult("smile", 0.94f),
                LabelResult("outdoor", 0.88f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(280, 180, 720, 680), smileProb = 0.94f, eyesOpenProb = 0.98f)
            )
        ),

        DisplayPhoto(
            id = 6L,
            imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=700&q=80",
            drawableRes = R.drawable.sample_portrait_rohit,
            title = "Rohit - Studio Portrait",
            subtitle = "3 days ago • Eyes 94% • Smile 88%",
            tag = "portrait 94%",
            ocrSnippet = null,
            faceBox = RectF(0.25f, 0.15f, 0.75f, 0.70f),
            score = 0.94f,
            category = "PEOPLE",
            albumType = AlbumType.PEOPLE,
            smileProb = 0.88f,
            eyesOpenProb = 0.94f,
            blurScore = 162.0,
            personClusterId = 2,
            labels = listOf(
                LabelResult("portrait", 0.94f),
                LabelResult("person", 0.93f),
                LabelResult("photography", 0.89f),
                LabelResult("studio", 0.86f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(250, 150, 750, 700), smileProb = 0.88f, eyesOpenProb = 0.94f)
            )
        ),

        DisplayPhoto(
            id = 7L,
            imageUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=700&q=80",
            drawableRes = R.drawable.sample_portrait_team,
            title = "Hackathon Core Team Trio",
            subtitle = "5 days ago • Eyes 96% • Smile 92%",
            tag = "team 96%",
            ocrSnippet = null,
            faceBox = RectF(0.15f, 0.20f, 0.85f, 0.65f),
            score = 0.96f,
            category = "PEOPLE",
            albumType = AlbumType.PEOPLE,
            smileProb = 0.92f,
            eyesOpenProb = 0.96f,
            blurScore = 142.8,
            personClusterId = 3,
            labels = listOf(
                LabelResult("people", 0.96f),
                LabelResult("team", 0.93f),
                LabelResult("smile", 0.91f),
                LabelResult("group", 0.89f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(150, 200, 400, 600), smileProb = 0.92f, eyesOpenProb = 0.96f),
                FaceInfo(box = Rect(420, 180, 680, 580), smileProb = 0.95f, eyesOpenProb = 0.98f),
                FaceInfo(box = Rect(700, 220, 920, 620), smileProb = 0.89f, eyesOpenProb = 0.93f)
            )
        ),

        DisplayPhoto(
            id = 8L,
            imageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=700&q=80",
            drawableRes = R.drawable.sample_portrait_alumni,
            title = "Alumni Reunion Portrait",
            subtitle = "Last week • Eyes 93% • Smile 89%",
            tag = "portrait 95%",
            ocrSnippet = null,
            faceBox = RectF(0.30f, 0.20f, 0.70f, 0.65f),
            score = 0.95f,
            category = "PEOPLE",
            albumType = AlbumType.PEOPLE,
            smileProb = 0.89f,
            eyesOpenProb = 0.93f,
            blurScore = 148.0,
            personClusterId = 1,
            labels = listOf(
                LabelResult("portrait", 0.95f),
                LabelResult("person", 0.93f),
                LabelResult("model", 0.89f),
                LabelResult("smile", 0.88f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(300, 200, 700, 650), smileProb = 0.89f, eyesOpenProb = 0.93f)
            )
        ),

        // =========================================================================
        // 4 REAL BURST / ACTION SEQUENCES
        // =========================================================================
        DisplayPhoto(
            id = 9L,
            imageUrl = "https://images.unsplash.com/photo-1519058082700-08a0b56da9b4?w=700&q=80",
            drawableRes = R.drawable.sample_burst_jump_winner,
            title = "Action Burst #1 (Peak Air)",
            subtitle = "Today 17:42 • Sharpness 168.4 • Face 0.96",
            tag = "burst 98%",
            ocrSnippet = null,
            faceBox = RectF(0.42f, 0.18f, 0.58f, 0.38f),
            score = 0.98f,
            category = "BURSTS",
            albumType = AlbumType.PEOPLE,
            smileProb = 0.94f,
            eyesOpenProb = 0.98f,
            blurScore = 168.4,
            isWinner = true,
            rejectionReason = null,
            labels = listOf(
                LabelResult("action", 0.98f),
                LabelResult("jumping", 0.96f),
                LabelResult("athlete", 0.92f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(420, 180, 580, 380), smileProb = 0.94f, eyesOpenProb = 0.98f)
            )
        ),

        DisplayPhoto(
            id = 10L,
            imageUrl = "https://images.unsplash.com/photo-1502680390469-be75c86b636f?w=700&q=80",
            drawableRes = R.drawable.sample_burst_surf_blur,
            title = "Action Burst #2 (Motion Blur)",
            subtitle = "Today 17:42 • Blur 38.2 • Shutter Drag",
            tag = "blur 38.2",
            ocrSnippet = null,
            faceBox = RectF(0.40f, 0.20f, 0.60f, 0.40f),
            score = 0.52f,
            category = "BURSTS",
            albumType = AlbumType.PEOPLE,
            smileProb = 0.72f,
            eyesOpenProb = 0.76f,
            blurScore = 38.2, // Low sharpness
            isWinner = false,
            rejectionReason = "Drop • Blur 38.2 (Motion Blur)",
            labels = listOf(
                LabelResult("action", 0.91f),
                LabelResult("blur", 0.88f)
            )
        ),

        DisplayPhoto(
            id = 11L,
            imageUrl = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=700&q=80",
            drawableRes = R.drawable.sample_burst_run_blink,
            title = "Action Burst #3 (Eyes Blinked)",
            subtitle = "Today 17:42 • Eyes Open 0.14 • Blinked",
            tag = "blinked",
            ocrSnippet = null,
            faceBox = RectF(0.44f, 0.18f, 0.68f, 0.46f),
            score = 0.45f,
            category = "BURSTS",
            albumType = AlbumType.PEOPLE,
            smileProb = 0.85f,
            eyesOpenProb = 0.14f, // Eyes closed
            blurScore = 145.0,
            isWinner = false,
            rejectionReason = "Drop • Eyes Open 0.14 (Blinked)",
            labels = listOf(
                LabelResult("runner", 0.90f),
                LabelResult("person", 0.87f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(440, 180, 680, 460), smileProb = 0.85f, eyesOpenProb = 0.14f)
            )
        ),

        DisplayPhoto(
            id = 12L,
            imageUrl = "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=700&q=80",
            drawableRes = R.drawable.sample_burst_action_angle,
            title = "Action Burst #4 (Awkward Angle)",
            subtitle = "Today 17:42 • Sub-optimal Posture",
            tag = "sub-optimal",
            ocrSnippet = null,
            faceBox = RectF(0.46f, 0.22f, 0.69f, 0.49f),
            score = 0.58f,
            category = "BURSTS",
            albumType = AlbumType.PEOPLE,
            smileProb = 0.60f,
            eyesOpenProb = 0.75f,
            blurScore = 52.0,
            isWinner = false,
            rejectionReason = "Drop • Sub-optimal Angle",
            labels = listOf(
                LabelResult("outdoor", 0.86f),
                LabelResult("running", 0.84f)
            )
        ),

        // =========================================================================
        // 4 REAL DOCUMENTS & CAMPUS SHOTS
        // =========================================================================
        DisplayPhoto(
            id = 13L,
            imageUrl = "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=700&q=80",
            drawableRes = R.drawable.sample_doc_notes,
            title = "CS302 Algorithm Lecture Notes",
            subtitle = "2 days ago • Dynamic Programming",
            tag = "document 97%",
            ocrSnippet = "BELLMAN-FORD SHORTEST PATH | O(V*E) COMPLEXITY",
            faceBox = null,
            score = 0.97f,
            category = "DOCUMENTS",
            albumType = AlbumType.DOCUMENTS,
            rawOcrText = """
                CS302: DESIGN AND ANALYSIS OF ALGORITHMS
                LECTURE 14: DYNAMIC PROGRAMMING
                - BELLMAN-FORD SHORTEST PATH
                - TIME COMPLEXITY: O(V * E)
                - DETECTING NEGATIVE WEIGHT CYCLES
                MEMOIZATION TABLE DP[I][J] = MIN(DP[I-1][J], DP[I-1][K] + COST)
            """.trimIndent(),
            labels = listOf(
                LabelResult("document", 0.97f),
                LabelResult("handwriting", 0.94f),
                LabelResult("paper", 0.92f),
                LabelResult("text", 0.89f)
            ),
            blurScore = 152.0
        ),

        DisplayPhoto(
            id = 14L,
            imageUrl = "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=700&q=80",
            drawableRes = R.drawable.sample_doc_journal,
            title = "Research Lab Daily Log",
            subtitle = "3 days ago • Distributed Systems",
            tag = "document 95%",
            ocrSnippet = "RAFT CONSENSUS LEADER ELECTION PROTOCOL LOG",
            faceBox = null,
            score = 0.95f,
            category = "DOCUMENTS",
            albumType = AlbumType.DOCUMENTS,
            rawOcrText = """
                RESEARCH LOG - AUTONOMOUS SYSTEMS LAB
                PROJECT: DARPAN ON-DEVICE MIRROR ARCHIVE
                EXPERIMENT #42: ZERO-CLOUD OCR INDEXING
                STATUS: VERIFIED (0 PERMISSION INTERNET)
                ML KIT LATIN TEXT ENGINE V2 INTEGRATED
                THROUGHPUT: 42 IMAGES/SEC BENCHMARKED
            """.trimIndent(),
            labels = listOf(
                LabelResult("document", 0.95f),
                LabelResult("notebook", 0.92f),
                LabelResult("paper", 0.90f),
                LabelResult("text", 0.87f)
            ),
            blurScore = 146.0
        ),

        DisplayPhoto(
            id = 15L,
            imageUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=700&q=80",
            drawableRes = R.drawable.sample_doc_code,
            title = "Kotlin Coroutines Architecture",
            subtitle = "4 days ago • IDE Screenshot",
            tag = "screenshot 99%",
            ocrSnippet = "fun observeLiveSearch(): Flow<List<ImageItem>>",
            faceBox = null,
            score = 0.99f,
            category = "SCREENSHOTS",
            albumType = AlbumType.SCREENSHOTS,
            rawOcrText = """
                @OptIn(ExperimentalCoroutinesApi::class)
                fun observeLiveSearch(query: Flow<String>): Flow<List<ImageItem>> {
                    return query
                        .debounce(250)
                        .distinctUntilChanged()
                        .flatMapLatest { token ->
                            repository.searchByTag(token)
                        }
                        .flowOn(Dispatchers.IO)
                }
            """.trimIndent(),
            labels = listOf(
                LabelResult("screenshot", 0.99f),
                LabelResult("code", 0.96f),
                LabelResult("font", 0.91f),
                LabelResult("text", 0.89f)
            ),
            blurScore = 178.0
        ),

        DisplayPhoto(
            id = 16L,
            imageUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=700&q=80",
            drawableRes = R.drawable.sample_doc_campus,
            title = "College Tech Fest Amphitheater",
            subtitle = "Last Friday • Campus Life",
            tag = "campus 96%",
            ocrSnippet = "TECH KRITI 2026 ANNUAL HACKATHON OPENING",
            faceBox = null,
            score = 0.96f,
            category = "DOCUMENTS",
            albumType = AlbumType.DOCUMENTS,
            rawOcrText = """
                TECH KRITI 2026 - ANNUAL INNOVATION SUMMIT
                MAIN STAGE - AUDITORIUM T2
                SESSION: ON-DEVICE EDGE INTELLIGENCE
                SPEAKER: DEEP LEARNING ARCHITECT
                DATE: 20 OCTOBER 2026
            """.trimIndent(),
            labels = listOf(
                LabelResult("campus", 0.96f),
                LabelResult("stage", 0.92f),
                LabelResult("event", 0.91f),
                LabelResult("audience", 0.88f)
            ),
            blurScore = 158.0
        )
    )

    val receiptPhotos: List<DisplayPhoto> get() = photos.filter { it.category == "RECEIPTS" }
    val peoplePhotos: List<DisplayPhoto> get() = photos.filter { it.category == "PEOPLE" }
    val burstPhotos: List<DisplayPhoto> get() = photos.filter { it.category == "BURSTS" }
    val documentPhotos: List<DisplayPhoto> get() = photos.filter { it.category == "DOCUMENTS" || it.category == "SCREENSHOTS" }

    fun getPhotoById(id: Long): DisplayPhoto? = photos.firstOrNull { it.id == id }

    fun filterByCategory(category: String): List<DisplayPhoto> {
        val cat = category.trim().uppercase()
        if (cat == "ALL" || cat.isEmpty()) return photos
        return when (cat) {
            "RECEIPTS" -> receiptPhotos
            "PORTRAITS", "PEOPLE" -> peoplePhotos
            "SCREENSHOTS" -> photos.filter { it.category == "SCREENSHOTS" }
            "DOCUMENTS" -> photos.filter { it.category == "DOCUMENTS" }
            else -> photos.filter { it.category.equals(cat, ignoreCase = true) }
        }
    }

    fun filterByQuery(query: String, modeIsText: Boolean): List<DisplayPhoto> {
        val q = query.trim().lowercase()
        if (q.isEmpty() || q == "all") {
            return if (modeIsText) photos.filter { it.rawOcrText != null } else photos
        }
        return if (modeIsText) {
            photos.filter { photo ->
                photo.rawOcrText != null && (
                    photo.rawOcrText.lowercase().contains(q) ||
                    (q == "receipt" && photo.category == "RECEIPTS") ||
                    (q == "invoice" && photo.category == "RECEIPTS") ||
                    (q == "document" && (photo.category == "DOCUMENTS" || photo.category == "SCREENSHOTS")) ||
                    (q == "screenshot" && photo.category == "SCREENSHOTS") ||
                    photo.title.lowercase().contains(q) ||
                    photo.tag.lowercase().contains(q)
                )
            }
        } else {
            photos.filter { photo ->
                photo.title.lowercase().contains(q) ||
                    photo.tag.lowercase().contains(q) ||
                    photo.category.lowercase().contains(q) ||
                    photo.labels.any { it.text.lowercase().contains(q) }
            }
        }
    }

    fun getClusterGroups(cosineThreshold: Float): Map<Int, List<DisplayPhoto>> {
        val people = peoplePhotos
        return when {
            cosineThreshold <= 0.80f -> {
                mapOf(
                    1 to people.take(2),
                    2 to people.drop(2)
                )
            }
            cosineThreshold in 0.81f..0.90f -> {
                people.groupBy { it.personClusterId ?: 1 }
            }
            else -> {
                people.groupBy { (it.id % 4).toInt() + 1 }
            }
        }
    }
}
