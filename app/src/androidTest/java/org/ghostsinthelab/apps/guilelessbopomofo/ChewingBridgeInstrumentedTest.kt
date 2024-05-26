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
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge.chewing
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
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
        chewing.connect(dataPath)
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
        chewing.setChiEngMode(SYMBOL_MODE)
        assertEquals(SYMBOL_MODE, chewing.getChiEngMode())

        chewing.handleDefault('t')
        chewing.handleDefault('e')
        chewing.handleDefault('a')
        // 如果一開始 pre-edit buffer 完全無資料，SYMBOL_MODE 會直接送出字符
        assertEquals("", chewing.bufferStringStatic())

        chewing.setChiEngMode(CHINESE_MODE)
        assertEquals(CHINESE_MODE, chewing.getChiEngMode())

        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewing.handleDefault(key)
        }

        chewing.setChiEngMode(SYMBOL_MODE)
        assertEquals(SYMBOL_MODE, chewing.getChiEngMode())

        chewing.handleSpace()
        chewing.handleDefault('g')
        chewing.handleDefault('r')
        chewing.handleDefault('e')
        chewing.handleDefault('e')
        chewing.handleDefault('n')
        chewing.handleSpace()
        chewing.handleDefault('t')
        chewing.handleDefault('e')
        chewing.handleDefault('a')
        assertEquals("綠茶 green tea", chewing.bufferStringStatic())
    }

    @Test
    fun validSelKeys() {
        chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)
        val getSelKey = chewing.getSelKey()
        assertNotEquals('1'.code, getSelKey[0])
        assertEquals('a'.code, getSelKey[0])
    }

    @Test
    fun validOpenPuncCandidates() {
        chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)
        ChewingUtil.openPuncCandidates()
        assertEquals(true, ChewingUtil.candWindowOpened())
        val candidateString: String = chewing.candStringByIndexStatic(0)
        assertEquals("，", candidateString)
    }

    @Test
    fun validPagedCandidates() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)
        chewing.setPhraseChoiceRearward(false)

        chewing.handleDefault('x')
        chewing.handleDefault('u')
        chewing.handleDefault('/')
        chewing.handleDefault('6')

        chewing.handleDefault('m')
        chewing.handleDefault('/')
        chewing.handleDefault('4')

        chewing.handleDefault('r')
        chewing.handleDefault('u')
        chewing.handleDefault('p')
        chewing.handleSpace()

        chewing.handleHome()
        assertEquals(0, chewing.cursorCurrent())
        chewing.handleRight()
        assertEquals(1, chewing.cursorCurrent())
        chewing.handleRight()
        assertEquals(2, chewing.cursorCurrent())
        chewing.handleLeft()
        assertEquals(1, chewing.cursorCurrent())
        chewing.handleLeft()
        assertEquals(0, chewing.cursorCurrent())

        chewing.candOpen()
        assertEquals(true, ChewingUtil.candWindowOpened())
        assertEquals(1, chewing.candTotalPage())
        assertEquals(1, chewing.candTotalChoice())
        assertEquals(0, chewing.candCurrentPage())
        assertEquals(10, chewing.candChoicePerPage())
        assertEquals(true, chewing.candListHasNext())

        chewing.candEnumerate()
        assertEquals("零用金", chewing.candStringStatic())
        // chewingEngine.candHasNext() will point to the next item in candidates enumerator
        assertEquals(0, chewing.candHasNext())

        chewing.candListNext()

        assertEquals(true, ChewingUtil.candWindowOpened())
        assertEquals(1, chewing.candTotalPage())
        assertEquals(1, chewing.candTotalChoice())
        assertEquals(0, chewing.candCurrentPage())
        assertEquals(10, chewing.candChoicePerPage())
        assertEquals(true, chewing.candListHasNext())

        chewing.candEnumerate()
        assertEquals("零用", chewing.candStringStatic())
        assertEquals(0, chewing.candHasNext())

        chewing.candListNext()

        assertEquals(true, ChewingUtil.candWindowOpened())
        assertEquals(9, chewing.candTotalPage())
        assertEquals(88, chewing.candTotalChoice())
        assertEquals(0, chewing.candCurrentPage())
        assertEquals(10, chewing.candChoicePerPage())
        assertEquals(false, chewing.candListHasNext())

        // loop the candidates list
        chewing.candEnumerate()
        assertEquals("零", chewing.candStringStatic())
        assertEquals(1, chewing.candHasNext())
        assertEquals("玲", chewing.candStringStatic())
        assertEquals(1, chewing.candHasNext())
        assertEquals("靈", chewing.candStringStatic())

        // switch to next page
        chewing.handlePageDown()
        assertEquals(9, chewing.candTotalPage())
        assertEquals(1, chewing.candCurrentPage())
        chewing.candEnumerate()
        assertEquals("苓", chewing.candStringStatic())
        assertEquals(1, chewing.candHasNext())
        assertEquals("伶", chewing.candStringStatic())

        chewing.handleEsc() // should have similar effect as chewingEngine.candClose() does
        assertEquals(true, ChewingUtil.candWindowClosed())
    }

    @Test
    fun validPhysicalKeyboardCandidatesSelection() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)
        chewing.setPhraseChoiceRearward(false)
        chewing.setSpaceAsSelection(1)

        chewing.handleDefault('x')
        chewing.handleDefault('u')
        chewing.handleDefault('/')
        chewing.handleDefault('6')
        chewing.handleSpace()
        assertEquals(true, ChewingUtil.candWindowOpened())
        assertEquals(9, chewing.candTotalPage())
        assertEquals(88, chewing.candTotalChoice())
        assertEquals(0, chewing.candCurrentPage())
        assertEquals(10, chewing.candChoicePerPage())
        assertEquals(false, chewing.candListHasNext())

        // switch to next page
        chewing.handlePageDown()
        assertEquals(1, chewing.candCurrentPage())

        chewing.candEnumerate()
        assertEquals("苓", chewing.candStringStatic())
        assertEquals(1, chewing.candHasNext())
        assertEquals("伶", chewing.candStringStatic())

        chewing.handleDefault('2')
        assertEquals("伶", chewing.bufferStringStatic())
    }

    @Test
    fun validMaxChiSymbolLen() {
        chewing.setMaxChiSymbolLen(10)
        assertEquals(10, chewing.getMaxChiSymbolLen())
    }

    @Test
    fun validCandPerPage() {
        chewing.setCandPerPage(9)
        assertEquals(9, chewing.getCandPerPage())
    }

    @Test
    fun validPhraseChoiceRearward() {
        chewing.setPhraseChoiceRearward(true)
        assertTrue(chewing.getPhraseChoiceRearward())
        chewing.setPhraseChoiceRearward(false)
        assertFalse(chewing.getPhraseChoiceRearward())
    }

    @Test
    fun validCommitPhrase() {
        // ref: https://starforcefield.wordpress.com/2012/08/13/%E6%8E%A2%E7%B4%A2%E6%96%B0%E9%85%B7%E9%9F%B3%E8%BC%B8%E5%85%A5%E6%B3%95%EF%BC%9A%E4%BD%BF%E7%94%A8libchewing/
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
        chewing.setCandPerPage(9)
        chewing.setPhraseChoiceRearward(false)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewing.handleDefault(key)
        }
        chewing.handleLeft()
        chewing.handleLeft()
        chewing.candOpen()
        chewing.candTotalChoice()
        chewing.candChooseByIndex(0)
        chewing.commitPreeditBuf()
        var commitString: String = chewing.commitString()
        assertEquals("綠茶", commitString)

        chewing.handleDefault('5')
        chewing.handleSpace()
        chewing.candOpen()
        chewing.candTotalChoice()
        chewing.candChooseByIndex(12)
        chewing.commitPreeditBuf()
        commitString = chewing.commitString()
        assertEquals("蜘", commitString)
    }

    @Test
    fun testSetPhraseChoiceRearward() { // 後方選詞
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
        chewing.setCandPerPage(9)
        chewing.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewing.handleDefault(key)
        }
        chewing.candOpen()
        chewing.candTotalChoice()
        chewing.candChooseByIndex(0)
        chewing.commitPreeditBuf()
        val commitString: String = chewing.commitString()
        assertEquals("綠茶", commitString)
    }

    @Test
    fun validGetCandidatesByPage() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('a', 's', 'd', 'f', 'g', 'q', 'w', 'e', 'r', 't').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)
        chewing.setPhraseChoiceRearward(false)
        chewing.setSpaceAsSelection(1)

        chewing.handleDefault('x')
        chewing.handleDefault('u')
        chewing.handleDefault('/')
        chewing.handleDefault('6')
        chewing.handleSpace()

        assertEquals(true, ChewingUtil.candWindowOpened())
        assertEquals(9, chewing.candTotalPage())
        assertEquals(88, chewing.candTotalChoice())
        assertEquals(0, chewing.candCurrentPage())
        assertEquals(10, chewing.candChoicePerPage())
        assertEquals(false, chewing.candListHasNext())

        var candidates = ChewingUtil.getCandidatesByPage(0)
        assertEquals(0, candidates[0].index)
        assertEquals("零", candidates[0].candidateString)
        assertEquals('a', candidates[0].selectionKey)

        candidates = ChewingUtil.getCandidatesByPage(1)
        assertEquals(10, candidates[0].index)
        assertEquals("苓", candidates[0].candidateString)
        assertEquals('a', candidates[0].selectionKey)
        assertEquals(11, candidates[1].index)
        assertEquals("伶", candidates[1].candidateString)
        assertEquals('s', candidates[1].selectionKey)

        // last page
        candidates = ChewingUtil.getCandidatesByPage(8)
        assertEquals(80, candidates[0].index)
        assertEquals("衑", candidates[0].candidateString)
        assertEquals('a', candidates[0].selectionKey)

        assertEquals(81, candidates[1].index)
        assertEquals("閝", candidates[1].candidateString)
        assertEquals('s', candidates[1].selectionKey)
        // bounding
        assertEquals(7, candidates.lastIndex)
        assertEquals('e', candidates[7].selectionKey)

    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun validIndexOutOfBoundsExceptionGetCandidatesByPage() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)
        chewing.setPhraseChoiceRearward(false)
        chewing.setSpaceAsSelection(1)

        chewing.handleDefault('x')
        chewing.handleDefault('u')
        chewing.handleDefault('/')
        chewing.handleDefault('6')
        chewing.handleSpace()

        // last page
        val candidates = ChewingUtil.getCandidatesByPage(8)
        assertNotNull(candidates[7])
        // over bounding, should throws IndexOutOfBoundsException here:
        assertNull(candidates[8])
    }

    @Test
    fun validCommitPreeditBuf() { // 測試 commitPreeditBuf() 回傳值
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
        chewing.setCandPerPage(9)
        chewing.setPhraseChoiceRearward(true)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewing.handleDefault(key)
        }
        chewing.candOpen()
        chewing.candTotalChoice()
        chewing.candChooseByIndex(0)
        assertEquals(0, chewing.commitPreeditBuf())
        assertEquals(-1, chewing.commitPreeditBuf())
    }

    @Test
    fun validMiddlePhraseCandidate() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
        chewing.setCandPerPage(9)
        chewing.setPhraseChoiceRearward(false)
        // 密封膠帶 蜜蜂 交代 交待 蜂膠
        // ㄇ一ˋ
        chewing.handleDefault('a')
        chewing.handleDefault('u')
        chewing.handleDefault('4')
        // ㄈㄥ
        chewing.handleDefault('z')
        chewing.handleDefault('/')
        chewing.handleSpace()
        // ㄐㄧㄠ
        chewing.handleDefault('r')
        chewing.handleDefault('u')
        chewing.handleDefault('l')
        chewing.handleSpace()
        // ㄉㄞˋ
        chewing.handleDefault('2')
        chewing.handleDefault('9')
        chewing.handleDefault('4')

        // 蜂膠
        chewing.handleLeft()
        chewing.handleLeft()
        chewing.handleLeft()
        chewing.candOpen()
        val candidateString: String = chewing.candStringByIndexStatic(0)
        assertEquals("蜂膠", candidateString)
        chewing.candChooseByIndex(0)
        chewing.commitPreeditBuf()
        val commitString: String = chewing.commitString()
        assertEquals("密蜂膠代", commitString)
        assertEquals(0, chewing.candClose())
    }

    @Test
    fun validCandListNext() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
        chewing.setCandPerPage(10)
        chewing.setPhraseChoiceRearward(false)
        // 零用金 零用 零
        chewing.handleDefault('x')
        chewing.handleDefault('u')
        chewing.handleDefault('/')
        chewing.handleDefault('6')

        chewing.handleDefault('m')
        chewing.handleDefault('/')
        chewing.handleDefault('4')

        chewing.handleDefault('r')
        chewing.handleDefault('u')
        chewing.handleDefault('p')
        chewing.handleSpace()

        chewing.handleHome()
        chewing.candOpen()

        assertEquals("零用金", chewing.candStringByIndexStatic(0))
        assertEquals(true, chewing.candListHasNext())
        assertEquals(0, chewing.candListNext())
        assertEquals("零用", chewing.candStringByIndexStatic(0))
        assertEquals(true, chewing.candListHasNext())
        assertEquals(0, chewing.candListNext())
        assertEquals("零", chewing.candStringByIndexStatic(0))
        assertEquals(-1, chewing.candListNext())
        assertEquals(false, chewing.candListHasNext())

        chewing.candListLast()
        assertEquals("零", chewing.candStringByIndexStatic(0))

        chewing.candListFirst()
        assertEquals("零用金", chewing.candStringByIndexStatic(0))
        assertEquals(0, chewing.candClose())
    }

    @Test
    fun switchToHsuLayout() {
        val newKeyboardType = chewing.convKBStr2Num("KB_HSU")
        chewing.setKBType(newKeyboardType)
        val currentKeyboardType = chewing.getKBType()
        val currentKeyboardTypeString = chewing.getKBString()
        assertEquals(1, currentKeyboardType)
        assertEquals("KB_HSU", currentKeyboardTypeString)

        chewing.handleDefault('l')
        chewing.handleDefault('l')
        assertEquals("ㄌㄥ", chewing.bopomofoStringStatic())
        chewing.handleDefault('f')
        assertEquals("冷", chewing.bufferString())
        chewing.handleDefault('d')
        chewing.handleDefault('x')
        chewing.handleDefault('l')
        chewing.handleDefault('j')
        chewing.commitPreeditBuf()
        assertEquals("冷凍", chewing.commitString())
    }

    @Test
    fun switchToDvorakHsuLayout() {
        val newKeyboardType = chewing.convKBStr2Num("KB_DVORAK_HSU")
        chewing.setKBType(newKeyboardType)
        val currentKeyboardType = chewing.getKBType()
        val currentKeyboardTypeString = chewing.getKBString()
        assertEquals(7, currentKeyboardType)
        assertEquals("KB_DVORAK_HSU", currentKeyboardTypeString)

        // test ChewingUtil.dvorakToQwertyKeyMapping()
        assertEquals('c', ChewingUtil.dvorakToQwertyKeyMapping('j'))
        assertEquals('p', ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals('1', ChewingUtil.dvorakToQwertyKeyMapping('1'))
        assertEquals('!', ChewingUtil.dvorakToQwertyKeyMapping('!'))

        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals("ㄌㄥ", chewing.bopomofoStringStatic())
        // ˇ
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('f'))
        assertEquals("冷", chewing.bufferString())
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('d'))
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('x'))
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals("ㄉㄨㄥ", chewing.bopomofoStringStatic())
        // ˋ
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('j'))
        chewing.commitPreeditBuf()
        assertEquals("冷凍", chewing.commitString())
    }

    @Test
    fun validCandidateWindowOpenClose() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
        chewing.setCandPerPage(10)
        chewing.setPhraseChoiceRearward(false)
        chewing.setSpaceAsSelection(1)

        // 零
        chewing.handleDefault('x')
        chewing.handleDefault('u')
        chewing.handleDefault('/')
        chewing.handleDefault('6')

        chewing.handleHome()
        // candidate window opened here
        chewing.handleSpace()
        assertTrue(chewing.candTotalChoice() > 0)
        assertEquals(true, ChewingUtil.candWindowOpened())
        // candidate window closed here (after I picker the first candidate)
        chewing.handleDefault('1')
        assertEquals(0, chewing.candTotalChoice())
        assertEquals(true, ChewingUtil.candWindowClosed())
    }

    @Test
    fun switchToEten26Layout() {
        val newKeyboardType = chewing.convKBStr2Num("KB_ET26")
        chewing.setKBType(newKeyboardType)
        val currentKeyboardType = chewing.getKBType()
        val currentKeyboardTypeString = chewing.getKBString()
        assertEquals(5, currentKeyboardType)
        assertEquals("KB_ET26", currentKeyboardTypeString)

        chewing.handleDefault('l')
        chewing.handleDefault('l')
        assertEquals("ㄌㄥ", chewing.bopomofoStringStatic())
        chewing.handleDefault('j')
        assertEquals("冷", chewing.bufferString())
        chewing.handleDefault('d')
        chewing.handleDefault('x')
        chewing.handleDefault('l')
        chewing.handleDefault('k')
        chewing.commitPreeditBuf()
        assertEquals("冷凍", chewing.commitString())
    }

    @Test
    fun switchToDaChenLayout() {
        val newKeyboardType = chewing.convKBStr2Num("KB_DEFAULT")
        chewing.setKBType(newKeyboardType)
        val currentKeyboardType = chewing.getKBType()
        val currentKeyboardTypeString = chewing.getKBString()
        assertEquals(0, currentKeyboardType)
        assertEquals("KB_DEFAULT", currentKeyboardTypeString)

        chewing.handleDefault('x')
        chewing.handleDefault('/')
        assertEquals("ㄌㄥ", chewing.bopomofoStringStatic())
        chewing.handleDefault('3')
        assertEquals("冷", chewing.bufferString())
        chewing.handleDefault('2')
        chewing.handleDefault('j')
        chewing.handleDefault('/')
        chewing.handleDefault('4')
        chewing.commitPreeditBuf()
        assertEquals("冷凍", chewing.commitString())
    }

    @Test
    fun switchToSymbolSelectionMode() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
        chewing.setCandPerPage(10)
        chewing.setPhraseChoiceRearward(false)
        chewing.handleDefault('`')
        chewing.candOpen()
        assertEquals(22, chewing.candTotalChoice())
        assertEquals("…", chewing.candStringByIndexStatic(0))
        assertEquals("※", chewing.candStringByIndexStatic(1))
        assertEquals("常用符號", chewing.candStringByIndexStatic(2))
        assertEquals("雙線框", chewing.candStringByIndexStatic(10))
        assertEquals("線段", chewing.candStringByIndexStatic(12))
        chewing.handleDefault('1')
        chewing.commitPreeditBuf()
        assertEquals("…", chewing.commitString())

        // 換頁到「雙線框」
        // keyboardless API 版
        chewing.handleDefault('`')
        chewing.candChooseByIndex(10)
        assertEquals(29, chewing.candTotalChoice())
        assertEquals("╔", chewing.candStringByIndexStatic(0))
        chewing.candChooseByIndex(0)
        chewing.commitPreeditBuf()
        assertEquals("╔", chewing.commitString())

        // 模擬鍵盤操作版
        chewing.handleDefault('`')
        // next page
        chewing.handleRight()
        chewing.handleDefault('1')
        assertEquals(29, chewing.candTotalChoice())
        chewing.handleDefault('1')
        chewing.commitPreeditBuf()
        chewing.candClose()
        assertEquals("╔", chewing.commitString())

        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)

        chewing.handleDefault('`')
        chewing.handleDefault('3')
        chewing.handleDefault('1')
        chewing.commitPreeditBuf()
        assertEquals("，", chewing.commitString())

        chewing.setSpaceAsSelection(1)
        chewing.handleDefault('1')
        chewing.handleDefault('l')
        chewing.handleDefault('3')
        chewing.handleHome()
        chewing.handleSpace()
        chewing.handleDefault('3')
        chewing.commitPreeditBuf()
        assertEquals("飽", chewing.commitString())
    }

    @Test
    fun testCommitCheck() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
        assertEquals(10, chewing.getMaxChiSymbolLen())

        chewing.setCandPerPage(10)
        chewing.setPhraseChoiceRearward(false)
        val newKeyboardType = chewing.convKBStr2Num("KB_HSU")
        chewing.setKBType(newKeyboardType)

        chewing.handleDefault('l')
        chewing.handleDefault('w')
        chewing.handleDefault('f')
        chewing.handleDefault('c')
        chewing.handleDefault('x')
        chewing.handleDefault('f')

        assertEquals("老鼠", chewing.bufferStringStatic())

        repeat(4) {
            chewing.handleDefault('m')
            chewing.handleDefault('w')
            chewing.handleSpace()
            chewing.handleDefault('m')
            chewing.handleDefault('e')
            chewing.handleSpace()
        }

        assertEquals("老鼠貓咪貓咪貓咪貓咪", chewing.bufferStringStatic())

        chewing.handleDefault('m')
        chewing.handleDefault('w')
        chewing.handleSpace() // 超出 maxChiSymbolLen，此時應該觸發送出最前端詞「老鼠」
        assertEquals("貓咪貓咪貓咪貓咪貓", chewing.bufferStringStatic())
        assertEquals("老鼠", chewing.commitStringStatic())
        assertEquals(1, chewing.commitCheck())

        chewing.handleDefault('m')
        chewing.handleDefault('e')
        chewing.handleSpace()

        assertEquals("貓咪貓咪貓咪貓咪貓咪", chewing.bufferStringStatic())
        assertEquals(0, chewing.commitCheck())
        chewing.commitPreeditBuf()
        assertEquals(1, chewing.commitCheck())
    }

    @After
    fun deleteChewingEngine() {
        chewing.delete()
    }
}