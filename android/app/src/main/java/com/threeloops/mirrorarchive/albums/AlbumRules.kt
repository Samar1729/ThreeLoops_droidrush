package com.threeloops.mirrorarchive.albums

/**
 * 3.8 Album Rules & Synonyms
 * Owner: Aditya
 *
 * Config object for auto-album heuristics and search synonyms.
 */
object AlbumRules {
    val receiptKeywords = listOf(
        "total", "subtotal", "invoice", "gst", "tax", "amount", "bill", "receipt", "qty"
    )

    const val documentMinOcrLength = 80

    val foodLabels = setOf(
        "food", "dish", "cuisine", "meal", "fruit", "vegetable", "dessert",
        "pizza", "pasta", "burger", "salad", "coffee", "cake", "bread",
        "snack", "drink", "beverage", "tableware", "recipe", "breakfast", "lunch", "dinner"
    )

    val peopleLabels = setOf(
        "person", "face", "smile", "human", "boy", "girl", "man", "woman",
        "portrait", "crowd", "baby", "child", "teenager", "family", "selfie"
    )

    val textLikeLabels = setOf(
        "text", "document", "paper", "font", "handwriting", "envelope", "page",
        "receipt", "book", "newspaper", "stationery", "label", "ticket", "diagram"
    )

    val synonyms: Map<String, List<String>> = mapOf(
        "cat" to listOf("pet", "feline", "kitten"),
        "dog" to listOf("pet", "canine", "puppy"),
        "selfie" to listOf("person", "portrait", "face"),
        "car" to listOf("automobile", "vehicle", "transport"),
        "latte" to listOf("coffee", "beverage", "drink"),
        "receipt" to listOf("bill", "invoice", "expense"),
        "food" to listOf("dish", "meal", "cuisine")
    )
}
