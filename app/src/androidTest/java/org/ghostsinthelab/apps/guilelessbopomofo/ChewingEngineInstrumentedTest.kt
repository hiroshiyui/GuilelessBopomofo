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

// NOTICE: You have to manually enable Guileless Bopomofo from system settings first.
@RunWith(AndroidJUnit4::class)
class ChewingEngineInstrumentedTest {
    private lateinit var chewingEngine: ChewingEngine
    private lateinit var dataPath: String
    val CHINESE_MODE = 1

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

    @Test
    fun validCommitPhrase() {
        // ref: https://starforcefield.wordpress.com/2012/08/13/%E6%8E%A2%E7%B4%A2%E6%96%B0%E9%85%B7%E9%9F%B3%E8%BC%B8%E5%85%A5%E6%B3%95%EF%BC%9A%E4%BD%BF%E7%94%A8libchewing/
        chewingEngine.setChiEngMode(CHINESE_MODE)
        chewingEngine.setMaxChiSymbolLen(10)
        chewingEngine.setCandPerPage(9)
        chewingEngine.setPhraseChoiceRearward(false)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewingEngine.handleDefault(key)
        }
        chewingEngine.handleLeft()
        chewingEngine.handleLeft()
        chewingEngine.candOpen()
        chewingEngine.candTotalChoice()
        chewingEngine.candChooseByIndex(0)
        chewingEngine.commitPreeditBuf()
        var commitString: String = chewingEngine.commitString()
        assertEquals(commitString, "綠茶")

