package com.threeloops.mirrorarchive.security

import java.nio.charset.StandardCharsets

/** Temporary R-10a database key; replace with a Keystore-backed key in R-10b. */
internal object TemporaryDatabasePassphrase {
    // TEMPORARY only. TODO(R-10b): derive/load this key from Android Keystore.
    fun bytes(): ByteArray = "MirrorArchive-R10a-TEMPORARY-PASSPHRASE".toByteArray(StandardCharsets.UTF_8)
}
