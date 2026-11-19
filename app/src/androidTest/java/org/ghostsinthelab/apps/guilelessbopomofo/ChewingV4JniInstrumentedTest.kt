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
import org.ghostsinthelab.apps.guilelessbopomofo.IsolatedChewingData.Companion.HISTORY_DICT
import org.ghostsinthelab.apps.guilelessbopomofo.IsolatedChewingData.Companion.USER_DICT
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The JNI bridge to libchewing 0.14: opening a context, and the options 0.14 brought. */
@RunWith(AndroidJUnit4::class)
class ChewingV4JniInstrumentedTest {
    private lateinit var data: IsolatedChewingData

    @Before
    fun setUp() {
        data = IsolatedChewingData()
    }

    @After
    fun tearDown() {
        data.close()
    }

    // region connect

    // libchewing 0.14's own behaviour, which ChewingUtil.connect() works around: should this
    // start failing, libchewing reports a bad start itself and the workaround can go.
    @Test
    fun libchewing_withoutDictionaries_fallsBackSilently() {
        ChewingBridge.chewing.connect(data.path)
        assertTrue("no error from libchewing", ChewingBridge.chewing.context != 0L)
        assertFalse("but no user data either", data.file(USER_DICT).exists())
    }

    @Test
    fun connect_withoutDictionaries_throwsAndLeavesNoContext() {
        assertThrows(Chewing.ChewingInitException::class.java) { ChewingUtil.connect(data.path) }
        assertEquals(0L, ChewingBridge.chewing.context)
    }

    @Test
    fun connect_toAPathGoingUp_throwsAndLeavesNoContext() {
        data.install()
        assertThrows(Chewing.ChewingInitException::class.java) {
            ChewingBridge.chewing.connect("${data.path}/../${data.dir.name}")
        }
        assertEquals(0L, ChewingBridge.chewing.context)
    }

    @Test
    fun connect_toAnEmptyPath_throws() {
        assertThrows(Chewing.ChewingInitException::class.java) { ChewingBridge.chewing.connect("") }
    }

    @Test
    fun connect_keepsTheUserDataInV4UnderTheDataDirectory() {
        data.installAndConnect()
        assertTrue(ChewingBridge.chewing.context != 0L)
        assertTrue("$USER_DICT should be created", data.file(USER_DICT).isFile)
        assertTrue("$HISTORY_DICT should be created", data.file(HISTORY_DICT).isFile)
    }

    @Test
    fun aFailedConnect_afterAGoodOne_dropsTheOldContextToo() {
        data.installAndConnect()
        assertThrows(Chewing.ChewingInitException::class.java) { ChewingBridge.chewing.connect("") }
        assertEquals(0L, ChewingBridge.chewing.context)
    }

    // endregion

    // region options new in libchewing 0.14

    @Test
    fun theNewOptions_areKnown() {
        data.installAndConnect()
        for (option in NEW_OPTIONS.keys) {
            assertEquals("$option should be known", 1, ChewingBridge.chewing.configHasOption(option))
        }
    }

    @Test
    fun theNewOptions_startAtLibchewingsDefaults() {
        data.installAndConnect()
        for ((option, default) in NEW_OPTIONS) {
            assertEquals(option, default, ChewingBridge.chewing.configGetInt(option))
        }
    }

    @Test
    fun theNewOptions_keepWhatIsSet() {
        data.installAndConnect()
        val values = mapOf(
            "chewing.auto_learn_user_history" to 0,
            "chewing.auto_learn_new_words" to 1,
            "chewing.cycle_first_n_selects" to 3,
            "chewing.auto_wrap_cursor" to 1,
        )
        for ((option, value) in values) {
            ChewingBridge.chewing.configSetInt(option, value)
            assertEquals(option, value, ChewingBridge.chewing.configGetInt(option))
        }
    }

    @Test
    fun aFreshContext_forgetsTheOptionsOfTheLastOne() {
        data.installAndConnect()
        ChewingBridge.chewing.configSetInt("chewing.cycle_first_n_selects", 4)
        ChewingBridge.chewing.connect(data.path)
        assertEquals(0, ChewingBridge.chewing.configGetInt("chewing.cycle_first_n_selects"))
    }

    // endregion

    private companion object {
        // Each option libchewing 0.14 added, and its default.
        val NEW_OPTIONS = mapOf(
            "chewing.auto_learn_user_history" to 1,
            "chewing.auto_learn_new_words" to 0,
            "chewing.cycle_first_n_selects" to 0,
            "chewing.auto_wrap_cursor" to 0,
        )
    }
}
