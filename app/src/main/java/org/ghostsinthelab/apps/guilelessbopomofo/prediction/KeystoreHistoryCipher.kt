/*
 * Guileless Bopomofo
 * Copyright (C) 2026.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.ghostsinthelab.apps.guilelessbopomofo.prediction

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM with a key that lives in the Android Keystore, hardware-backed where the device
 * has it: the key never leaves the Keystore, so a copy of the file alone cannot be read
 * anywhere else, and once [destroyKey] has run it cannot be read at all.
 *
 * A sealed history is `version | IV length | IV | ciphertext and tag`, the version byte also
 * authenticated along with the ciphertext.
 */
class KeystoreHistoryCipher(private val alias: String = DEFAULT_ALIAS) : HistoryCipher {

    override fun encrypt(plain: ByteArray): ByteArray = keystoreCall {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // the Keystore picks a fresh random IV every time
        cipher.init(Cipher.ENCRYPT_MODE, key())
        cipher.updateAAD(HEADER)
        val iv = cipher.iv
        val sealed = cipher.doFinal(plain)
        HEADER + byteArrayOf(iv.size.toByte()) + iv + sealed
    }

    override fun decrypt(sealed: ByteArray): ByteArray = keystoreCall {
        if (sealed.size < HEADER.size + 1 || sealed[0] != VERSION) throw GeneralSecurityException("Unknown word history format")
        val ivLength = sealed[HEADER.size].toInt()
        val ivStart = HEADER.size + 1
        if (ivLength !in MIN_IV_LENGTH..MAX_IV_LENGTH || sealed.size < ivStart + ivLength + TAG_LENGTH_BYTES) {
            throw GeneralSecurityException("Malformed word history")
        }
        val key = existingKey() ?: throw GeneralSecurityException("The word history key is gone")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, sealed, ivStart, ivLength))
        cipher.updateAAD(HEADER)
        cipher.doFinal(sealed, ivStart + ivLength, sealed.size - ivStart - ivLength)
    }

    override fun destroyKey() = keystoreCall {
        val keyStore = keyStore()
        if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias)
    }

    // Some Keystore implementations fail with an unchecked ProviderException; it is reported
    // like any other security failure, rather than bring the input method down.
    private inline fun <T> keystoreCall(block: () -> T): T =
        try {
            block()
        } catch (exception: ProviderException) {
            throw GeneralSecurityException("The Android Keystore failed", exception)
        }

    private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun existingKey(): SecretKey? = keyStore().getKey(alias, null) as? SecretKey

    private fun key(): SecretKey = existingKey() ?: generateKey()

    private fun generateKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        const val DEFAULT_ALIAS = "english_word_history"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE_BITS = 256
        private const val TAG_LENGTH_BITS = 128
        private const val TAG_LENGTH_BYTES = TAG_LENGTH_BITS / 8
        private const val MIN_IV_LENGTH = 12
        private const val MAX_IV_LENGTH = 16
        private const val VERSION: Byte = 1
        private val HEADER = byteArrayOf(VERSION)
    }
}
