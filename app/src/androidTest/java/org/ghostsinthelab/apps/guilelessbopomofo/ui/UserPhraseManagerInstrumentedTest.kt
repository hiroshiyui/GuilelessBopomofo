/*
 * Guileless Bopomofo
 * Copyright (C) 2025.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
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

package org.ghostsinthelab.apps.guilelessbopomofo.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingTestSetup
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.UserPhrase
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.UserPhraseManagerScreen
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * libchewing lists a user phrase twice while a change to it waits to be written over the copy
 * on disk, and the user phrase manager, which keys its list by phrase and Bopomofo, used to
 * crash on that with "Key ... was already used".
 */
@RunWith(AndroidJUnit4::class)
class UserPhraseManagerInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val phrase = UserPhrase("重複測試", "ㄔㄨㄥˊ ㄈㄨˋ ㄘㄜˋ ㄕˋ")

    @Before
    fun listThePhraseTwice() {
        ChewingTestSetup.connect(InstrumentationRegistry.getInstrumentation().targetContext)
        val chewing = ChewingBridge.chewing

        chewing.userphraseAdd(phrase.phrase, phrase.bopomofo)
        // Adding it again bumps its frequency, as using it does, and that change has the next
        // key write the user dictionary to disk, on a thread.
        chewing.userphraseAdd(phrase.phrase, phrase.bopomofo)
        chewing.handleEsc()
        Thread.sleep(DICTIONARY_WRITE_MILLIS)
        // One more bump, kept in memory; the next key reloads the dictionary just written,
        // which holds the phrase as well.
        chewing.userphraseAdd(phrase.phrase, phrase.bopomofo)
        chewing.handleEsc()
    }

    @After
    fun removeThePhrase() {
        ChewingBridge.chewing.userphraseRemove(phrase.phrase, phrase.bopomofo)
        ChewingBridge.chewing.handleEsc()
        Thread.sleep(DICTIONARY_WRITE_MILLIS)
        ChewingBridge.chewing.delete()
    }

    @Test
    fun libchewing_listsThePhraseTwice() {
        val listed = ChewingBridge.chewing.userphraseGetAll().orEmpty()
            .count { it.toList() == listOf(phrase.phrase, phrase.bopomofo) }
        assertEquals(2, listed)
    }

    @Test
    fun enumerateUserPhrases_listsThePhraseOnce() {
        assertEquals(1, ChewingUtil.enumerateUserPhrases().count { it == phrase })
    }

    @Test
    fun theManager_showsThePhraseOnce() {
        composeRule.setContent { GuilelessTheme { UserPhraseManagerScreen() } }
        composeRule.onAllNodesWithText(phrase.phrase).assertCountEquals(1)
    }

    private companion object {
        // the user dictionary is tiny, writing it takes a few milliseconds
        const val DICTIONARY_WRITE_MILLIS = 500L
    }
}
