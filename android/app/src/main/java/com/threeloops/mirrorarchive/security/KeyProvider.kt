package com.threeloops.mirrorarchive.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Singleton

class KeyLossException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Supplies a database passphrase wrapped by a non-exportable Android Keystore AES key. */
@Singleton
class KeyProvider(
    private val keystoreProviderName: String,
    private val keyFileDirectory: File,
    private val alias: String = KEY_ALIAS,
    private val hasExistingDatabase: () -> Boolean = { false }
) {
    private var cachedPassphrase: ByteArray? = null

    @Synchronized
    fun resetForRecovery() {
        cachedPassphrase?.fill(0)
        cachedPassphrase = null
        File(keyFileDirectory, KEY_FILE_NAME).delete()
        File(keyFileDirectory, "$KEY_FILE_NAME.tmp").delete()
        try {
            val keyStore = KeyStore.getInstance(keystoreProviderName).apply { load(null) }
            if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias)
        } catch (error: Exception) {
            throw KeyLossException("Could not clear the database wrapping key", error)
        }
    }

    @Synchronized
    fun getPassphrase(): ByteArray {
        cachedPassphrase?.let {
            if (!File(keyFileDirectory, KEY_FILE_NAME).isFile) {
                throw KeyLossException("The wrapped database passphrase is missing")
            }
            return it.copyOf()
        }
        try {
            val keyStore = KeyStore.getInstance(keystoreProviderName).apply { load(null) }
            val keyFile = File(keyFileDirectory, KEY_FILE_NAME)
            val passphrase = if (keyFile.exists()) {
                val wrappingKey = keyStore.getKey(alias, null) as? SecretKey
                    ?: throw KeyLossException("The database wrapping key is missing from Keystore")
                decrypt(keyFile, wrappingKey)
            } else {
                if (keyStore.containsAlias(alias) || hasExistingDatabase()) {
                    throw KeyLossException("The wrapped database passphrase is missing")
                }
                createAndStore()
            }
            try {
                if (passphrase.size != PASSPHRASE_SIZE) {
                    throw KeyLossException("Unexpected database passphrase size")
                }
                cachedPassphrase = passphrase.copyOf()
                return passphrase.copyOf()
            } finally {
                passphrase.fill(0)
            }
        } catch (loss: KeyLossException) {
            throw loss
        } catch (error: Exception) {
            throw KeyLossException("Could not recover the database passphrase", error)
        }
    }

    private fun createAndStore(): ByteArray {
        val wrappingKey = generateWrappingKey()
        val passphrase = ByteArray(PASSPHRASE_SIZE).also(SecureRandom()::nextBytes)
        var iv: ByteArray? = null
        var ciphertext: ByteArray? = null
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, wrappingKey)
            val generatedIv = cipher.iv
            iv = try {
                generatedIv.copyOf()
            } finally {
                generatedIv.fill(0)
            }
            ciphertext = cipher.doFinal(passphrase)
            writeAtomically(iv, ciphertext)
            return passphrase.copyOf()
        } catch (error: Exception) {
            throw KeyLossException("Could not create the wrapped database passphrase", error)
        } finally {
            passphrase.fill(0)
            iv?.fill(0)
            ciphertext?.fill(0)
        }
    }

    private fun decrypt(file: File, wrappingKey: SecretKey): ByteArray {
        val encoded = file.readBytes()
        var iv: ByteArray? = null
        var ciphertext: ByteArray? = null
        var plaintext: ByteArray? = null
        try {
            DataInputStream(ByteArrayInputStream(encoded)).use { input ->
                if (input.readInt() != FILE_MAGIC || input.readUnsignedByte() != FORMAT_VERSION) {
                    throw KeyLossException("The wrapped database passphrase file is invalid")
                }
                val ivSize = input.readUnsignedByte()
                if (ivSize != GCM_IV_SIZE) throw KeyLossException("Invalid database key IV")
                iv = ByteArray(ivSize).also(input::readFully)
                val ciphertextSize = input.readInt()
                if (ciphertextSize !in MIN_CIPHERTEXT_SIZE..MAX_CIPHERTEXT_SIZE) {
                    throw KeyLossException("Invalid wrapped database passphrase size")
                }
                ciphertext = ByteArray(ciphertextSize).also(input::readFully)
                if (input.available() != 0) throw KeyLossException("Unexpected data in database key file")
            }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val ivBytes = checkNotNull(iv)
            val ciphertextBytes = checkNotNull(ciphertext)
            cipher.init(Cipher.DECRYPT_MODE, wrappingKey, GCMParameterSpec(GCM_TAG_BITS, ivBytes))
            val cleartext = cipher.doFinal(ciphertextBytes)
            plaintext = cleartext
            if (cleartext.size != PASSPHRASE_SIZE) throw KeyLossException("Invalid database passphrase size")
            return cleartext.copyOf()
        } catch (loss: KeyLossException) {
            throw loss
        } catch (error: Exception) {
            throw KeyLossException("The database passphrase could not be decrypted", error)
        } finally {
            encoded.fill(0)
            iv?.fill(0)
            ciphertext?.fill(0)
            plaintext?.fill(0)
        }
    }

    private fun generateWrappingKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keystoreProviderName)
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKey()
    }

    private fun writeAtomically(iv: ByteArray, ciphertext: ByteArray) {
        if (!keyFileDirectory.exists() && !keyFileDirectory.mkdirs()) {
            throw IllegalStateException("Could not create database key directory")
        }
        val keyFile = File(keyFileDirectory, KEY_FILE_NAME)
        val temporaryFile = File(keyFileDirectory, "$KEY_FILE_NAME.tmp")
        val encoded = ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeInt(FILE_MAGIC)
                output.writeByte(FORMAT_VERSION)
                output.writeByte(iv.size)
                output.write(iv)
                output.writeInt(ciphertext.size)
                output.write(ciphertext)
            }
            bytes.toByteArray()
        }
        try {
            FileOutputStream(temporaryFile).use { output ->
                output.write(encoded)
                output.fd.sync()
            }
            Files.move(
                temporaryFile.toPath(),
                keyFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } finally {
            encoded.fill(0)
            if (temporaryFile.exists()) temporaryFile.delete()
        }
    }

    companion object {
        const val KEY_ALIAS = "mirror_db_key"
        const val KEY_FILE_NAME = "db.key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PASSPHRASE_SIZE = 32
        private const val GCM_IV_SIZE = 12
        private const val GCM_TAG_BITS = 128
        private const val FILE_MAGIC = 0x4D444B31 // MDK1
        private const val FORMAT_VERSION = 1
        private const val MIN_CIPHERTEXT_SIZE = PASSPHRASE_SIZE + 16
        private const val MAX_CIPHERTEXT_SIZE = 128
    }
}
