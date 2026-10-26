package com.threeloops.mirrorarchive.data.demo

import android.graphics.Rect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.LabelResult

data class DemoPhoto(
    val id: Long,
    val title: String,
    val category: String, // "RECEIPTS", "PEOPLE", "BURSTS", "DOCUMENTS", "FOOD"
    val albumType: AlbumType,
    val gradientColors: List<Color>,
    val iconVector: ImageVector,
    val ocrText: String? = null,
    val labels: List<LabelResult>,
    val faces: List<FaceInfo> = emptyList(),
    val faceQuality: Float = 0f,
    val blurScore: Double = 85.0,
    val dateFormatted: String,
    val sizeBytes: Long = 2_850_000L,
    val width: Int = 1920,
    val height: Int = 1080,
    val merchant: String? = null,
    val totalAmount: Double? = null,
    val gstin: String? = null,
    val isWinner: Boolean = false,
    val rejectionReason: String? = null,
    val burstGroupId: Long? = null,
    val personClusterId: Int? = null,
    val primaryLabel: String,
    val primaryConfidence: Float
)

object DemoSeedData {

    val photos: List<DemoPhoto> = listOf(
        // ==========================================
        // 4 INDIAN GST RECEIPTS
        // ==========================================
        DemoPhoto(
            id = 1,
            title = "Starbucks Flat White & Croissant",
            category = "RECEIPTS",
            albumType = AlbumType.RECEIPTS,
            gradientColors = listOf(Color(0xFF0F3826), Color(0xFF071C13)),
            iconVector = Icons.Outlined.Coffee,
            ocrText = """
                STARBUCKS COFFEE INDIA
                STORE #4812, 100 FT ROAD, INDIRANAGAR
                GSTIN: 29AAACB2184A1Z5
                DATE: 24/10/2026  TIME: 09:42 AM
                
                ITEM                  QTY    PRICE
                FLAT WHITE TALL         1    280.00
                BUTTER CROISSANT        1    170.00
                
                SUBTOTAL:                    450.00
                CGST 2.5%:                    11.25
                SGST 2.5%:                    11.25
                NET TOTAL:                 ₹ 472.50
                PAID VIA: UPI / GPAY
            """.trimIndent(),
            labels = listOf(
                LabelResult("receipt", 0.96f),
                LabelResult("paper", 0.94f),
                LabelResult("coffee", 0.88f),
                LabelResult("text", 0.85f)
            ),
            dateFormatted = "24 Oct 2026",
            merchant = "STARBUCKS COFFEE",
            totalAmount = 472.50,
            gstin = "29AAACB2184A1Z5",
            primaryLabel = "receipt",
            primaryConfidence = 0.96f
        ),
        DemoPhoto(
            id = 2,
            title = "Shell Highway Fuel Refill",
            category = "RECEIPTS",
            albumType = AlbumType.RECEIPTS,
            gradientColors = listOf(Color(0xFF381C0F), Color(0xFF1C0E07)),
            iconVector = Icons.Outlined.LocalGasStation,
            ocrText = """
                SHELL INDIA MARKETS PVT LTD
                RETAIL OUTLET #104, NH-44 HIGHWAY
                GSTIN: 27AAECS1490F1ZM
                DATE: 22/10/2026  TIME: 19:15 PM
                
                PUMP #03 V-POWER UNLEADED
                RATE: ₹ 104.50 / LTR
                VOLUME: 23.92 LITRES
                
                TAXABLE AMOUNT:            2,118.64
                IGST 18%:                    381.36
                GRAND TOTAL:             ₹ 2,500.00
                MODE: HDFC VISA CREDIT
            """.trimIndent(),
            labels = listOf(
                LabelResult("receipt", 0.95f),
                LabelResult("bill", 0.92f),
                LabelResult("paper", 0.89f),
                LabelResult("fuel", 0.84f)
            ),
            dateFormatted = "22 Oct 2026",
            merchant = "SHELL FUEL STATION",
            totalAmount = 2500.00,
            gstin = "27AAECS1490F1ZM",
            primaryLabel = "receipt",
            primaryConfidence = 0.95f
        ),
        DemoPhoto(
            id = 3,
            title = "Reliance Supermarket Groceries",
            category = "RECEIPTS",
            albumType = AlbumType.RECEIPTS,
            gradientColors = listOf(Color(0xFF1B2A4A), Color(0xFF0E1626)),
            iconVector = Icons.Outlined.ShoppingBag,
            ocrText = """
                RELIANCE RETAIL SMART BAZAAR
                STORE CODE: 7194, KORAMANGALA
                GSTIN: 27AAACR7194N1ZX
                DATE: 19/10/2026  TIME: 16:30 PM
                
                ITEM                  QTY    PRICE
                CALIFORNIA ALMONDS 500G  1    420.00
                EXTRA VIRGIN OLIVE OIL   1    850.00
                ROLLED OATS 1KG          2    550.50
                
                SUBTOTAL:                  1,820.50
                DISCOUNT:                     0.00
                TOTAL PAYABLE:           ₹ 1,820.50
                STATUS: PAID
            """.trimIndent(),
            labels = listOf(
                LabelResult("receipt", 0.93f),
                LabelResult("invoice", 0.91f),
                LabelResult("paper", 0.88f),
                LabelResult("groceries", 0.82f)
            ),
            dateFormatted = "19 Oct 2026",
            merchant = "RELIANCE RETAIL",
            totalAmount = 1820.50,
            gstin = "27AAACR7194N1ZX",
            primaryLabel = "invoice",
            primaryConfidence = 0.93f
        ),
        DemoPhoto(
            id = 4,
            title = "Apollo Pharmacy Prescription",
            category = "RECEIPTS",
            albumType = AlbumType.RECEIPTS,
            gradientColors = listOf(Color(0xFF2E1A38), Color(0xFF170D1C)),
            iconVector = Icons.Outlined.Medication,
            ocrText = """
                APOLLO PHARMACY LTD
                UNIT #890, BTM LAYOUT 2ND STAGE
                GSTIN: 33AAACA0890B1Z8
                DATE: 15/10/2026  TIME: 20:10 PM
                
                DRUG DESCRIPTION      BATCH    AMT
                VITAMIN C 500MG CHEW   VC42   240.00
                DAILY MULTIVITAMIN     MV19   400.00
                
                TAXABLE VALUE:                571.43
                GST @ 12%:                     68.57
                NET AMOUNT:                 ₹ 640.00
                REGISTERED PHARMACIST APPROVED
            """.trimIndent(),
            labels = listOf(
                LabelResult("receipt", 0.92f),
                LabelResult("pharmacy", 0.89f),
                LabelResult("document", 0.86f),
                LabelResult("medicine", 0.84f)
            ),
            dateFormatted = "15 Oct 2026",
            merchant = "APOLLO PHARMACY",
            totalAmount = 640.00,
            gstin = "33AAACA0890B1Z8",
            primaryLabel = "receipt",
            primaryConfidence = 0.92f
        ),

        // ==========================================
        // 4 PORTRAIT & GROUP PHOTOS
        // ==========================================
        DemoPhoto(
            id = 5,
            title = "Ananya - Campus Golden Hour",
            category = "PEOPLE",
            albumType = AlbumType.PEOPLE,
            gradientColors = listOf(Color(0xFF382314), Color(0xFF1A1009)),
            iconVector = Icons.Outlined.Person,
            labels = listOf(
                LabelResult("portrait", 0.97f),
                LabelResult("person", 0.95f),
                LabelResult("smile", 0.92f),
                LabelResult("outdoor", 0.88f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(380, 240, 720, 680), smileProb = 0.94f, eyesOpenProb = 0.98f)
            ),
            faceQuality = 0.96f,
            blurScore = 156.4,
            dateFormatted = "Yesterday",
            personClusterId = 1,
            primaryLabel = "portrait",
            primaryConfidence = 0.97f
        ),
        DemoPhoto(
            id = 6,
            title = "Hackathon Core Team Trio",
            category = "PEOPLE",
            albumType = AlbumType.PEOPLE,
            gradientColors = listOf(Color(0xFF142B38), Color(0xFF09161D)),
            iconVector = Icons.Outlined.Groups,
            labels = listOf(
                LabelResult("people", 0.96f),
                LabelResult("team", 0.93f),
                LabelResult("smile", 0.91f),
                LabelResult("indoor", 0.87f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(140, 220, 360, 500), smileProb = 0.92f, eyesOpenProb = 0.96f),
                FaceInfo(box = Rect(420, 200, 660, 510), smileProb = 0.95f, eyesOpenProb = 0.98f),
                FaceInfo(box = Rect(720, 230, 940, 520), smileProb = 0.88f, eyesOpenProb = 0.92f)
            ),
            faceQuality = 0.93f,
            blurScore = 142.8,
            dateFormatted = "3 days ago",
            personClusterId = 2,
            primaryLabel = "team",
            primaryConfidence = 0.96f
        ),
        DemoPhoto(
            id = 7,
            title = "Rohit - Studio Portrait",
            category = "PEOPLE",
            albumType = AlbumType.PEOPLE,
            gradientColors = listOf(Color(0xFF281838), Color(0xFF130A1C)),
            iconVector = Icons.Outlined.Face,
            labels = listOf(
                LabelResult("portrait", 0.94f),
                LabelResult("person", 0.93f),
                LabelResult("photography", 0.89f),
                LabelResult("black and white", 0.86f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(400, 210, 760, 690), smileProb = 0.88f, eyesOpenProb = 0.94f)
            ),
            faceQuality = 0.91f,
            blurScore = 168.0,
            dateFormatted = "5 days ago",
            personClusterId = 1,
            primaryLabel = "portrait",
            primaryConfidence = 0.94f
        ),
        DemoPhoto(
            id = 8,
            title = "Alumni Reunion Group Celebration",
            category = "PEOPLE",
            albumType = AlbumType.PEOPLE,
            gradientColors = listOf(Color(0xFF1E3321), Color(0xFF0F1A11)),
            iconVector = Icons.Outlined.Celebration,
            labels = listOf(
                LabelResult("celebration", 0.95f),
                LabelResult("people", 0.94f),
                LabelResult("group", 0.92f),
                LabelResult("smile", 0.90f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(120, 260, 310, 480), smileProb = 0.89f, eyesOpenProb = 0.93f),
                FaceInfo(box = Rect(340, 240, 520, 470), smileProb = 0.94f, eyesOpenProb = 0.97f),
                FaceInfo(box = Rect(550, 230, 730, 460), smileProb = 0.91f, eyesOpenProb = 0.95f),
                FaceInfo(box = Rect(760, 250, 940, 480), smileProb = 0.86f, eyesOpenProb = 0.91f)
            ),
            faceQuality = 0.90f,
            blurScore = 135.2,
            dateFormatted = "Last week",
            personClusterId = 3,
            primaryLabel = "celebration",
            primaryConfidence = 0.95f
        ),

        // ==========================================
        // 4 BURST / DUPLICATE SEQUENCES
        // ==========================================
        DemoPhoto(
            id = 9,
            title = "Action Burst #1 (Winner - Peak Jump)",
            category = "BURSTS",
            albumType = AlbumType.PEOPLE,
            gradientColors = listOf(Color(0xFF133829), Color(0xFF0A1F17)),
            iconVector = Icons.Outlined.AutoAwesome,
            labels = listOf(
                LabelResult("jumping", 0.96f),
                LabelResult("action", 0.94f),
                LabelResult("person", 0.92f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(440, 180, 680, 460), smileProb = 0.93f, eyesOpenProb = 0.97f)
            ),
            faceQuality = 0.95f,
            blurScore = 192.4,
            dateFormatted = "Today 17:42",
            isWinner = true,
            rejectionReason = null,
            burstGroupId = 101,
            primaryLabel = "action",
            primaryConfidence = 0.96f
        ),
        DemoPhoto(
            id = 10,
            title = "Action Burst #2 (Motion Blur)",
            category = "BURSTS",
            albumType = AlbumType.PEOPLE,
            gradientColors = listOf(Color(0xFF381414), Color(0xFF1E0A0A)),
            iconVector = Icons.Outlined.BlurOn,
            labels = listOf(
                LabelResult("motion blur", 0.91f),
                LabelResult("jumping", 0.88f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(430, 190, 670, 470), smileProb = 0.72f, eyesOpenProb = 0.81f)
            ),
            faceQuality = 0.76f,
            blurScore = 38.2, // Low sharpness -> motion blur
            dateFormatted = "Today 17:42",
            isWinner = false,
            rejectionReason = "Drop • Blur 38.2 (Motion Blur)",
            burstGroupId = 101,
            primaryLabel = "motion blur",
            primaryConfidence = 0.91f
        ),
        DemoPhoto(
            id = 11,
            title = "Action Burst #3 (Eyes Blinked)",
            category = "BURSTS",
            albumType = AlbumType.PEOPLE,
            gradientColors = listOf(Color(0xFF382C14), Color(0xFF1E170A)),
            iconVector = Icons.Outlined.VisibilityOff,
            labels = listOf(
                LabelResult("person", 0.90f),
                LabelResult("jumping", 0.87f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(440, 185, 680, 465), smileProb = 0.85f, eyesOpenProb = 0.14f) // Blinked!
            ),
            faceQuality = 0.38f,
            blurScore = 145.0,
            dateFormatted = "Today 17:42",
            isWinner = false,
            rejectionReason = "Drop • Eyes Open 0.14 (Blinked)",
            burstGroupId = 101,
            primaryLabel = "person",
            primaryConfidence = 0.90f
        ),
        DemoPhoto(
            id = 12,
            title = "Action Burst #4 (Awkward Angle)",
            category = "BURSTS",
            albumType = AlbumType.PEOPLE,
            gradientColors = listOf(Color(0xFF27272A), Color(0xFF18181B)),
            iconVector = Icons.Outlined.CropFree,
            labels = listOf(
                LabelResult("outdoor", 0.86f),
                LabelResult("person", 0.84f)
            ),
            faces = listOf(
                FaceInfo(box = Rect(460, 220, 690, 490), smileProb = 0.60f, eyesOpenProb = 0.75f)
            ),
            faceQuality = 0.68f,
            blurScore = 52.0,
            dateFormatted = "Today 17:42",
            isWinner = false,
            rejectionReason = "Drop • Sub-optimal Angle",
            burstGroupId = 101,
            primaryLabel = "outdoor",
            primaryConfidence = 0.86f
        ),

        // ==========================================
        // 4 SCREENSHOTS & DOCUMENTS
        // ==========================================
        DemoPhoto(
            id = 13,
            title = "IndiGo Boarding Pass (BLR -> DEL)",
            category = "DOCUMENTS",
            albumType = AlbumType.DOCUMENTS,
            gradientColors = listOf(Color(0xFF0F2642), Color(0xFF071221)),
            iconVector = Icons.Outlined.FlightTakeoff,
            ocrText = """
                INDIGO AIRLINES - BOARDING PASS
                PASSENGER: ADITYA SINGH
                PNR: 6E-4812   FLIGHT: 6E-204
                FROM: BLR (BENGALURU T2)
                TO:   DEL (DELHI T3)
                DATE: 28 OCT 2026   DEPARTURE: 06:15 AM
                GATE: 14B   BOARDING: 05:35 AM   SEAT: 04F
                ZONE: 2     SEQ: 034
                ELECTRONIC TICKET / BARCODE: 48129038102
            """.trimIndent(),
            labels = listOf(
                LabelResult("boarding pass", 0.98f),
                LabelResult("ticket", 0.95f),
                LabelResult("document", 0.92f),
                LabelResult("text", 0.89f)
            ),
            dateFormatted = "Yesterday",
            primaryLabel = "boarding pass",
            primaryConfidence = 0.98f
        ),
        DemoPhoto(
            id = 14,
            title = "Metro Purple Line Schedule",
            category = "SCREENSHOTS",
            albumType = AlbumType.SCREENSHOTS,
            gradientColors = listOf(Color(0xFF2A1438), Color(0xFF140A1C)),
            iconVector = Icons.Outlined.DirectionsSubway,
            ocrText = """
                BMRCL NAMMA METRO PURPLE LINE SCHEDULE
                WHITEFIELD (KADUGODI) TO CHALLAGHATTA
                FIRST TRAIN: 05:00 AM   LAST TRAIN: 23:00 PM
                PEAK HOURS (08:30 - 11:00 AM): EVERY 4 MINS
                NON-PEAK: EVERY 8 MINS
                SMART CARD FARE DISCOUNT: 5%
            """.trimIndent(),
            labels = listOf(
                LabelResult("screenshot", 0.98f),
                LabelResult("timetable", 0.93f),
                LabelResult("text", 0.91f),
                LabelResult("diagram", 0.87f)
            ),
            dateFormatted = "2 days ago",
            primaryLabel = "screenshot",
            primaryConfidence = 0.98f
        ),
        DemoPhoto(
            id = 15,
            title = "University Student ID Card",
            category = "DOCUMENTS",
            albumType = AlbumType.DOCUMENTS,
            gradientColors = listOf(Color(0xFF1F382B), Color(0xFF0F1C15)),
            iconVector = Icons.Outlined.Badge,
            ocrText = """
                NATIONAL INSTITUTE OF TECHNOLOGY
                STUDENT IDENTIFICATION CARD
                NAME: ADITYA SINGH
                ROLL NO: 22BCS108
                DEPARTMENT: COMPUTER SCIENCE & ENGG
                BLOOD GROUP: B+   VALID TILL: JUNE 2026
                CHIP SERIAL: NIT-2022-CS-4812
            """.trimIndent(),
            labels = listOf(
                LabelResult("identity document", 0.97f),
                LabelResult("card", 0.94f),
                LabelResult("document", 0.91f),
                LabelResult("text", 0.88f)
            ),
            dateFormatted = "3 days ago",
            primaryLabel = "id card",
            primaryConfidence = 0.97f
        ),
        DemoPhoto(
            id = 16,
            title = "Kotlin Coroutines Architecture Code",
            category = "SCREENSHOTS",
            albumType = AlbumType.SCREENSHOTS,
            gradientColors = listOf(Color(0xFF1B1B26), Color(0xFF0F0F17)),
            iconVector = Icons.Outlined.Code,
            ocrText = """
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
            dateFormatted = "4 days ago",
            primaryLabel = "code",
            primaryConfidence = 0.99f
        )
    )

    val receiptPhotos: List<DemoPhoto> get() = photos.filter { it.category == "RECEIPTS" }
    val peoplePhotos: List<DemoPhoto> get() = photos.filter { it.category == "PEOPLE" }
    val burstPhotos: List<DemoPhoto> get() = photos.filter { it.category == "BURSTS" }
    val documentPhotos: List<DemoPhoto> get() = photos.filter { it.category == "DOCUMENTS" || it.category == "SCREENSHOTS" }

    fun getPhotoById(id: Long): DemoPhoto? = photos.firstOrNull { it.id == id }

    fun filterByQuery(query: String, modeIsText: Boolean): List<DemoPhoto> {
        val q = query.trim().lowercase()
        if (q.isEmpty() || q == "all") {
            return if (modeIsText) photos.filter { it.ocrText != null } else photos
        }
        return if (modeIsText) {
            photos.filter { photo ->
                photo.ocrText != null && (
                    photo.ocrText.lowercase().contains(q) ||
                    (q == "receipt" && photo.category == "RECEIPTS") ||
                    (q == "invoice" && photo.category == "RECEIPTS") ||
                    (q == "document" && (photo.category == "DOCUMENTS" || photo.category == "SCREENSHOTS")) ||
                    (q == "screenshot" && photo.category == "SCREENSHOTS") ||
                    photo.primaryLabel.lowercase().contains(q) ||
                    photo.title.lowercase().contains(q)
                )
            }
        } else {
            photos.filter { photo ->
                photo.title.lowercase().contains(q) ||
                    photo.primaryLabel.lowercase().contains(q) ||
                    photo.labels.any { it.text.lowercase().contains(q) } ||
                    photo.category.lowercase().contains(q)
            }
        }
    }

    fun filterByTag(tag: String): List<DemoPhoto> {
        val t = tag.trim().lowercase()
        if (t == "all" || t.isEmpty()) return photos
        return photos.filter { photo ->
            photo.category.equals(t, ignoreCase = true) ||
                photo.primaryLabel.equals(t, ignoreCase = true) ||
                photo.labels.any { it.text.equals(t, ignoreCase = true) }
        }
    }

    fun getClusterGroups(cosineThreshold: Float): Map<Int, List<DemoPhoto>> {
        val people = photos.filter { it.category == "PEOPLE" }
        // Dynamically adjust clustering based on threshold slider
        return when {
            cosineThreshold <= 0.80f -> {
                // Loose similarity: merge into 2 big groups
                mapOf(
                    1 to people.take(3),
                    2 to people.drop(3)
                )
            }
            cosineThreshold in 0.81f..0.90f -> {
                // Balanced similarity: 3 distinct groups
                people.groupBy { it.personClusterId ?: 1 }
            }
            else -> {
                // Strict similarity: 4 groups
                people.groupBy { (it.id % 4).toInt() + 1 }
            }
        }
    }
}