        chewingEngine.handleDefault('5')
        chewingEngine.handleSpace()
        chewingEngine.candOpen()
        chewingEngine.candTotalChoice()
        chewingEngine.candChooseByIndex(12)
        chewingEngine.commitPreeditBuf()
        commitString = chewingEngine.commitString()
        assertEquals(commitString, "蜘")
    }

    @Test
    fun testSetPhraseChoiceRearward() { // 後方選詞
        chewingEngine.setChiEngMode(CHINESE_MODE)
        chewingEngine.setMaxChiSymbolLen(10)
        chewingEngine.setCandPerPage(9)
        chewingEngine.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewingEngine.handleDefault(key)
        }
        chewingEngine.candOpen()
        chewingEngine.candTotalChoice()
        chewingEngine.candChooseByIndex(0)
        chewingEngine.commitPreeditBuf()
        var commitString: String = chewingEngine.commitString()
        assertEquals(commitString, "綠茶")
    }

    @Test
    fun validCommitPreeditBuf() { // 測試 commitPreeditBuf() 回傳值
        chewingEngine.setChiEngMode(CHINESE_MODE)
        chewingEngine.setMaxChiSymbolLen(10)
        chewingEngine.setCandPerPage(9)
        chewingEngine.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewingEngine.handleDefault(key)
        }
        chewingEngine.candOpen()
        chewingEngine.candTotalChoice()
        chewingEngine.candChooseByIndex(0)
        assertEquals(chewingEngine.commitPreeditBuf(), 0)
        assertEquals(chewingEngine.commitPreeditBuf(), -1)
    }

    @Test
    fun validMiddlePhraseCandidate() {
        chewingEngine.setChiEngMode(CHINESE_MODE)
        chewingEngine.setMaxChiSymbolLen(10)
        chewingEngine.setCandPerPage(9)
        chewingEngine.setPhraseChoiceRearward(false)
        // 密封膠帶 蜜蜂 交代 交待 蜂膠
        // ㄇ一ˋ
        chewingEngine.handleDefault('a')
        chewingEngine.handleDefault('u')
        chewingEngine.handleDefault('4')
        // ㄈㄥ
        chewingEngine.handleDefault('z')
        chewingEngine.handleDefault('/')
        chewingEngine.handleSpace()
        // ㄐㄧㄠ
        chewingEngine.handleDefault('r')
        chewingEngine.handleDefault('u')
        chewingEngine.handleDefault('l')
        chewingEngine.handleSpace()
        // ㄉㄞˋ
        chewingEngine.handleDefault('2')
        chewingEngine.handleDefault('9')
        chewingEngine.handleDefault('4')

        // 蜂膠
        chewingEngine.handleLeft()
        chewingEngine.handleLeft()
        chewingEngine.handleLeft()
        chewingEngine.candOpen()
        val candidateString: String = chewingEngine.candStringByIndexStatic(0);
        assertEquals(candidateString, "蜂膠")
        chewingEngine.candChooseByIndex(0)
        chewingEngine.commitPreeditBuf()
        val commitString: String = chewingEngine.commitString()
        assertEquals(commitString, "密蜂膠代")
    }

    @Test
    fun validCandListNext() {
        chewingEngine.setChiEngMode(CHINESE_MODE)
        chewingEngine.setMaxChiSymbolLen(10)
        chewingEngine.setCandPerPage(10)
        chewingEngine.setPhraseChoiceRearward(false)
        // 零用金 零用 零
        chewingEngine.handleDefault('x')
        chewingEngine.handleDefault('u')
        chewingEngine.handleDefault('/')
        chewingEngine.handleDefault('6')

        chewingEngine.handleDefault('m')
        chewingEngine.handleDefault('/')
        chewingEngine.handleDefault('4')

        chewingEngine.handleDefault('r')
        chewingEngine.handleDefault('u')
        chewingEngine.handleDefault('p')
        chewingEngine.handleSpace()

        chewingEngine.handleHome()
        chewingEngine.candOpen()

        assertEquals(chewingEngine.candStringByIndexStatic(0), "零用金")
        assertEquals(chewingEngine.candListHasNext(), true)
        assertEquals(chewingEngine.candListNext(), 0)

        assertEquals(chewingEngine.candStringByIndexStatic(0), "零用")
        assertEquals(chewingEngine.candListHasNext(), true)
        assertEquals(chewingEngine.candListNext(), 0)

        assertEquals(chewingEngine.candStringByIndexStatic(0), "零")
        assertEquals(chewingEngine.candListNext(), -1)

        chewingEngine.candListLast()
        assertEquals(chewingEngine.candStringByIndexStatic(0), "零")

        chewingEngine.candListFirst()
        assertEquals(chewingEngine.candStringByIndexStatic(0), "零用金")
    }

    @Test
    fun switchToHsuLayout() {
        val newKeyboardType = chewingEngine.convKBStr2Num("KB_HSU")
        chewingEngine.setKBType(newKeyboardType)
        val currentKeyboardType = chewingEngine.getKBType()
        val currentKeyboardTypeString = chewingEngine.getKBString()
        assertEquals(currentKeyboardType, 1)
        assertEquals(currentKeyboardTypeString, "KB_HSU")

        chewingEngine.handleDefault('l')
        chewingEngine.handleDefault('l')
        assertEquals(chewingEngine.bopomofoStringStatic(), "ㄌㄥ")
        chewingEngine.handleDefault('f')
        assertEquals(chewingEngine.bufferString(), "冷")
        chewingEngine.handleDefault('d')
        chewingEngine.handleDefault('x')
        chewingEngine.handleDefault('l')
        chewingEngine.handleDefault('j')
        chewingEngine.commitPreeditBuf()
        assertEquals(chewingEngine.commitString(), "冷凍")
    }

    @Test
    fun switchToEten26Layout() {
        val newKeyboardType = chewingEngine.convKBStr2Num("KB_ET26")
        chewingEngine.setKBType(newKeyboardType)
        val currentKeyboardType = chewingEngine.getKBType()
        val currentKeyboardTypeString = chewingEngine.getKBString()
        assertEquals(currentKeyboardType, 5)
        assertEquals(currentKeyboardTypeString, "KB_ET26")

        chewingEngine.handleDefault('l')
        chewingEngine.handleDefault('l')
        assertEquals(chewingEngine.bopomofoStringStatic(), "ㄌㄥ")
        chewingEngine.handleDefault('j')
        assertEquals(chewingEngine.bufferString(), "冷")
        chewingEngine.handleDefault('d')
        chewingEngine.handleDefault('x')
        chewingEngine.handleDefault('l')
        chewingEngine.handleDefault('k')
        chewingEngine.commitPreeditBuf()
        assertEquals(chewingEngine.commitString(), "冷凍")
    }

    @After
    fun deleteChewingEngine() {
        chewingEngine.delete()
    }
}