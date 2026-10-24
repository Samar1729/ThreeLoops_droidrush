package com.threeloops.mirrorarchive.data.model

import android.graphics.Rect

/**
 * Section 3.7: LabelResult contract owned by Aditya
 */
data class LabelResult(
    val text: String,
    val confidence: Float
)

/**
 * Section 3.7: FaceInfo contract owned by Aditya
 */
data class FaceInfo(
    val box: Rect,
    val smileProb: Float?,
    val eyesOpenProb: Float?
)
