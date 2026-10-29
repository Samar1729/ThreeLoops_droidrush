package com.threeloops.mirrorarchive.ui.navigation

/**
 * Central route string constants and helpers for Navigation Compose.
 */
object Routes {
    /** Route for user onboarding flow. */
    const val ONBOARDING = "onboarding"

    /** Route for permission denial explanation screen. */
    const val PERMISSION_DENIED = "permission_denied"

    /** Main bottom tab route for smart albums grid. */
    const val ALBUMS = "albums"

    /** Route pattern for album detail with albumType argument. */
    const val ALBUM_DETAIL = "albums/{albumType}"

    /** Route pattern for image inspector with imageId argument. */
    const val IMAGE_DETAIL = "image/{imageId}"

    /** Main bottom tab and parameterized route for search. */
    const val SEARCH = "search?mode={mode}&q={q}"

    /** Main bottom tab route for people and face clusters. */
    const val PEOPLE = "people"

    /** Route for duplicate finder overview. */
    const val DUPLICATES = "duplicates"

    /** Route pattern for duplicate group detail with groupId argument. */
    const val DUPLICATE_GROUP = "duplicates/group/{groupId}"

    /** Route for storage savings optimizer. */
    const val STORAGE_SAVER = "storage_saver"

    /** Route pattern for archive export with optional media ids. */
    const val EXPORT = "export?ids={ids}"

    /** Route for app settings and storage preferences. */
    const val SETTINGS = "settings"

    /** Route for analytics and performance metrics. */
    const val METRICS = "metrics"

    /** Route for Raseed Ledger (offline FinTech receipt & expense extractor). */
    const val LEDGER = "ledger"

    /**
     * Builds navigation route for album detail.
     *
     * @param albumType The category name of the album.
     */
    fun albumDetail(albumType: String): String = "albums/$albumType"

    /**
     * Builds navigation route for image detail.
     *
     * @param imageId The unique database or MediaStore ID.
     */
    fun imageDetail(imageId: Long): String = "image/$imageId"

    /**
     * Builds navigation route for duplicate group.
     *
     * @param groupId The group cluster ID.
     */
    fun duplicateGroup(groupId: Long): String = "duplicates/group/$groupId"

    /**
     * Builds navigation route for search with query parameters.
     *
     * @param mode Search filter mode (TAGS or TEXT).
     * @param q Query search term.
     */
    fun search(mode: String = "TAGS", q: String = ""): String = "search?mode=$mode&q=$q"

    /**
     * Builds navigation route for export with optional media IDs.
     *
     * @param ids Comma-separated media IDs.
     */
    fun export(ids: String = ""): String = "export?ids=$ids"

    fun export(ids: List<Long>): String = "export?ids=${ids.joinToString(",")}"
}
