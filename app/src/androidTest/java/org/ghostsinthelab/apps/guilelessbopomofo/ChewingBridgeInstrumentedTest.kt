/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
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
class ChewingInstrumentedTest {
    private lateinit var dataPath: String

    @Before
    fun setupChewingEngine() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        dataPath = appContext.dataDir.absolutePath
        Chewing.connect(dataPath)
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
        Chewing.setChiEngMode(SYMBOL_MODE)
        assertEquals(Chewing.getChiEngMode(), SYMBOL_MODE)

        Chewing.handleDefault('t')
        Chewing.handleDefault('e')
        Chewing.handleDefault('a')
        // 如果一開始 pre-edit buffer 完全無資料，SYMBOL_MODE 會直接送出字符
        assertEquals(Chewing.bufferStringStatic(), "")

        Chewing.setChiEngMode(CHINESE_MODE)
        assertEquals(Chewing.getChiEngMode(), CHINESE_MODE)

        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            Chewing.handleDefault(key)
        }

        Chewing.setChiEngMode(SYMBOL_MODE)
        assertEquals(Chewing.getChiEngMode(), SYMBOL_MODE)

        Chewing.handleSpace()
        Chewing.handleDefault('g')
        Chewing.handleDefault('r')
        Chewing.handleDefault('e')
        Chewing.handleDefault('e')
        Chewing.handleDefault('n')
        Chewing.handleSpace()
        Chewing.handleDefault('t')
        Chewing.handleDefault('e')
        Chewing.handleDefault('a')
        assertEquals(Chewing.bufferStringStatic(), "綠茶 green tea")
    }

    @Test
    fun validSelKeys() {
        Chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';').map { it.code }
                .toIntArray()
        Chewing.setSelKey(selKeys, 10)
        val getSelKey = Chewing.getSelKey()
        assertNotEquals(getSelKey[0], '1'.code)
        assertEquals(getSelKey[0], 'a'.code)
    }

    @Test
    fun validOpenPuncCandidates() {
        Chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        Chewing.setSelKey(selKeys, 10)
        ChewingUtil.openPuncCandidates()
        assertEquals(ChewingUtil.candWindowOpened(), true)
        val candidateString: String = Chewing.candStringByIndexStatic(0)
        assertEquals(candidateString, "，")
    }

    @Test
    fun validPagedCandidates() {
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        Chewing.setSelKey(selKeys, 10)
        Chewing.setPhraseChoiceRearward(false)

        Chewing.handleDefault('x')
        Chewing.handleDefault('u')
        Chewing.handleDefault('/')
        Chewing.handleDefault('6')

        Chewing.handleDefault('m')
        Chewing.handleDefault('/')
        Chewing.handleDefault('4')

        Chewing.handleDefault('r')
        Chewing.handleDefault('u')
        Chewing.handleDefault('p')
        Chewing.handleSpace()

        Chewing.handleHome()
        assertEquals(Chewing.cursorCurrent(), 0)
        Chewing.handleRight()
        assertEquals(Chewing.cursorCurrent(), 1)
        Chewing.handleRight()
        assertEquals(Chewing.cursorCurrent(), 2)
        Chewing.handleLeft()
        assertEquals(Chewing.cursorCurrent(), 1)
        Chewing.handleLeft()
        assertEquals(Chewing.cursorCurrent(), 0)

        Chewing.candOpen()
        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(Chewing.candTotalPage(), 1)
        assertEquals(Chewing.candTotalChoice(), 1)
        assertEquals(Chewing.candCurrentPage(), 0)
        assertEquals(Chewing.candChoicePerPage(), 10)
        assertEquals(Chewing.candListHasNext(), true)

        Chewing.candEnumerate()
        assertEquals(Chewing.candStringStatic(), "零用金")
        // ChewingEngine.candHasNext() will point to the next item in candidates enumerator
        assertEquals(Chewing.candHasNext(), 0)

        Chewing.candListNext()

        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(Chewing.candTotalPage(), 1)
        assertEquals(Chewing.candTotalChoice(), 1)
        assertEquals(Chewing.candCurrentPage(), 0)
        assertEquals(Chewing.candChoicePerPage(), 10)
        assertEquals(Chewing.candListHasNext(), true)

        Chewing.candEnumerate()
        assertEquals(Chewing.candStringStatic(), "零用")
        assertEquals(Chewing.candHasNext(), 0)

        Chewing.candListNext()

        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(Chewing.candTotalPage(), 9)
        assertEquals(Chewing.candTotalChoice(), 88)
        assertEquals(Chewing.candCurrentPage(), 0)
        assertEquals(Chewing.candChoicePerPage(), 10)
        assertEquals(Chewing.candListHasNext(), false)

        // loop the candidates list
        Chewing.candEnumerate()
        assertEquals(Chewing.candStringStatic(), "零")
        assertEquals(Chewing.candHasNext(), 1)
        assertEquals(Chewing.candStringStatic(), "玲")
        assertEquals(Chewing.candHasNext(), 1)
        assertEquals(Chewing.candStringStatic(), "靈")

        // switch to next page
        Chewing.handlePageDown()
        assertEquals(Chewing.candTotalPage(), 9)
        assertEquals(Chewing.candCurrentPage(), 1)
        Chewing.candEnumerate()
        assertEquals(Chewing.candStringStatic(), "苓")
        assertEquals(Chewing.candHasNext(), 1)
        assertEquals(Chewing.candStringStatic(), "伶")

        Chewing.handleEsc() // should have similar effect as ChewingEngine.candClose() does
        assertEquals(ChewingUtil.candWindowClosed(), true)
    }

    @Test
    fun validPhysicalKeyboardCandidatesSelection() {
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        Chewing.setSelKey(selKeys, 10)
        Chewing.setPhraseChoiceRearward(false)
        Chewing.setSpaceAsSelection(1)

        Chewing.handleDefault('x')
        Chewing.handleDefault('u')
        Chewing.handleDefault('/')
        Chewing.handleDefault('6')
        Chewing.handleSpace()
        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(Chewing.candTotalPage(), 9)
        assertEquals(Chewing.candTotalChoice(), 88)
        assertEquals(Chewing.candCurrentPage(), 0)
        assertEquals(Chewing.candChoicePerPage(), 10)
        assertEquals(Chewing.candListHasNext(), false)

        // switch to next page
        Chewing.handlePageDown()
        assertEquals(Chewing.candCurrentPage(), 1)

        Chewing.candEnumerate()
        assertEquals(Chewing.candStringStatic(), "苓")
        assertEquals(Chewing.candHasNext(), 1)
        assertEquals(Chewing.candStringStatic(), "伶")

        Chewing.handleDefault('2')
        assertEquals(Chewing.bufferStringStatic(), "伶")
    }

    @Test
    fun validMaxChiSymbolLen() {
        Chewing.setMaxChiSymbolLen(10)
        assertEquals(Chewing.getMaxChiSymbolLen(), 10)
    }

    @Test
    fun validCandPerPage() {
        Chewing.setCandPerPage(9)
        assertEquals(Chewing.getCandPerPage(), 9)
    }

    @Test
    fun validPhraseChoiceRearward() {
        Chewing.setPhraseChoiceRearward(true)
        assertTrue(Chewing.getPhraseChoiceRearward())
        Chewing.setPhraseChoiceRearward(false)
        assertFalse(Chewing.getPhraseChoiceRearward())
    }

    @Test
    fun validCommitPhrase() {
        // ref: https://starforcefield.wordpress.com/2012/08/13/%E6%8E%A2%E7%B4%A2%E6%96%B0%E9%85%B7%E9%9F%B3%E8%BC%B8%E5%85%A5%E6%B3%95%EF%BC%9A%E4%BD%BF%E7%94%A8libchewing/
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setMaxChiSymbolLen(10)
        Chewing.setCandPerPage(9)
        Chewing.setPhraseChoiceRearward(false)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            Chewing.handleDefault(key)
        }
        Chewing.handleLeft()
        Chewing.handleLeft()
        Chewing.candOpen()
        Chewing.candTotalChoice()
        Chewing.candChooseByIndex(0)
        Chewing.commitPreeditBuf()
        var commitString: String = Chewing.commitString()
        assertEquals(commitString, "綠茶")

        Chewing.handleDefault('5')
        Chewing.handleSpace()
        Chewing.candOpen()
        Chewing.candTotalChoice()
        Chewing.candChooseByIndex(12)
        Chewing.commitPreeditBuf()
        commitString = Chewing.commitString()
        assertEquals(commitString, "蜘")
    }

    @Test
    fun testSetPhraseChoiceRearward() { // 後方選詞
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setMaxChiSymbolLen(10)
        Chewing.setCandPerPage(9)
        Chewing.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            Chewing.handleDefault(key)
        }
        Chewing.candOpen()
        Chewing.candTotalChoice()
        Chewing.candChooseByIndex(0)
        Chewing.commitPreeditBuf()
        val commitString: String = Chewing.commitString()
        assertEquals(commitString, "綠茶")
    }

    @Test
    fun validGetCandidatesByPage() {
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('a', 's', 'd', 'f', 'g', 'q', 'w', 'e', 'r', 't').map { it.code }
                .toIntArray()
        Chewing.setSelKey(selKeys, 10)
        Chewing.setPhraseChoiceRearward(false)
        Chewing.setSpaceAsSelection(1)

        Chewing.handleDefault('x')
        Chewing.handleDefault('u')
        Chewing.handleDefault('/')
        Chewing.handleDefault('6')
        Chewing.handleSpace()

        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(Chewing.candTotalPage(), 9)
        assertEquals(Chewing.candTotalChoice(), 88)
        assertEquals(Chewing.candCurrentPage(), 0)
        assertEquals(Chewing.candChoicePerPage(), 10)
        assertEquals(Chewing.candListHasNext(), false)

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
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        Chewing.setSelKey(selKeys, 10)
        Chewing.setPhraseChoiceRearward(false)
        Chewing.setSpaceAsSelection(1)

        Chewing.handleDefault('x')
        Chewing.handleDefault('u')
        Chewing.handleDefault('/')
        Chewing.handleDefault('6')
        Chewing.handleSpace()

        // last page
        val candidates = ChewingUtil.getCandidatesByPage(8)
        assertNotNull(candidates[7])
        // over bounding, should throws IndexOutOfBoundsException here:
        assertNull(candidates[8])
    }

    @Test
    fun validCommitPreeditBuf() { // 測試 commitPreeditBuf() 回傳值
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setMaxChiSymbolLen(10)
        Chewing.setCandPerPage(9)
        Chewing.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            Chewing.handleDefault(key)
        }
        Chewing.candOpen()
        Chewing.candTotalChoice()
        Chewing.candChooseByIndex(0)
        assertEquals(Chewing.commitPreeditBuf(), 0)
        assertEquals(Chewing.commitPreeditBuf(), -1)
    }

    @Test
    fun validMiddlePhraseCandidate() {
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setMaxChiSymbolLen(10)
        Chewing.setCandPerPage(9)
        Chewing.setPhraseChoiceRearward(false)
        // 密封膠帶 蜜蜂 交代 交待 蜂膠
        // ㄇ一ˋ
        Chewing.handleDefault('a')
        Chewing.handleDefault('u')
        Chewing.handleDefault('4')
        // ㄈㄥ
        Chewing.handleDefault('z')
        Chewing.handleDefault('/')
        Chewing.handleSpace()
        // ㄐㄧㄠ
        Chewing.handleDefault('r')
        Chewing.handleDefault('u')
        Chewing.handleDefault('l')
        Chewing.handleSpace()
        // ㄉㄞˋ
        Chewing.handleDefault('2')
        Chewing.handleDefault('9')
        Chewing.handleDefault('4')

        // 蜂膠
        Chewing.handleLeft()
        Chewing.handleLeft()
        Chewing.handleLeft()
        Chewing.candOpen()
        val candidateString: String = Chewing.candStringByIndexStatic(0)
        assertEquals(candidateString, "蜂膠")
        Chewing.candChooseByIndex(0)
        Chewing.commitPreeditBuf()
        val commitString: String = Chewing.commitString()
        assertEquals(commitString, "密蜂膠代")
        assertEquals(Chewing.candClose(), 0)
    }

    @Test
    fun validCandListNext() {
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setMaxChiSymbolLen(10)
        Chewing.setCandPerPage(10)
        Chewing.setPhraseChoiceRearward(false)
        // 零用金 零用 零
        Chewing.handleDefault('x')
        Chewing.handleDefault('u')
        Chewing.handleDefault('/')
        Chewing.handleDefault('6')

        Chewing.handleDefault('m')
        Chewing.handleDefault('/')
        Chewing.handleDefault('4')

        Chewing.handleDefault('r')
        Chewing.handleDefault('u')
        Chewing.handleDefault('p')
        Chewing.handleSpace()

        Chewing.handleHome()
        Chewing.candOpen()

        assertEquals(Chewing.candStringByIndexStatic(0), "零用金")
        assertEquals(Chewing.candListHasNext(), true)
        assertEquals(Chewing.candListNext(), 0)
        assertEquals(Chewing.candStringByIndexStatic(0), "零用")
        assertEquals(Chewing.candListHasNext(), true)
        assertEquals(Chewing.candListNext(), 0)
        assertEquals(Chewing.candStringByIndexStatic(0), "零")
        assertEquals(Chewing.candListNext(), -1)
        assertEquals(Chewing.candListHasNext(), false)

        Chewing.candListLast()
        assertEquals(Chewing.candStringByIndexStatic(0), "零")

        Chewing.candListFirst()
        assertEquals(Chewing.candStringByIndexStatic(0), "零用金")
        assertEquals(Chewing.candClose(), 0)
    }

    @Test
    fun switchToHsuLayout() {
        val newKeyboardType = Chewing.convKBStr2Num("KB_HSU")
        Chewing.setKBType(newKeyboardType)
        val currentKeyboardType = Chewing.getKBType()
        val currentKeyboardTypeString = Chewing.getKBString()
        assertEquals(currentKeyboardType, 1)
        assertEquals(currentKeyboardTypeString, "KB_HSU")

        Chewing.handleDefault('l')
        Chewing.handleDefault('l')
        assertEquals(Chewing.bopomofoStringStatic(), "ㄌㄥ")
        Chewing.handleDefault('f')
        assertEquals(Chewing.bufferString(), "冷")
        Chewing.handleDefault('d')
        Chewing.handleDefault('x')
        Chewing.handleDefault('l')
        Chewing.handleDefault('j')
        Chewing.commitPreeditBuf()
        assertEquals(Chewing.commitString(), "冷凍")
    }

    @Test
    fun switchToDvorakHsuLayout() {
        val newKeyboardType = Chewing.convKBStr2Num("KB_DVORAK_HSU")
        Chewing.setKBType(newKeyboardType)
        val currentKeyboardType = Chewing.getKBType()
        val currentKeyboardTypeString = Chewing.getKBString()
        assertEquals(currentKeyboardType, 7)
        assertEquals(currentKeyboardTypeString, "KB_DVORAK_HSU")

        // test ChewingUtil.dvorakToQwertyKeyMapping()
        assertEquals(ChewingUtil.dvorakToQwertyKeyMapping('j'), 'c')
        assertEquals(ChewingUtil.dvorakToQwertyKeyMapping('l'), 'p')
        assertEquals(ChewingUtil.dvorakToQwertyKeyMapping('1'), '1')
        assertEquals(ChewingUtil.dvorakToQwertyKeyMapping('!'), '!')

        Chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        Chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals(Chewing.bopomofoStringStatic(), "ㄌㄥ")
        // ˇ
        Chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('f'))
        assertEquals(Chewing.bufferString(), "冷")
        Chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('d'))
        Chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('x'))
        Chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals(Chewing.bopomofoStringStatic(), "ㄉㄨㄥ")
        // ˋ
        Chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('j'))
        Chewing.commitPreeditBuf()
        assertEquals(Chewing.commitString(), "冷凍")
    }

    @Test
    fun validCandidateWindowOpenClose() {
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setMaxChiSymbolLen(10)
        Chewing.setCandPerPage(10)
        Chewing.setPhraseChoiceRearward(false)
        Chewing.setSpaceAsSelection(1)

        // 零
        Chewing.handleDefault('x')
        Chewing.handleDefault('u')
        Chewing.handleDefault('/')
        Chewing.handleDefault('6')

        Chewing.handleHome()
        // candidate window opened here
        Chewing.handleSpace()
        assertTrue(Chewing.candTotalChoice() > 0)
        assertEquals(ChewingUtil.candWindowOpened(), true)
        // candidate window closed here (after I picker the first candidate)
        Chewing.handleDefault('1')
        assertEquals(Chewing.candTotalChoice(), 0)
        assertEquals(ChewingUtil.candWindowClosed(), true)
    }

    @Test
    fun switchToEten26Layout() {
        val newKeyboardType = Chewing.convKBStr2Num("KB_ET26")
        Chewing.setKBType(newKeyboardType)
        val currentKeyboardType = Chewing.getKBType()
        val currentKeyboardTypeString = Chewing.getKBString()
        assertEquals(currentKeyboardType, 5)
        assertEquals(currentKeyboardTypeString, "KB_ET26")

        Chewing.handleDefault('l')
        Chewing.handleDefault('l')
        assertEquals(Chewing.bopomofoStringStatic(), "ㄌㄥ")
        Chewing.handleDefault('j')
        assertEquals(Chewing.bufferString(), "冷")
        Chewing.handleDefault('d')
        Chewing.handleDefault('x')
        Chewing.handleDefault('l')
        Chewing.handleDefault('k')
        Chewing.commitPreeditBuf()
        assertEquals(Chewing.commitString(), "冷凍")
    }

    @Test
    fun switchToDaChenLayout() {
        val newKeyboardType = Chewing.convKBStr2Num("KB_DEFAULT")
        Chewing.setKBType(newKeyboardType)
        val currentKeyboardType = Chewing.getKBType()
        val currentKeyboardTypeString = Chewing.getKBString()
        assertEquals(currentKeyboardType, 0)
        assertEquals(currentKeyboardTypeString, "KB_DEFAULT")

        Chewing.handleDefault('x')
        Chewing.handleDefault('/')
        assertEquals(Chewing.bopomofoStringStatic(), "ㄌㄥ")
        Chewing.handleDefault('3')
        assertEquals(Chewing.bufferString(), "冷")
        Chewing.handleDefault('2')
        Chewing.handleDefault('j')
        Chewing.handleDefault('/')
        Chewing.handleDefault('4')
        Chewing.commitPreeditBuf()
        assertEquals(Chewing.commitString(), "冷凍")
    }

    @Test
    fun switchToSymbolSelectionMode() {
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setMaxChiSymbolLen(10)
        Chewing.setCandPerPage(10)
        Chewing.setPhraseChoiceRearward(false)
        Chewing.handleDefault('`')
        Chewing.candOpen()
        assertEquals(Chewing.candTotalChoice(), 22)
        assertEquals(Chewing.candStringByIndexStatic(0), "…")
        assertEquals(Chewing.candStringByIndexStatic(1), "※")
        assertEquals(Chewing.candStringByIndexStatic(2), "常用符號")
        assertEquals(Chewing.candStringByIndexStatic(10), "雙線框")
        assertEquals(Chewing.candStringByIndexStatic(12), "線段")
        Chewing.handleDefault('1')
        Chewing.commitPreeditBuf()
        assertEquals(Chewing.commitString(), "…")

        // 換頁到「雙線框」
        // keyboardless API 版
        Chewing.handleDefault('`')
        Chewing.candChooseByIndex(10)
        assertEquals(Chewing.candTotalChoice(), 29)
        assertEquals(Chewing.candStringByIndexStatic(0), "╔")
        Chewing.candChooseByIndex(0)
        Chewing.commitPreeditBuf()
        assertEquals(Chewing.commitString(), "╔")

        // 模擬鍵盤操作版
        Chewing.handleDefault('`')
        Chewing.handleSpace()
        Chewing.handleDefault('1')
        assertEquals(Chewing.candTotalChoice(), 29)
        Chewing.handleDefault('1')
        Chewing.commitPreeditBuf()
        Chewing.candClose()
        assertEquals(Chewing.commitString(), "╔")

        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        Chewing.setSelKey(selKeys, 10)

        Chewing.handleDefault('`')
        Chewing.handleDefault('3')
        Chewing.handleDefault('1')
        Chewing.commitPreeditBuf()
        assertEquals(Chewing.commitString(), "，")

        Chewing.setSpaceAsSelection(1)
        Chewing.handleDefault('1')
        Chewing.handleDefault('l')
        Chewing.handleDefault('3')
        Chewing.handleHome()
        Chewing.handleSpace()
        Chewing.handleDefault('3')
        Chewing.commitPreeditBuf()
        assertEquals(Chewing.commitString(), "飽")
    }

    @Test
    fun testCommitCheck() {
        Chewing.setChiEngMode(CHINESE_MODE)
        Chewing.setMaxChiSymbolLen(10)
        Chewing.setCandPerPage(10)
        Chewing.setPhraseChoiceRearward(false)
        val newKeyboardType = Chewing.convKBStr2Num("KB_HSU")
        Chewing.setKBType(newKeyboardType)

        Chewing.handleDefault('l')
        Chewing.handleDefault('w')
        Chewing.handleDefault('f')
        Chewing.handleDefault('c')
        Chewing.handleDefault('x')
        Chewing.handleDefault('f')

        repeat(4) {
            Chewing.handleDefault('m')
            Chewing.handleDefault('w')
            Chewing.handleSpace()
            Chewing.handleDefault('m')
            Chewing.handleDefault('e')
            Chewing.handleSpace()
        }

        Chewing.handleDefault('m')
        Chewing.handleDefault('w')
        Chewing.handleSpace() // 此時應該觸發送出最前端詞「老鼠」
        assertEquals(Chewing.commitCheck(), 1)

        Chewing.handleDefault('m')
        Chewing.handleDefault('e')
        Chewing.handleSpace()

        assertEquals(Chewing.commitStringStatic(), "老鼠")
        assertEquals(Chewing.bufferStringStatic(), "貓咪貓咪貓咪貓咪貓咪")
        assertEquals(Chewing.commitCheck(), 0)
        Chewing.commitPreeditBuf()
        assertEquals(Chewing.commitCheck(), 1)
    }

    @After
    fun deleteChewingEngine() {
        Chewing.delete()
    }
}