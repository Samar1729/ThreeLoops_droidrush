package com.threeloops.mirrorarchive.data.mediastore

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitmapLoaderTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private lateinit var loader: BitmapLoader
    private var fixtureUri: Uri? = null

    @Before
    fun setUp() {
        val permissions = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        instrumentation.uiAutomation.adoptShellPermissionIdentity(*permissions)
        loader = BitmapLoader(context)
    }

    @After
    fun tearDown() {
        fixtureUri?.let { context.contentResolver.delete(it, null, null) }
        instrumentation.uiAutomation.dropShellPermissionIdentity()
    }

    @Test
    fun loadOnNonexistentMediaStoreUriReturnsNull() {
        val result = loader.load(Uri.parse("content://media/external/images/media/-1"))
        assertNull(result)
    }

    @Test
    fun loadOnRealMediaStoreImageReturnsThumbnailAtMost512Pixels() {
        val image = Bitmap.createBitmap(96, 64, Bitmap.Config.ARGB_8888)
        try {
            image.eraseColor(android.graphics.Color.MAGENTA)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "bitmap-loader-test-${System.nanoTime()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BitmapLoaderTest")
                if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            assertNotNull("Could not insert MediaStore fixture", uri)
            fixtureUri = uri
            context.contentResolver.openOutputStream(uri!!, "w")!!.use { output ->
                assertTrue("Could not encode MediaStore fixture", image.compress(Bitmap.CompressFormat.PNG, 100, output))
            }
            if (Build.VERSION.SDK_INT >= 29) {
                context.contentResolver.update(uri, ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null)
            }

            val thumbnail = loader.load(uri)
            assertNotNull("MediaStore thumbnail should load", thumbnail)
            try {
                val maxSide = maxOf(thumbnail!!.width, thumbnail.height)
                assertTrue("Thumbnail dimensions must be positive", maxSide > 0)
                assertTrue("Thumbnail max side $maxSide exceeds 512", maxSide <= 512)
            } finally {
                loader.recycle(thumbnail)
            }
        } finally {
            image.recycle()
        }
    }
}
