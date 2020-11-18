/*
 * Guileless Bopomofo
 * Copyright (C) 2020 YOU, HUI-HONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChewingEngineInstrumentedTest {
    private lateinit var chewingEngine: ChewingEngine
    private lateinit var dataPath: String

    @Before
    fun setupChewingEngine() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        dataPath = appContext.dataDir.absolutePath
        chewingEngine = ChewingEngine(dataPath)
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.ghostsinthelab.apps.guilelessbopomofo", appContext.packageName)
    }

    @Test
    fun validDataPath() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertThat(dataPath, CoreMatchers.containsString(appContext.packageName))
    }

    @Test
    fun validChiEngMode() {
        val CHINESE_MODE = 1
        chewingEngine.setChiEngMode(CHINESE_MODE)
        val chewing_chi_mode = chewingEngine.getChiEngMode()
        assertEquals(chewing_chi_mode, CHINESE_MODE)
    }

    @Test
    fun validSelKeys() {
        val selKeys = arrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9').map { it.toInt() }
        chewingEngine.setSelKey(selKeys, 9)
        val listKeys = chewingEngine.getSelKey()
        // listKeys should be a pointer address, which should not be zero:
        assertNotEquals(listKeys, 0)
        chewingEngine.free(listKeys)
        // TODO: 可能需要一個給 Android 這邊用的 listKeys
    }

    @Test
    fun validMaxChiSymbolLen() {
        chewingEngine.setMaxChiSymbolLen(10)
        assertEquals(chewingEngine.getMaxChiSymbolLen(), 10)
    }

    @Test
    fun validCandPerPage() {
        chewingEngine.setCandPerPage(9)
        assertEquals(chewingEngine.getCandPerPage(), 9)
    }

    @Test
    fun validPhraseChoiceRearward() {
        chewingEngine.setPhraseChoiceRearward(true)
        assertTrue(chewingEngine.getPhraseChoiceRearward())
        chewingEngine.setPhraseChoiceRearward(false)
        assertFalse(chewingEngine.getPhraseChoiceRearward())
    }

    @After
    fun deleteChewingEngine() {
        chewingEngine.delete()
    }
}