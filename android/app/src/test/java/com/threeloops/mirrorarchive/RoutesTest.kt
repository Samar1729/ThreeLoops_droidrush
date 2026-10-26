package com.threeloops.mirrorarchive

import com.threeloops.mirrorarchive.ui.navigation.Routes
import com.threeloops.mirrorarchive.ui.navigation.bottomNavItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying navigation routes, builders, and bottom bar tab definitions.
 */
class RoutesTest {

    @Test
    fun verifyRouteConstants() {
        assertEquals("onboarding", Routes.ONBOARDING)
        assertEquals("permission_denied", Routes.PERMISSION_DENIED)
        assertEquals("albums", Routes.ALBUMS)
        assertEquals("albums/{albumType}", Routes.ALBUM_DETAIL)
        assertEquals("image/{imageId}", Routes.IMAGE_DETAIL)
        assertEquals("search?mode={mode}&q={q}", Routes.SEARCH)
        assertEquals("people", Routes.PEOPLE)
        assertEquals("duplicates", Routes.DUPLICATES)
        assertEquals("duplicates/group/{groupId}", Routes.DUPLICATE_GROUP)
        assertEquals("storage_saver", Routes.STORAGE_SAVER)
        assertEquals("export?ids={ids}", Routes.EXPORT)
        assertEquals("settings", Routes.SETTINGS)
        assertEquals("metrics", Routes.METRICS)
    }

    @Test
    fun verifyRouteBuilders() {
        assertEquals("albums/RECEIPTS", Routes.albumDetail("RECEIPTS"))
        assertEquals("image/42", Routes.imageDetail(42L))
        assertEquals("duplicates/group/7", Routes.duplicateGroup(7L))
        assertEquals("search?mode=TAGS&q=flower", Routes.search("TAGS", "flower"))
        assertEquals("search?mode=TAGS&q=", Routes.search())
        assertEquals("export?ids=1,2,3", Routes.export("1,2,3"))
        assertEquals("export?ids=", Routes.export())
    }

    @Test
    fun verifyFiveBottomTabs() {
        assertEquals(5, bottomNavItems.size)
        assertEquals("Library", bottomNavItems[0].label)
        assertEquals(Routes.ALBUMS, bottomNavItems[0].route)

        assertEquals("Dupes", bottomNavItems[1].label)
        assertEquals(Routes.DUPLICATES, bottomNavItems[1].route)

        assertEquals("Search", bottomNavItems[2].label)
        assertTrue(bottomNavItems[2].route.startsWith("search"))

        assertEquals("People", bottomNavItems[3].label)
        assertEquals(Routes.PEOPLE, bottomNavItems[3].route)

        assertEquals("Ledger", bottomNavItems[4].label)
        assertEquals(Routes.LEDGER, bottomNavItems[4].route)
    }
}
