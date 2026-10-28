package com.threeloops.mirrorarchive.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.KeyStore
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class KeyProviderTest {
    private lateinit var directory: File
    private lateinit var alias: String

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        alias = "mirror_db_key_test_${UUID.randomUUID().toString().replace('-', '_')}"
        directory = File(context.cacheDir, "key-provider-test-$alias").apply { mkdirs() }
        keyStore().deleteEntry(alias)
    }

    @After
    fun tearDown() {
        runCatching { keyStore().deleteEntry(alias) }
        directory.deleteRecursively()
    }

    @Test
    fun firstCallStoresWrappedKeyAndLaterProviderRecoversSamePassphrase() {
        val firstProvider = provider()
        val firstPassphrase = firstProvider.getPassphrase()
        assertTrue(File(directory, KeyProvider.KEY_FILE_NAME).isFile)

        val secondPassphrase = provider().getPassphrase()
        assertArrayEquals(firstPassphrase, secondPassphrase)

        firstPassphrase.fill(0)
        secondPassphrase.fill(0)
    }

    @Test
    fun missingKeyFileForExistingAliasThrowsKeyLossException() {
        val keyProvider = provider()
        keyProvider.getPassphrase().fill(0)
        assertTrue(File(directory, KeyProvider.KEY_FILE_NAME).delete())

        assertThrows(KeyLossException::class.java) { keyProvider.getPassphrase() }
    }

    private fun provider() = KeyProvider(
        keystoreProviderName = KEYSTORE_PROVIDER,
        keyFileDirectory = directory,
        alias = alias
    )

    private fun keyStore() = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

    private companion object { const val KEYSTORE_PROVIDER = "AndroidKeyStore" }
}
