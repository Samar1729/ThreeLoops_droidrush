package com.threeloops.mirrorarchive.albums

/**
 * Rules and search synonyms for album categorization and semantic search expansion.
 */
object AlbumRules {
    /**
     * Synonym mappings for query expansion.
     */
    val synonyms: Map<String, List<String>> = mapOf(
        "cat" to listOf("pet", "animal", "feline", "kitten"),
        "dog" to listOf("pet", "animal", "canine", "puppy"),
        "pet" to listOf("dog", "cat", "animal"),
        "selfie" to listOf("person", "portrait", "face", "people"),
        "people" to listOf("person", "human", "face", "portrait"),
        "bill" to listOf("receipt", "invoice", "payment", "tax"),
        "invoice" to listOf("receipt", "bill", "payment", "subtotal"),
        "receipt" to listOf("bill", "invoice", "total", "tax", "gst"),
        "food" to listOf("dish", "meal", "cuisine", "snack", "dinner", "lunch"),
        "screenshot" to listOf("capture", "screen", "display")
    )
}
