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

package org.ghostsinthelab.apps.guilelessbopomofo

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.ghostsinthelab.apps.guilelessbopomofo.IsolatedChewingData.Companion.LEGACY_DELETED
import org.ghostsinthelab.apps.guilelessbopomofo.IsolatedChewingData.Companion.LEGACY_PHRASE
import org.ghostsinthelab.apps.guilelessbopomofo.IsolatedChewingData.Companion.LEGACY_PHRASES
import org.ghostsinthelab.apps.guilelessbopomofo.IsolatedChewingData.Companion.MIGRATABLE_PHRASES
import org.ghostsinthelab.apps.guilelessbopomofo.IsolatedChewingData.Companion.USER_DICT
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Installing libchewing 0.14's dictionaries, and bringing over what users of 0.13 had
 * taught it, each test in a data directory of its own.
 */
@RunWith(AndroidJUnit4::class)
class ChewingDataMigrationInstrumentedTest {
    private lateinit var data: IsolatedChewingData

    @Before
    fun setUp() {
        data = IsolatedChewingData()
    }

    @After
    fun tearDown() {
        data.close()
    }

    private fun userPhrases(): List<String> = ChewingUtil.enumerateUserPhrases().map { it.phrase }

    // libchewing has its dictionaries, not the fallback it starts with when it cannot.
    private fun assertConvertsText() {
        assertTrue(data.file(USER_DICT).exists())
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        "xm4t86".forEach { ChewingBridge.chewing.handleDefault(it) }
        assertEquals("綠茶", ChewingBridge.chewing.bufferString())
        ChewingBridge.chewing.cleanPreeditBuf()
    }

    // region dictionaries

    @Test
    fun setup_installsEveryDictionary() {
        data.install()
        for (file in ChewingUtil.listOfDataFiles()) {
            assertTrue("$file should be installed", data.file(file).length() > 0)
        }
        assertTrue(ChewingUtil.chewingDataFilesInstalled(data.path))
    }

    @Test
    fun setup_removesTheDictionariesOfLibchewing013() {
        data.file("tsi.dat").writeText("old")
        data.file("word.dat").writeText("old")
        data.install()
        assertFalse(data.file("tsi.dat").exists())
        assertFalse(data.file("word.dat").exists())
    }

    @Test
    fun setup_recordsTheAppVersionAndLibchewingRevision() {
        data.install()
        assertEquals(
            "${BuildConfig.VERSION_NAME}+${BuildConfig.CHEWING_REVISION}",
            data.file(DATA_VERSION_FILE).readText(),
        )
    }

    @Test
    fun setup_reinstallsTheDictionaries_whenLibchewingChanged() {
        data.install()
        val languageModel = data.file("static_lm.bin")
        val installed = languageModel.readBytes()
        languageModel.writeText("stale")
        // What an install of the same app version with an older libchewing leaves behind.
        data.file(DATA_VERSION_FILE).writeText(BuildConfig.VERSION_NAME)

        data.install()
        assertArrayEquals(installed, languageModel.readBytes())
    }

    @Test
    fun setup_keepsTheDictionaries_whenNothingChanged() {
        data.install()
        val languageModel = data.file("static_lm.bin")
        languageModel.writeText("touched")
        data.install()
        assertEquals("touched", languageModel.readText())
    }

    // endregion

    // region migration from libchewing 0.13

    @Test
    fun theOldUserPhrases_areBroughtOver() {
        data.putLegacyUserData()
        data.installAndConnect()
        assertConvertsText()
        assertTrue(data.file(USER_DICT).readText().contains(LEGACY_PHRASE))
        assertTrue(LEGACY_PHRASE in userPhrases())
    }

    @Test
    fun theOldUserPhraseFile_isCopied_notMoved() {
        data.putLegacyUserData()
        data.install()
        assertTrue("$LEGACY_PHRASES stays for older builds", data.file(LEGACY_PHRASES).exists())
        assertArrayEquals(data.file(LEGACY_PHRASES).readBytes(), data.file(MIGRATABLE_PHRASES).readBytes())
    }

    @Test
    fun withoutTheDeletedPhraseFile_nothingIsMigrated_andLibchewingStillStarts() {
        data.putLegacyUserData(LEGACY_PHRASES)
        data.installAndConnect()
        assertFalse(data.file(MIGRATABLE_PHRASES).exists())
        assertConvertsText()
        assertFalse(LEGACY_PHRASE in userPhrases())
    }

    @Test
    fun once_libchewingHasItsOwnUserData_theOldPhrasesAreLeftAlone() {
        data.installAndConnect()
        ChewingBridge.chewing.delete()
        data.putLegacyUserData()

        data.installAndConnect()
        assertFalse(data.file(MIGRATABLE_PHRASES).exists())
        assertFalse(LEGACY_PHRASE in userPhrases())
    }

    @Test
    fun anUnreadableOldPhraseFile_stillLeavesAWorkingKeyboard() {
        data.putLegacyUserData(LEGACY_DELETED)
        data.file(LEGACY_PHRASES).writeText("not a dictionary")
        data.installAndConnect()
        assertConvertsText()
        assertTrue("the old file is kept", data.file(LEGACY_PHRASES).exists())
    }

    @Test
    fun anUnreadableDeletedPhraseFile_stillLeavesAWorkingKeyboard() {
        data.putLegacyUserData(LEGACY_PHRASES)
        data.file(LEGACY_DELETED).writeText("not a dictionary")
        data.installAndConnect()
        assertConvertsText()
    }

    @Test
    fun aMigratedPhrase_survivesReconnecting() {
        data.putLegacyUserData()
        data.installAndConnect()
        ChewingBridge.chewing.delete()
        data.installAndConnect()
        assertTrue(LEGACY_PHRASE in userPhrases())
    }

    // endregion

    // region reset

    @Test
    fun reset_throwsAwayTheNewAndTheOldUserData() {
        data.putLegacyUserData()
        data.installAndConnect()
        ChewingBridge.chewing.userphraseAdd("測試", "ㄘㄜˋ ㄕˋ")

        ChewingUtil.resetUserPhraseData(data.path)

        for (name in listOf(LEGACY_PHRASES, LEGACY_DELETED, MIGRATABLE_PHRASES)) {
            assertFalse("$name should be gone", data.file(name).exists())
        }
        assertTrue(userPhrases().isEmpty())
    }

    @Test
    fun reset_keepsTheDictionaries_andAWorkingContext() {
        data.installAndConnect()
        ChewingUtil.resetUserPhraseData(data.path)
        assertTrue(ChewingUtil.chewingDataFilesInstalled(data.path))
        assertTrue(ChewingBridge.chewing.context != 0L)
    }

    // endregion

    private companion object {
        const val DATA_VERSION_FILE = "data_appversion.txt"
    }
}
