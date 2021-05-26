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
class ChewingBridgeInstrumentedTest {
    private lateinit var dataPath: String

    @Before
    fun setupChewingEngine() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        dataPath = appContext.dataDir.absolutePath
        ChewingBridge.connect(dataPath)
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
        ChewingBridge.setChiEngMode(SYMBOL_MODE)
        assertEquals(ChewingBridge.getChiEngMode(), SYMBOL_MODE)

        ChewingBridge.handleDefault('t')
        ChewingBridge.handleDefault('e')
        ChewingBridge.handleDefault('a')
        // 如果一開始 pre-edit buffer 完全無資料，SYMBOL_MODE 會直接送出字符
        assertEquals(ChewingBridge.bufferStringStatic(), "")

        ChewingBridge.setChiEngMode(CHINESE_MODE)
        assertEquals(ChewingBridge.getChiEngMode(), CHINESE_MODE)

        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.handleDefault(key)
        }

        ChewingBridge.setChiEngMode(SYMBOL_MODE)
        assertEquals(ChewingBridge.getChiEngMode(), SYMBOL_MODE)

        ChewingBridge.handleSpace()
        ChewingBridge.handleDefault('g')
        ChewingBridge.handleDefault('r')
        ChewingBridge.handleDefault('e')
        ChewingBridge.handleDefault('e')
        ChewingBridge.handleDefault('n')
        ChewingBridge.handleSpace()
        ChewingBridge.handleDefault('t')
        ChewingBridge.handleDefault('e')
        ChewingBridge.handleDefault('a')
        assertEquals(ChewingBridge.bufferStringStatic(), "綠茶 green tea")
    }

    @Test
    fun validSelKeys() {
        ChewingBridge.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';').map { it.code }
                .toIntArray()
        ChewingBridge.setSelKey(selKeys, 10)
        val getSelKey = ChewingBridge.getSelKey()
        assertNotEquals(getSelKey[0], '1'.code)
        assertEquals(getSelKey[0], 'a'.code)
    }

    @Test
    fun validOpenPuncCandidates() {
        ChewingBridge.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.setSelKey(selKeys, 10)
        ChewingUtil.openPuncCandidates()
        assertEquals(ChewingUtil.candWindowOpened(), true)
        val candidateString: String = ChewingBridge.candStringByIndexStatic(0)
        assertEquals(candidateString, "，")
    }

    @Test
    fun validPagedCandidates() {
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.setSelKey(selKeys, 10)
        ChewingBridge.setPhraseChoiceRearward(false)

        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleDefault('6')

        ChewingBridge.handleDefault('m')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleDefault('4')

        ChewingBridge.handleDefault('r')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('p')
        ChewingBridge.handleSpace()

        ChewingBridge.handleHome()
        assertEquals(ChewingBridge.cursorCurrent(), 0)
        ChewingBridge.handleRight()
        assertEquals(ChewingBridge.cursorCurrent(), 1)
        ChewingBridge.handleRight()
        assertEquals(ChewingBridge.cursorCurrent(), 2)
        ChewingBridge.handleLeft()
        assertEquals(ChewingBridge.cursorCurrent(), 1)
        ChewingBridge.handleLeft()
        assertEquals(ChewingBridge.cursorCurrent(), 0)

        ChewingBridge.candOpen()
        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(ChewingBridge.candTotalPage(), 1)
        assertEquals(ChewingBridge.candTotalChoice(), 1)
        assertEquals(ChewingBridge.candCurrentPage(), 0)
        assertEquals(ChewingBridge.candChoicePerPage(), 10)
        assertEquals(ChewingBridge.candListHasNext(), true)

        ChewingBridge.candEnumerate()
        assertEquals(ChewingBridge.candStringStatic(), "零用金")
        // ChewingEngine.candHasNext() will point to the next item in candidates enumerator
        assertEquals(ChewingBridge.candHasNext(), 0)

        ChewingBridge.candListNext()

        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(ChewingBridge.candTotalPage(), 1)
        assertEquals(ChewingBridge.candTotalChoice(), 1)
        assertEquals(ChewingBridge.candCurrentPage(), 0)
        assertEquals(ChewingBridge.candChoicePerPage(), 10)
        assertEquals(ChewingBridge.candListHasNext(), true)

        ChewingBridge.candEnumerate()
        assertEquals(ChewingBridge.candStringStatic(), "零用")
        assertEquals(ChewingBridge.candHasNext(), 0)

        ChewingBridge.candListNext()

        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(ChewingBridge.candTotalPage(), 9)
        assertEquals(ChewingBridge.candTotalChoice(), 88)
        assertEquals(ChewingBridge.candCurrentPage(), 0)
        assertEquals(ChewingBridge.candChoicePerPage(), 10)
        assertEquals(ChewingBridge.candListHasNext(), false)

        // loop the candidates list
        ChewingBridge.candEnumerate()
        assertEquals(ChewingBridge.candStringStatic(), "零")
        assertEquals(ChewingBridge.candHasNext(), 1)
        assertEquals(ChewingBridge.candStringStatic(), "玲")
        assertEquals(ChewingBridge.candHasNext(), 1)
        assertEquals(ChewingBridge.candStringStatic(), "靈")

        // switch to next page
        ChewingBridge.handlePageDown()
        assertEquals(ChewingBridge.candTotalPage(), 9)
        assertEquals(ChewingBridge.candCurrentPage(), 1)
        ChewingBridge.candEnumerate()
        assertEquals(ChewingBridge.candStringStatic(), "苓")
        assertEquals(ChewingBridge.candHasNext(), 1)
        assertEquals(ChewingBridge.candStringStatic(), "伶")

        ChewingBridge.handleEsc() // should have similar effect as ChewingEngine.candClose() does
        assertEquals(ChewingUtil.candWindowClosed(), true)
    }

    @Test
    fun validPhysicalKeyboardCandidatesSelection() {
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.setSelKey(selKeys, 10)
        ChewingBridge.setPhraseChoiceRearward(false)
        ChewingBridge.setSpaceAsSelection(1)

        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleDefault('6')
        ChewingBridge.handleSpace()
        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(ChewingBridge.candTotalPage(), 9)
        assertEquals(ChewingBridge.candTotalChoice(), 88)
        assertEquals(ChewingBridge.candCurrentPage(), 0)
        assertEquals(ChewingBridge.candChoicePerPage(), 10)
        assertEquals(ChewingBridge.candListHasNext(), false)

        // switch to next page
        ChewingBridge.handlePageDown()
        assertEquals(ChewingBridge.candCurrentPage(), 1)

        ChewingBridge.candEnumerate()
        assertEquals(ChewingBridge.candStringStatic(), "苓")
        assertEquals(ChewingBridge.candHasNext(), 1)
        assertEquals(ChewingBridge.candStringStatic(), "伶")

        ChewingBridge.handleDefault('2')
        assertEquals(ChewingBridge.bufferStringStatic(), "伶")
    }

    @Test
    fun validMaxChiSymbolLen() {
        ChewingBridge.setMaxChiSymbolLen(10)
        assertEquals(ChewingBridge.getMaxChiSymbolLen(), 10)
    }

    @Test
    fun validCandPerPage() {
        ChewingBridge.setCandPerPage(9)
        assertEquals(ChewingBridge.getCandPerPage(), 9)
    }

    @Test
    fun validPhraseChoiceRearward() {
        ChewingBridge.setPhraseChoiceRearward(true)
        assertTrue(ChewingBridge.getPhraseChoiceRearward())
        ChewingBridge.setPhraseChoiceRearward(false)
        assertFalse(ChewingBridge.getPhraseChoiceRearward())
    }

    @Test
    fun validCommitPhrase() {
        // ref: https://starforcefield.wordpress.com/2012/08/13/%E6%8E%A2%E7%B4%A2%E6%96%B0%E9%85%B7%E9%9F%B3%E8%BC%B8%E5%85%A5%E6%B3%95%EF%BC%9A%E4%BD%BF%E7%94%A8libchewing/
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setMaxChiSymbolLen(10)
        ChewingBridge.setCandPerPage(9)
        ChewingBridge.setPhraseChoiceRearward(false)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.handleDefault(key)
        }
        ChewingBridge.handleLeft()
        ChewingBridge.handleLeft()
        ChewingBridge.candOpen()
        ChewingBridge.candTotalChoice()
        ChewingBridge.candChooseByIndex(0)
        ChewingBridge.commitPreeditBuf()
        var commitString: String = ChewingBridge.commitString()
        assertEquals(commitString, "綠茶")

        ChewingBridge.handleDefault('5')
        ChewingBridge.handleSpace()
        ChewingBridge.candOpen()
        ChewingBridge.candTotalChoice()
        ChewingBridge.candChooseByIndex(12)
        ChewingBridge.commitPreeditBuf()
        commitString = ChewingBridge.commitString()
        assertEquals(commitString, "蜘")
    }

    @Test
    fun testSetPhraseChoiceRearward() { // 後方選詞
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setMaxChiSymbolLen(10)
        ChewingBridge.setCandPerPage(9)
        ChewingBridge.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.handleDefault(key)
        }
        ChewingBridge.candOpen()
        ChewingBridge.candTotalChoice()
        ChewingBridge.candChooseByIndex(0)
        ChewingBridge.commitPreeditBuf()
        val commitString: String = ChewingBridge.commitString()
        assertEquals(commitString, "綠茶")
    }

    @Test
    fun validGetCandidatesByPage() {
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('a', 's', 'd', 'f', 'g', 'q', 'w', 'e', 'r', 't').map { it.code }
                .toIntArray()
        ChewingBridge.setSelKey(selKeys, 10)
        ChewingBridge.setPhraseChoiceRearward(false)
        ChewingBridge.setSpaceAsSelection(1)

        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleDefault('6')
        ChewingBridge.handleSpace()

        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(ChewingBridge.candTotalPage(), 9)
        assertEquals(ChewingBridge.candTotalChoice(), 88)
        assertEquals(ChewingBridge.candCurrentPage(), 0)
        assertEquals(ChewingBridge.candChoicePerPage(), 10)
        assertEquals(ChewingBridge.candListHasNext(), false)

        var candidates = ChewingUtil.getCandidatesByPage(0)
        assertEquals(candidates[0].index, 0)
        assertEquals(candidates[0].candidateString, "零")
        assertEquals(candidates[0].selectionKey, 'a')

        candidates = ChewingUtil.getCandidatesByPage(1)
        assertEquals(candidates[0].index, 10)
        assertEquals(candidates[0].candidateString, "苓")
        assertEquals(candidates[0].selectionKey, 'a')
        assertEquals(candidates[1].index, 11)
        assertEquals(candidates[1].candidateString, "伶")
        assertEquals(candidates[1].selectionKey, 's')

        // last page
        candidates = ChewingUtil.getCandidatesByPage(8)
        assertEquals(candidates[0].index, 80)
        assertEquals(candidates[0].candidateString, "衑")
        assertEquals(candidates[0].selectionKey, 'a')

        assertEquals(candidates[1].index, 81)
        assertEquals(candidates[1].candidateString, "閝")
        assertEquals(candidates[1].selectionKey, 's')
        // bounding
        assertEquals(candidates.lastIndex, 7)
        assertEquals(candidates[7].selectionKey, 'e')

    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun validIndexOutOfBoundsExceptionGetCandidatesByPage() {
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.setSelKey(selKeys, 10)
        ChewingBridge.setPhraseChoiceRearward(false)
        ChewingBridge.setSpaceAsSelection(1)

        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleDefault('6')
        ChewingBridge.handleSpace()

        // last page
        val candidates = ChewingUtil.getCandidatesByPage(8)
        assertNotNull(candidates[7])
        // over bounding, should throws IndexOutOfBoundsException here:
        assertNull(candidates[8])
    }

    @Test
    fun validCommitPreeditBuf() { // 測試 commitPreeditBuf() 回傳值
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setMaxChiSymbolLen(10)
        ChewingBridge.setCandPerPage(9)
        ChewingBridge.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.handleDefault(key)
        }
        ChewingBridge.candOpen()
        ChewingBridge.candTotalChoice()
        ChewingBridge.candChooseByIndex(0)
        assertEquals(ChewingBridge.commitPreeditBuf(), 0)
        assertEquals(ChewingBridge.commitPreeditBuf(), -1)
    }

    @Test
    fun validMiddlePhraseCandidate() {
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setMaxChiSymbolLen(10)
        ChewingBridge.setCandPerPage(9)
        ChewingBridge.setPhraseChoiceRearward(false)
        // 密封膠帶 蜜蜂 交代 交待 蜂膠
        // ㄇ一ˋ
        ChewingBridge.handleDefault('a')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('4')
        // ㄈㄥ
        ChewingBridge.handleDefault('z')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleSpace()
        // ㄐㄧㄠ
        ChewingBridge.handleDefault('r')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('l')
        ChewingBridge.handleSpace()
        // ㄉㄞˋ
        ChewingBridge.handleDefault('2')
        ChewingBridge.handleDefault('9')
        ChewingBridge.handleDefault('4')

        // 蜂膠
        ChewingBridge.handleLeft()
        ChewingBridge.handleLeft()
        ChewingBridge.handleLeft()
        ChewingBridge.candOpen()
        val candidateString: String = ChewingBridge.candStringByIndexStatic(0)
        assertEquals(candidateString, "蜂膠")
        ChewingBridge.candChooseByIndex(0)
        ChewingBridge.commitPreeditBuf()
        val commitString: String = ChewingBridge.commitString()
        assertEquals(commitString, "密蜂膠代")
        assertEquals(ChewingBridge.candClose(), 0)
    }

    @Test
    fun validCandListNext() {
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setMaxChiSymbolLen(10)
        ChewingBridge.setCandPerPage(10)
        ChewingBridge.setPhraseChoiceRearward(false)
        // 零用金 零用 零
        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleDefault('6')

        ChewingBridge.handleDefault('m')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleDefault('4')

        ChewingBridge.handleDefault('r')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('p')
        ChewingBridge.handleSpace()

        ChewingBridge.handleHome()
        ChewingBridge.candOpen()

        assertEquals(ChewingBridge.candStringByIndexStatic(0), "零用金")
        assertEquals(ChewingBridge.candListHasNext(), true)
        assertEquals(ChewingBridge.candListNext(), 0)
        assertEquals(ChewingBridge.candStringByIndexStatic(0), "零用")
        assertEquals(ChewingBridge.candListHasNext(), true)
        assertEquals(ChewingBridge.candListNext(), 0)
        assertEquals(ChewingBridge.candStringByIndexStatic(0), "零")
        assertEquals(ChewingBridge.candListNext(), -1)
        assertEquals(ChewingBridge.candListHasNext(), false)

        ChewingBridge.candListLast()
        assertEquals(ChewingBridge.candStringByIndexStatic(0), "零")

        ChewingBridge.candListFirst()
        assertEquals(ChewingBridge.candStringByIndexStatic(0), "零用金")
        assertEquals(ChewingBridge.candClose(), 0)
    }

    @Test
    fun switchToHsuLayout() {
        val newKeyboardType = ChewingBridge.convKBStr2Num("KB_HSU")
        ChewingBridge.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.getKBType()
        val currentKeyboardTypeString = ChewingBridge.getKBString()
        assertEquals(currentKeyboardType, 1)
        assertEquals(currentKeyboardTypeString, "KB_HSU")

        ChewingBridge.handleDefault('l')
        ChewingBridge.handleDefault('l')
        assertEquals(ChewingBridge.bopomofoStringStatic(), "ㄌㄥ")
        ChewingBridge.handleDefault('f')
        assertEquals(ChewingBridge.bufferString(), "冷")
        ChewingBridge.handleDefault('d')
        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('l')
        ChewingBridge.handleDefault('j')
        ChewingBridge.commitPreeditBuf()
        assertEquals(ChewingBridge.commitString(), "冷凍")
    }

    @Test
    fun switchToDvorakHsuLayout() {
        val newKeyboardType = ChewingBridge.convKBStr2Num("KB_DVORAK_HSU")
        ChewingBridge.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.getKBType()
        val currentKeyboardTypeString = ChewingBridge.getKBString()
        assertEquals(currentKeyboardType, 7)
        assertEquals(currentKeyboardTypeString, "KB_DVORAK_HSU")

        ChewingBridge.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        ChewingBridge.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals(ChewingBridge.bopomofoStringStatic(), "ㄌㄥ")
        ChewingBridge.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('f'))
        assertEquals(ChewingBridge.bufferString(), "冷")
        ChewingBridge.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('d'))
        ChewingBridge.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('x'))
        ChewingBridge.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals(ChewingBridge.bopomofoStringStatic(), "ㄉㄨㄥ")
        ChewingBridge.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('j'))
        ChewingBridge.commitPreeditBuf()
        assertEquals(ChewingBridge.commitString(), "冷凍")
    }

    @Test
    fun validCandidateWindowOpenClose() {
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setMaxChiSymbolLen(10)
        ChewingBridge.setCandPerPage(10)
        ChewingBridge.setPhraseChoiceRearward(false)
        ChewingBridge.setSpaceAsSelection(1)

        // 零
        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('u')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleDefault('6')

        ChewingBridge.handleHome()
        // candidate window opened here
        ChewingBridge.handleSpace()
        assertTrue(ChewingBridge.candTotalChoice() > 0)
        assertEquals(ChewingUtil.candWindowOpened(), true)
        // candidate window closed here (after I picker the first candidate)
        ChewingBridge.handleDefault('1')
        assertEquals(ChewingBridge.candTotalChoice(), 0)
        assertEquals(ChewingUtil.candWindowClosed(), true)
    }

    @Test
    fun switchToEten26Layout() {
        val newKeyboardType = ChewingBridge.convKBStr2Num("KB_ET26")
        ChewingBridge.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.getKBType()
        val currentKeyboardTypeString = ChewingBridge.getKBString()
        assertEquals(currentKeyboardType, 5)
        assertEquals(currentKeyboardTypeString, "KB_ET26")

        ChewingBridge.handleDefault('l')
        ChewingBridge.handleDefault('l')
        assertEquals(ChewingBridge.bopomofoStringStatic(), "ㄌㄥ")
        ChewingBridge.handleDefault('j')
        assertEquals(ChewingBridge.bufferString(), "冷")
        ChewingBridge.handleDefault('d')
        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('l')
        ChewingBridge.handleDefault('k')
        ChewingBridge.commitPreeditBuf()
        assertEquals(ChewingBridge.commitString(), "冷凍")
    }

    @Test
    fun switchToDaChenLayout() {
        val newKeyboardType = ChewingBridge.convKBStr2Num("KB_DEFAULT")
        ChewingBridge.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.getKBType()
        val currentKeyboardTypeString = ChewingBridge.getKBString()
        assertEquals(currentKeyboardType, 0)
        assertEquals(currentKeyboardTypeString, "KB_DEFAULT")

        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('/')
        assertEquals(ChewingBridge.bopomofoStringStatic(), "ㄌㄥ")
        ChewingBridge.handleDefault('3')
        assertEquals(ChewingBridge.bufferString(), "冷")
        ChewingBridge.handleDefault('2')
        ChewingBridge.handleDefault('j')
        ChewingBridge.handleDefault('/')
        ChewingBridge.handleDefault('4')
        ChewingBridge.commitPreeditBuf()
        assertEquals(ChewingBridge.commitString(), "冷凍")
    }

    @Test
    fun switchToSymbolSelectionMode() {
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setMaxChiSymbolLen(10)
        ChewingBridge.setCandPerPage(10)
        ChewingBridge.setPhraseChoiceRearward(false)
        ChewingBridge.handleDefault('`')
        ChewingBridge.candOpen()
        assertEquals(ChewingBridge.candTotalChoice(), 22)
        assertEquals(ChewingBridge.candStringByIndexStatic(0), "…")
        assertEquals(ChewingBridge.candStringByIndexStatic(1), "※")
        assertEquals(ChewingBridge.candStringByIndexStatic(2), "常用符號")
        assertEquals(ChewingBridge.candStringByIndexStatic(10), "雙線框")
        assertEquals(ChewingBridge.candStringByIndexStatic(12), "線段")
        ChewingBridge.handleDefault('1')
        ChewingBridge.commitPreeditBuf()
        assertEquals(ChewingBridge.commitString(), "…")

        // 換頁到「雙線框」
        // keyboardless API 版
        ChewingBridge.handleDefault('`')
        ChewingBridge.candChooseByIndex(10)
        assertEquals(ChewingBridge.candTotalChoice(), 29)
        assertEquals(ChewingBridge.candStringByIndexStatic(0), "╔")
        ChewingBridge.candChooseByIndex(0)
        ChewingBridge.commitPreeditBuf()
        assertEquals(ChewingBridge.commitString(), "╔")

        // 模擬鍵盤操作版
        ChewingBridge.handleDefault('`')
        ChewingBridge.handleSpace()
        ChewingBridge.handleDefault('1')
        assertEquals(ChewingBridge.candTotalChoice(), 29)
        ChewingBridge.handleDefault('1')
        ChewingBridge.commitPreeditBuf()
        ChewingBridge.candClose()
        assertEquals(ChewingBridge.commitString(), "╔")

        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.setSelKey(selKeys, 10)

        ChewingBridge.handleDefault('`')
        ChewingBridge.handleDefault('3')
        ChewingBridge.handleDefault('1')
        ChewingBridge.commitPreeditBuf()
        assertEquals(ChewingBridge.commitString(), "，")

        ChewingBridge.setSpaceAsSelection(1)
        ChewingBridge.handleDefault('1')
        ChewingBridge.handleDefault('l')
        ChewingBridge.handleDefault('3')
        ChewingBridge.handleHome()
        ChewingBridge.handleSpace()
        ChewingBridge.handleDefault('3')
        ChewingBridge.commitPreeditBuf()
        assertEquals(ChewingBridge.commitString(), "飽")
    }

    @Test
    fun testCommitCheck() {
        ChewingBridge.setChiEngMode(CHINESE_MODE)
        ChewingBridge.setMaxChiSymbolLen(10)
        ChewingBridge.setCandPerPage(10)
        ChewingBridge.setPhraseChoiceRearward(false)
        val newKeyboardType = ChewingBridge.convKBStr2Num("KB_HSU")
        ChewingBridge.setKBType(newKeyboardType)

        ChewingBridge.handleDefault('l')
        ChewingBridge.handleDefault('w')
        ChewingBridge.handleDefault('f')
        ChewingBridge.handleDefault('c')
        ChewingBridge.handleDefault('x')
        ChewingBridge.handleDefault('f')

        repeat(4) {
            ChewingBridge.handleDefault('m')
            ChewingBridge.handleDefault('w')
            ChewingBridge.handleSpace()
            ChewingBridge.handleDefault('m')
            ChewingBridge.handleDefault('e')
            ChewingBridge.handleSpace()
        }

        ChewingBridge.handleDefault('m')
        ChewingBridge.handleDefault('w')
        ChewingBridge.handleSpace() // 此時應該觸發送出最前端詞「老鼠」
        assertEquals(ChewingBridge.commitCheck(), 1)

        ChewingBridge.handleDefault('m')
        ChewingBridge.handleDefault('e')
        ChewingBridge.handleSpace()

        assertEquals(ChewingBridge.commitStringStatic(), "老鼠")
        assertEquals(ChewingBridge.bufferStringStatic(), "貓咪貓咪貓咪貓咪貓咪")
        assertEquals(ChewingBridge.commitCheck(), 0)
        ChewingBridge.commitPreeditBuf()
        assertEquals(ChewingBridge.commitCheck(), 1)
    }

    @After
    fun deleteChewingEngine() {
        ChewingBridge.delete()
    }
}