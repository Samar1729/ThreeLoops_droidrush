package com.threeloops.mirrorarchive.ui.navigation

import com.threeloops.mirrorarchive.contracts.AlbumType

/**
 * Type-safe screen abstractions referencing [Routes].
 *
 * @property route Route pattern string.
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen(Routes.ONBOARDING)
    data object PermissionDenied : Screen(Routes.PERMISSION_DENIED)
    data object Albums : Screen(Routes.ALBUMS)
    data object AlbumDetail : Screen(Routes.ALBUM_DETAIL) {
        fun createRoute(type: AlbumType) = Routes.albumDetail(type.name)
    }
    data object ImageDetail : Screen(Routes.IMAGE_DETAIL) {
        fun createRoute(imageId: Long) = Routes.imageDetail(imageId)
    }
    data object Search : Screen(Routes.SEARCH) {
        fun createRoute(mode: String = "TAGS", q: String = "") = Routes.search(mode, q)
    }
    data object People : Screen(Routes.PEOPLE)
    data object Duplicates : Screen(Routes.DUPLICATES)
    data object DuplicateGroup : Screen(Routes.DUPLICATE_GROUP) {
        fun createRoute(groupId: Long) = Routes.duplicateGroup(groupId)
    }
    data object StorageSaver : Screen(Routes.STORAGE_SAVER)
    data object Export : Screen(Routes.EXPORT) {
        fun createRoute(ids: String = "") = Routes.export(ids)
    }
    data object Settings : Screen(Routes.SETTINGS)
    data object Metrics : Screen(Routes.METRICS)
}
