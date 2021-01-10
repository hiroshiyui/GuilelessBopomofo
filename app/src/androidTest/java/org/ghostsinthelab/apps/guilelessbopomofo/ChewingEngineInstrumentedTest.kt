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
    private lateinit var dataPath: String

    @Before
    fun setupChewingEngine() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        dataPath = appContext.dataDir.absolutePath
        ChewingEngine.start(dataPath)
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
        ChewingEngine.setChiEngMode(SYMBOL_MODE)
        assertEquals(ChewingEngine.getChiEngMode(), SYMBOL_MODE)

        ChewingEngine.handleDefault('t')
        ChewingEngine.handleDefault('e')
        ChewingEngine.handleDefault('a')
        // 如果一開始 pre-edit buffer 完全無資料，SYMBOL_MODE 會直接送出字符
        assertEquals(ChewingEngine.bufferStringStatic(), "")

        ChewingEngine.setChiEngMode(CHINESE_MODE)
        assertEquals(ChewingEngine.getChiEngMode(), CHINESE_MODE)

        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingEngine.handleDefault(key)
        }

        ChewingEngine.setChiEngMode(SYMBOL_MODE)
        assertEquals(ChewingEngine.getChiEngMode(), SYMBOL_MODE)

        ChewingEngine.handleSpace()
        ChewingEngine.handleDefault('g')
        ChewingEngine.handleDefault('r')
        ChewingEngine.handleDefault('e')
        ChewingEngine.handleDefault('e')
        ChewingEngine.handleDefault('n')
        ChewingEngine.handleSpace()
        ChewingEngine.handleDefault('t')
        ChewingEngine.handleDefault('e')
        ChewingEngine.handleDefault('a')
        assertEquals(ChewingEngine.bufferStringStatic(), "綠茶 green tea")
    }

    @Test
    fun validSelKeys() {
        val selKeys = charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9').map { it.toInt() }.toIntArray()
        ChewingEngine.setSelKey(selKeys, 9)
        val listKeys = ChewingEngine.getSelKey()
        // listKeys should be a pointer address, which should not be zero:
        assertNotEquals(listKeys, 0)
        ChewingEngine.free(listKeys)
        // TODO: 可能需要一個給 Android 這邊用的 listKeys
    }

    @Test
    fun validMaxChiSymbolLen() {
        ChewingEngine.setMaxChiSymbolLen(10)
        assertEquals(ChewingEngine.getMaxChiSymbolLen(), 10)
    }

    @Test
    fun validCandPerPage() {
        ChewingEngine.setCandPerPage(9)
        assertEquals(ChewingEngine.getCandPerPage(), 9)
    }

    @Test
    fun validPhraseChoiceRearward() {
        ChewingEngine.setPhraseChoiceRearward(true)
        assertTrue(ChewingEngine.getPhraseChoiceRearward())
        ChewingEngine.setPhraseChoiceRearward(false)
        assertFalse(ChewingEngine.getPhraseChoiceRearward())
    }

    @Test
    fun validCommitPhrase() {
        // ref: https://starforcefield.wordpress.com/2012/08/13/%E6%8E%A2%E7%B4%A2%E6%96%B0%E9%85%B7%E9%9F%B3%E8%BC%B8%E5%85%A5%E6%B3%95%EF%BC%9A%E4%BD%BF%E7%94%A8libchewing/
        ChewingEngine.setChiEngMode(CHINESE_MODE)
        ChewingEngine.setMaxChiSymbolLen(10)
        ChewingEngine.setCandPerPage(9)
        ChewingEngine.setPhraseChoiceRearward(false)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingEngine.handleDefault(key)
        }
        ChewingEngine.handleLeft()
        ChewingEngine.handleLeft()
        ChewingEngine.candOpen()
        ChewingEngine.candTotalChoice()
        ChewingEngine.candChooseByIndex(0)
        ChewingEngine.commitPreeditBuf()
        var commitString: String = ChewingEngine.commitString()
        assertEquals(commitString, "綠茶")

        ChewingEngine.handleDefault('5')
        ChewingEngine.handleSpace()
        ChewingEngine.candOpen()
        ChewingEngine.candTotalChoice()
        ChewingEngine.candChooseByIndex(12)
        ChewingEngine.commitPreeditBuf()
        commitString = ChewingEngine.commitString()
        assertEquals(commitString, "蜘")
    }

    @Test
    fun testSetPhraseChoiceRearward() { // 後方選詞
        ChewingEngine.setChiEngMode(CHINESE_MODE)
        ChewingEngine.setMaxChiSymbolLen(10)
        ChewingEngine.setCandPerPage(9)
        ChewingEngine.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingEngine.handleDefault(key)
        }
        ChewingEngine.candOpen()
        ChewingEngine.candTotalChoice()
        ChewingEngine.candChooseByIndex(0)
        ChewingEngine.commitPreeditBuf()
        val commitString: String = ChewingEngine.commitString()
        assertEquals(commitString, "綠茶")
    }

    @Test
    fun validCommitPreeditBuf() { // 測試 commitPreeditBuf() 回傳值
        ChewingEngine.setChiEngMode(CHINESE_MODE)
        ChewingEngine.setMaxChiSymbolLen(10)
        ChewingEngine.setCandPerPage(9)
        ChewingEngine.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingEngine.handleDefault(key)
        }
        ChewingEngine.candOpen()
        ChewingEngine.candTotalChoice()
        ChewingEngine.candChooseByIndex(0)
        assertEquals(ChewingEngine.commitPreeditBuf(), 0)
        assertEquals(ChewingEngine.commitPreeditBuf(), -1)
    }

    @Test
    fun validMiddlePhraseCandidate() {
        ChewingEngine.setChiEngMode(CHINESE_MODE)
        ChewingEngine.setMaxChiSymbolLen(10)
        ChewingEngine.setCandPerPage(9)
        ChewingEngine.setPhraseChoiceRearward(false)
        // 密封膠帶 蜜蜂 交代 交待 蜂膠
        // ㄇ一ˋ
        ChewingEngine.handleDefault('a')
        ChewingEngine.handleDefault('u')
        ChewingEngine.handleDefault('4')
        // ㄈㄥ
        ChewingEngine.handleDefault('z')
        ChewingEngine.handleDefault('/')
        ChewingEngine.handleSpace()
        // ㄐㄧㄠ
        ChewingEngine.handleDefault('r')
        ChewingEngine.handleDefault('u')
        ChewingEngine.handleDefault('l')
        ChewingEngine.handleSpace()
        // ㄉㄞˋ
        ChewingEngine.handleDefault('2')
        ChewingEngine.handleDefault('9')
        ChewingEngine.handleDefault('4')

        // 蜂膠
        ChewingEngine.handleLeft()
        ChewingEngine.handleLeft()
        ChewingEngine.handleLeft()
        ChewingEngine.candOpen()
        val candidateString: String = ChewingEngine.candStringByIndexStatic(0)
        assertEquals(candidateString, "蜂膠")
        ChewingEngine.candChooseByIndex(0)
        ChewingEngine.commitPreeditBuf()
        val commitString: String = ChewingEngine.commitString()
        assertEquals(commitString, "密蜂膠代")
        assertEquals(ChewingEngine.candClose(), 0)
    }

    @Test
    fun validCandListNext() {
        ChewingEngine.setChiEngMode(CHINESE_MODE)
        ChewingEngine.setMaxChiSymbolLen(10)
        ChewingEngine.setCandPerPage(10)
        ChewingEngine.setPhraseChoiceRearward(false)
        // 零用金 零用 零
        ChewingEngine.handleDefault('x')
        ChewingEngine.handleDefault('u')
        ChewingEngine.handleDefault('/')
        ChewingEngine.handleDefault('6')

        ChewingEngine.handleDefault('m')
        ChewingEngine.handleDefault('/')
        ChewingEngine.handleDefault('4')

        ChewingEngine.handleDefault('r')
        ChewingEngine.handleDefault('u')
        ChewingEngine.handleDefault('p')
        ChewingEngine.handleSpace()

        ChewingEngine.handleHome()
        ChewingEngine.candOpen()

        assertEquals(ChewingEngine.candStringByIndexStatic(0), "零用金")
        assertEquals(ChewingEngine.candListHasNext(), true)
        assertEquals(ChewingEngine.candListNext(), 0)
        assertEquals(ChewingEngine.candStringByIndexStatic(0), "零用")
        assertEquals(ChewingEngine.candListHasNext(), true)
        assertEquals(ChewingEngine.candListNext(), 0)
        assertEquals(ChewingEngine.candStringByIndexStatic(0), "零")
        assertEquals(ChewingEngine.candListNext(), -1)
        assertEquals(ChewingEngine.candListHasNext(), false)

        ChewingEngine.candListLast()
        assertEquals(ChewingEngine.candStringByIndexStatic(0), "零")

        ChewingEngine.candListFirst()
        assertEquals(ChewingEngine.candStringByIndexStatic(0), "零用金")
        assertEquals(ChewingEngine.candClose(), 0)
    }

    @Test
    fun switchToHsuLayout() {
        val newKeyboardType = ChewingEngine.convKBStr2Num("KB_HSU")
        ChewingEngine.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingEngine.getKBType()
        val currentKeyboardTypeString = ChewingEngine.getKBString()
        assertEquals(currentKeyboardType, 1)
        assertEquals(currentKeyboardTypeString, "KB_HSU")

        ChewingEngine.handleDefault('l')
        ChewingEngine.handleDefault('l')
        assertEquals(ChewingEngine.bopomofoStringStatic(), "ㄌㄥ")
        ChewingEngine.handleDefault('f')
        assertEquals(ChewingEngine.bufferString(), "冷")
        ChewingEngine.handleDefault('d')
        ChewingEngine.handleDefault('x')
        ChewingEngine.handleDefault('l')
        ChewingEngine.handleDefault('j')
        ChewingEngine.commitPreeditBuf()
        assertEquals(ChewingEngine.commitString(), "冷凍")
    }

    @Test
    fun switchToEten26Layout() {
        val newKeyboardType = ChewingEngine.convKBStr2Num("KB_ET26")
        ChewingEngine.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingEngine.getKBType()
        val currentKeyboardTypeString = ChewingEngine.getKBString()
        assertEquals(currentKeyboardType, 5)
        assertEquals(currentKeyboardTypeString, "KB_ET26")

        ChewingEngine.handleDefault('l')
        ChewingEngine.handleDefault('l')
        assertEquals(ChewingEngine.bopomofoStringStatic(), "ㄌㄥ")
        ChewingEngine.handleDefault('j')
        assertEquals(ChewingEngine.bufferString(), "冷")
        ChewingEngine.handleDefault('d')
        ChewingEngine.handleDefault('x')
        ChewingEngine.handleDefault('l')
        ChewingEngine.handleDefault('k')
        ChewingEngine.commitPreeditBuf()
        assertEquals(ChewingEngine.commitString(), "冷凍")
    }

    @Test
    fun switchToDaChenLayout() {
        val newKeyboardType = ChewingEngine.convKBStr2Num("KB_DEFAULT")
        ChewingEngine.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingEngine.getKBType()
        val currentKeyboardTypeString = ChewingEngine.getKBString()
        assertEquals(currentKeyboardType, 0)
        assertEquals(currentKeyboardTypeString, "KB_DEFAULT")

        ChewingEngine.handleDefault('x')
        ChewingEngine.handleDefault('/')
        assertEquals(ChewingEngine.bopomofoStringStatic(), "ㄌㄥ")
        ChewingEngine.handleDefault('3')
        assertEquals(ChewingEngine.bufferString(), "冷")
        ChewingEngine.handleDefault('2')
        ChewingEngine.handleDefault('j')
        ChewingEngine.handleDefault('/')
        ChewingEngine.handleDefault('4')
        ChewingEngine.commitPreeditBuf()
        assertEquals(ChewingEngine.commitString(), "冷凍")
    }

    @Test
    fun switchToSymbolSelectionMode() {
        ChewingEngine.setChiEngMode(CHINESE_MODE)
        ChewingEngine.setMaxChiSymbolLen(10)
        ChewingEngine.setCandPerPage(10)
        ChewingEngine.setPhraseChoiceRearward(false)
        ChewingEngine.handleDefault('`')
        ChewingEngine.candOpen()
        assertEquals(ChewingEngine.candTotalChoice(), 13)
        assertEquals(ChewingEngine.candStringByIndexStatic(0), "…")
        assertEquals(ChewingEngine.candStringByIndexStatic(1), "※")
        assertEquals(ChewingEngine.candStringByIndexStatic(2), "常用符號")
        assertEquals(ChewingEngine.candStringByIndexStatic(10), "雙線框")
        assertEquals(ChewingEngine.candStringByIndexStatic(12), "線段")
        ChewingEngine.handleDefault('1')
        ChewingEngine.commitPreeditBuf()
        assertEquals(ChewingEngine.commitString(), "…")

        // 換頁到「雙線框」
        // keyboardless API 版
        ChewingEngine.handleDefault('`')
        ChewingEngine.candChooseByIndex(10)
        assertEquals(ChewingEngine.candTotalChoice(), 29)
        assertEquals(ChewingEngine.candStringByIndexStatic(0), "╔")
        ChewingEngine.candChooseByIndex(0)
        ChewingEngine.commitPreeditBuf()
        assertEquals(ChewingEngine.commitString(), "╔")

        // 模擬鍵盤操作版
        ChewingEngine.handleDefault('`')
        ChewingEngine.handleSpace()
        ChewingEngine.handleDefault('1')
        assertEquals(ChewingEngine.candTotalChoice(), 29)
        ChewingEngine.handleDefault('1')
        ChewingEngine.commitPreeditBuf()
        ChewingEngine.candClose()
        assertEquals(ChewingEngine.commitString(), "╔")
    }

    @Test
    fun testCommitCheck() {
        ChewingEngine.setChiEngMode(CHINESE_MODE)
        ChewingEngine.setMaxChiSymbolLen(10)
        ChewingEngine.setCandPerPage(10)
        ChewingEngine.setPhraseChoiceRearward(false)
        val newKeyboardType = ChewingEngine.convKBStr2Num("KB_HSU")
        ChewingEngine.setKBType(newKeyboardType)

        ChewingEngine.handleDefault('l')
        ChewingEngine.handleDefault('w')
        ChewingEngine.handleDefault('f')
        ChewingEngine.handleDefault('c')
        ChewingEngine.handleDefault('x')
        ChewingEngine.handleDefault('f')

        repeat(4) {
            ChewingEngine.handleDefault('m')
            ChewingEngine.handleDefault('w')
            ChewingEngine.handleSpace()
            ChewingEngine.handleDefault('m')
            ChewingEngine.handleDefault('e')
            ChewingEngine.handleSpace()
        }

        ChewingEngine.handleDefault('m')
        ChewingEngine.handleDefault('w')
        ChewingEngine.handleSpace() // 此時應該觸發送出最前端詞「老鼠」
        assertEquals(ChewingEngine.commitCheck(), 1)

        ChewingEngine.handleDefault('m')
        ChewingEngine.handleDefault('e')
        ChewingEngine.handleSpace()

        assertEquals(ChewingEngine.commitStringStatic(), "老鼠")
        assertEquals(ChewingEngine.bufferStringStatic(), "貓咪貓咪貓咪貓咪貓咪")
        assertEquals(ChewingEngine.commitCheck(), 0)
        ChewingEngine.commitPreeditBuf()
        assertEquals(ChewingEngine.commitCheck(), 1)
    }

    @After
    fun deleteChewingEngine() {
        ChewingEngine.delete()
    }
}