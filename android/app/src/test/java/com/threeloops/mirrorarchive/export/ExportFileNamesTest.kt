package com.threeloops.mirrorarchive.export

import com.threeloops.mirrorarchive.data.export.ExportFileNames
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportFileNamesTest {
    @Test
    fun unsafeArchiveNamesAreSanitizedAndBlankNamesGetADefault() {
        assertEquals("holiday_export", ExportFileNames.safeArchiveName("../holiday/export"))
        assertEquals("MirrorArchive_Export", ExportFileNames.safeArchiveName("..."))
    }

    @Test
    fun duplicateZipEntriesGetDistinctNamesWithoutChangingExtensions() {
        val usedNames = mutableSetOf<String>()

        assertEquals("photo.jpg", ExportFileNames.uniqueEntryName("photo.jpg", usedNames))
        assertEquals("photo (2).jpg", ExportFileNames.uniqueEntryName("photo.jpg", usedNames))
        assertEquals("PHOTO (3).JPG", ExportFileNames.uniqueEntryName("PHOTO.JPG", usedNames))
    }

    @Test
    fun zipEntriesCannotEscapeTheArchiveDirectory() {
        val name = ExportFileNames.uniqueEntryName("../../private/photo.jpg", mutableSetOf())

        assertEquals("photo.jpg", name)
    }
}
