package com.threeloops.mirrorarchive.ui.navigation

import com.threeloops.mirrorarchive.data.model.AlbumType

/**
 * Section 5: Navigation Routes (Compose)
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object PermissionDenied : Screen("permission_denied")
    data object Albums : Screen("albums")
    data object AlbumDetail : Screen("albums/{albumType}") {
        fun createRoute(type: AlbumType) = "albums/${type.name}"
    }
    data object ImageDetail : Screen("image/{imageId}") {
        fun createRoute(imageId: Long) = "image/$imageId"
    }
    data object Search : Screen("search")
    data object People : Screen("people")
    data object Duplicates : Screen("duplicates")
    data object DuplicateGroup : Screen("duplicates/group/{groupId}")
    data object StorageSaver : Screen("storage_saver")
    data object Export : Screen("export?ids={ids}")
    data object Settings : Screen("settings")
    data object Metrics : Screen("metrics")
}
