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
import com.miyabi_hiroshi.app.libchewing_android_module.Chewing
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
class chewingInstrumentedTest {
    private lateinit var dataPath: String
    private val chewing: Chewing = Chewing()
    
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
        assertEquals(chewing.getChiEngMode(), SYMBOL_MODE)

        chewing.handleDefault('t')
        chewing.handleDefault('e')
        chewing.handleDefault('a')
        // 如果一開始 pre-edit buffer 完全無資料，SYMBOL_MODE 會直接送出字符
        assertEquals(chewing.bufferStringStatic(), "")

        chewing.setChiEngMode(CHINESE_MODE)
        assertEquals(chewing.getChiEngMode(), CHINESE_MODE)

        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            chewing.handleDefault(key)
        }

        chewing.setChiEngMode(SYMBOL_MODE)
        assertEquals(chewing.getChiEngMode(), SYMBOL_MODE)

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
        assertEquals(chewing.bufferStringStatic(), "綠茶 green tea")
    }

    @Test
    fun validSelKeys() {
        chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)
        val getSelKey = chewing.getSelKey()
        assertNotEquals(getSelKey[0], '1'.code)
        assertEquals(getSelKey[0], 'a'.code)
    }

    @Test
    fun validOpenPuncCandidates() {
        chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)
        ChewingUtil.openPuncCandidates()
        assertEquals(ChewingUtil.candWindowOpened(), true)
        val candidateString: String = chewing.candStringByIndexStatic(0)
        assertEquals(candidateString, "，")
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
        assertEquals(chewing.cursorCurrent(), 0)
        chewing.handleRight()
        assertEquals(chewing.cursorCurrent(), 1)
        chewing.handleRight()
        assertEquals(chewing.cursorCurrent(), 2)
        chewing.handleLeft()
        assertEquals(chewing.cursorCurrent(), 1)
        chewing.handleLeft()
        assertEquals(chewing.cursorCurrent(), 0)

        chewing.candOpen()
        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(chewing.candTotalPage(), 1)
        assertEquals(chewing.candTotalChoice(), 1)
        assertEquals(chewing.candCurrentPage(), 0)
        assertEquals(chewing.candChoicePerPage(), 10)
        assertEquals(chewing.candListHasNext(), true)

        chewing.candEnumerate()
        assertEquals(chewing.candStringStatic(), "零用金")
        // chewingEngine.candHasNext() will point to the next item in candidates enumerator
        assertEquals(chewing.candHasNext(), 0)

        chewing.candListNext()

        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(chewing.candTotalPage(), 1)
        assertEquals(chewing.candTotalChoice(), 1)
        assertEquals(chewing.candCurrentPage(), 0)
        assertEquals(chewing.candChoicePerPage(), 10)
        assertEquals(chewing.candListHasNext(), true)

        chewing.candEnumerate()
        assertEquals(chewing.candStringStatic(), "零用")
        assertEquals(chewing.candHasNext(), 0)

        chewing.candListNext()

        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(chewing.candTotalPage(), 9)
        assertEquals(chewing.candTotalChoice(), 88)
        assertEquals(chewing.candCurrentPage(), 0)
        assertEquals(chewing.candChoicePerPage(), 10)
        assertEquals(chewing.candListHasNext(), false)

        // loop the candidates list
        chewing.candEnumerate()
        assertEquals(chewing.candStringStatic(), "零")
        assertEquals(chewing.candHasNext(), 1)
        assertEquals(chewing.candStringStatic(), "玲")
        assertEquals(chewing.candHasNext(), 1)
        assertEquals(chewing.candStringStatic(), "靈")

        // switch to next page
        chewing.handlePageDown()
        assertEquals(chewing.candTotalPage(), 9)
        assertEquals(chewing.candCurrentPage(), 1)
        chewing.candEnumerate()
        assertEquals(chewing.candStringStatic(), "苓")
        assertEquals(chewing.candHasNext(), 1)
        assertEquals(chewing.candStringStatic(), "伶")

        chewing.handleEsc() // should have similar effect as chewingEngine.candClose() does
        assertEquals(ChewingUtil.candWindowClosed(), true)
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
        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(chewing.candTotalPage(), 9)
        assertEquals(chewing.candTotalChoice(), 88)
        assertEquals(chewing.candCurrentPage(), 0)
        assertEquals(chewing.candChoicePerPage(), 10)
        assertEquals(chewing.candListHasNext(), false)

        // switch to next page
        chewing.handlePageDown()
        assertEquals(chewing.candCurrentPage(), 1)

        chewing.candEnumerate()
        assertEquals(chewing.candStringStatic(), "苓")
        assertEquals(chewing.candHasNext(), 1)
        assertEquals(chewing.candStringStatic(), "伶")

        chewing.handleDefault('2')
        assertEquals(chewing.bufferStringStatic(), "伶")
    }

    @Test
    fun validMaxChiSymbolLen() {
        chewing.setMaxChiSymbolLen(10)
        assertEquals(chewing.getMaxChiSymbolLen(), 10)
    }

    @Test
    fun validCandPerPage() {
        chewing.setCandPerPage(9)
        assertEquals(chewing.getCandPerPage(), 9)
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
        assertEquals(commitString, "綠茶")

        chewing.handleDefault('5')
        chewing.handleSpace()
        chewing.candOpen()
        chewing.candTotalChoice()
        chewing.candChooseByIndex(12)
        chewing.commitPreeditBuf()
        commitString = chewing.commitString()
        assertEquals(commitString, "蜘")
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
        assertEquals(commitString, "綠茶")
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

        assertEquals(ChewingUtil.candWindowOpened(), true)
        assertEquals(chewing.candTotalPage(), 9)
        assertEquals(chewing.candTotalChoice(), 88)
        assertEquals(chewing.candCurrentPage(), 0)
        assertEquals(chewing.candChoicePerPage(), 10)
        assertEquals(chewing.candListHasNext(), false)

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
        assertEquals(chewing.commitPreeditBuf(), 0)
        assertEquals(chewing.commitPreeditBuf(), -1)
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
        assertEquals(candidateString, "蜂膠")
        chewing.candChooseByIndex(0)
        chewing.commitPreeditBuf()
        val commitString: String = chewing.commitString()
        assertEquals(commitString, "密蜂膠代")
        assertEquals(chewing.candClose(), 0)
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

        assertEquals(chewing.candStringByIndexStatic(0), "零用金")
        assertEquals(chewing.candListHasNext(), true)
        assertEquals(chewing.candListNext(), 0)
        assertEquals(chewing.candStringByIndexStatic(0), "零用")
        assertEquals(chewing.candListHasNext(), true)
        assertEquals(chewing.candListNext(), 0)
        assertEquals(chewing.candStringByIndexStatic(0), "零")
        assertEquals(chewing.candListNext(), -1)
        assertEquals(chewing.candListHasNext(), false)

        chewing.candListLast()
        assertEquals(chewing.candStringByIndexStatic(0), "零")

        chewing.candListFirst()
        assertEquals(chewing.candStringByIndexStatic(0), "零用金")
        assertEquals(chewing.candClose(), 0)
    }

    @Test
    fun switchToHsuLayout() {
        val newKeyboardType = chewing.convKBStr2Num("KB_HSU")
        chewing.setKBType(newKeyboardType)
        val currentKeyboardType = chewing.getKBType()
        val currentKeyboardTypeString = chewing.getKBString()
        assertEquals(currentKeyboardType, 1)
        assertEquals(currentKeyboardTypeString, "KB_HSU")

        chewing.handleDefault('l')
        chewing.handleDefault('l')
        assertEquals(chewing.bopomofoStringStatic(), "ㄌㄥ")
        chewing.handleDefault('f')
        assertEquals(chewing.bufferString(), "冷")
        chewing.handleDefault('d')
        chewing.handleDefault('x')
        chewing.handleDefault('l')
        chewing.handleDefault('j')
        chewing.commitPreeditBuf()
        assertEquals(chewing.commitString(), "冷凍")
    }

    @Test
    fun switchToDvorakHsuLayout() {
        val newKeyboardType = chewing.convKBStr2Num("KB_DVORAK_HSU")
        chewing.setKBType(newKeyboardType)
        val currentKeyboardType = chewing.getKBType()
        val currentKeyboardTypeString = chewing.getKBString()
        assertEquals(currentKeyboardType, 7)
        assertEquals(currentKeyboardTypeString, "KB_DVORAK_HSU")

        // test ChewingUtil.dvorakToQwertyKeyMapping()
        assertEquals(ChewingUtil.dvorakToQwertyKeyMapping('j'), 'c')
        assertEquals(ChewingUtil.dvorakToQwertyKeyMapping('l'), 'p')
        assertEquals(ChewingUtil.dvorakToQwertyKeyMapping('1'), '1')
        assertEquals(ChewingUtil.dvorakToQwertyKeyMapping('!'), '!')

        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals(chewing.bopomofoStringStatic(), "ㄌㄥ")
        // ˇ
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('f'))
        assertEquals(chewing.bufferString(), "冷")
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('d'))
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('x'))
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals(chewing.bopomofoStringStatic(), "ㄉㄨㄥ")
        // ˋ
        chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('j'))
        chewing.commitPreeditBuf()
        assertEquals(chewing.commitString(), "冷凍")
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
        assertEquals(ChewingUtil.candWindowOpened(), true)
        // candidate window closed here (after I picker the first candidate)
        chewing.handleDefault('1')
        assertEquals(chewing.candTotalChoice(), 0)
        assertEquals(ChewingUtil.candWindowClosed(), true)
    }

    @Test
    fun switchToEten26Layout() {
        val newKeyboardType = chewing.convKBStr2Num("KB_ET26")
        chewing.setKBType(newKeyboardType)
        val currentKeyboardType = chewing.getKBType()
        val currentKeyboardTypeString = chewing.getKBString()
        assertEquals(currentKeyboardType, 5)
        assertEquals(currentKeyboardTypeString, "KB_ET26")

        chewing.handleDefault('l')
        chewing.handleDefault('l')
        assertEquals(chewing.bopomofoStringStatic(), "ㄌㄥ")
        chewing.handleDefault('j')
        assertEquals(chewing.bufferString(), "冷")
        chewing.handleDefault('d')
        chewing.handleDefault('x')
        chewing.handleDefault('l')
        chewing.handleDefault('k')
        chewing.commitPreeditBuf()
        assertEquals(chewing.commitString(), "冷凍")
    }

    @Test
    fun switchToDaChenLayout() {
        val newKeyboardType = chewing.convKBStr2Num("KB_DEFAULT")
        chewing.setKBType(newKeyboardType)
        val currentKeyboardType = chewing.getKBType()
        val currentKeyboardTypeString = chewing.getKBString()
        assertEquals(currentKeyboardType, 0)
        assertEquals(currentKeyboardTypeString, "KB_DEFAULT")

        chewing.handleDefault('x')
        chewing.handleDefault('/')
        assertEquals(chewing.bopomofoStringStatic(), "ㄌㄥ")
        chewing.handleDefault('3')
        assertEquals(chewing.bufferString(), "冷")
        chewing.handleDefault('2')
        chewing.handleDefault('j')
        chewing.handleDefault('/')
        chewing.handleDefault('4')
        chewing.commitPreeditBuf()
        assertEquals(chewing.commitString(), "冷凍")
    }

    @Test
    fun switchToSymbolSelectionMode() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
        chewing.setCandPerPage(10)
        chewing.setPhraseChoiceRearward(false)
        chewing.handleDefault('`')
        chewing.candOpen()
        assertEquals(chewing.candTotalChoice(), 22)
        assertEquals(chewing.candStringByIndexStatic(0), "…")
        assertEquals(chewing.candStringByIndexStatic(1), "※")
        assertEquals(chewing.candStringByIndexStatic(2), "常用符號")
        assertEquals(chewing.candStringByIndexStatic(10), "雙線框")
        assertEquals(chewing.candStringByIndexStatic(12), "線段")
        chewing.handleDefault('1')
        chewing.commitPreeditBuf()
        assertEquals(chewing.commitString(), "…")

        // 換頁到「雙線框」
        // keyboardless API 版
        chewing.handleDefault('`')
        chewing.candChooseByIndex(10)
        assertEquals(chewing.candTotalChoice(), 29)
        assertEquals(chewing.candStringByIndexStatic(0), "╔")
        chewing.candChooseByIndex(0)
        chewing.commitPreeditBuf()
        assertEquals(chewing.commitString(), "╔")

        // 模擬鍵盤操作版
        chewing.handleDefault('`')
        chewing.handleSpace()
        chewing.handleDefault('1')
        assertEquals(chewing.candTotalChoice(), 29)
        chewing.handleDefault('1')
        chewing.commitPreeditBuf()
        chewing.candClose()
        assertEquals(chewing.commitString(), "╔")

        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        chewing.setSelKey(selKeys, 10)

        chewing.handleDefault('`')
        chewing.handleDefault('3')
        chewing.handleDefault('1')
        chewing.commitPreeditBuf()
        assertEquals(chewing.commitString(), "，")

        chewing.setSpaceAsSelection(1)
        chewing.handleDefault('1')
        chewing.handleDefault('l')
        chewing.handleDefault('3')
        chewing.handleHome()
        chewing.handleSpace()
        chewing.handleDefault('3')
        chewing.commitPreeditBuf()
        assertEquals(chewing.commitString(), "飽")
    }

    @Test
    fun testCommitCheck() {
        chewing.setChiEngMode(CHINESE_MODE)
        chewing.setMaxChiSymbolLen(10)
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

        repeat(4) {
            chewing.handleDefault('m')
            chewing.handleDefault('w')
            chewing.handleSpace()
            chewing.handleDefault('m')
            chewing.handleDefault('e')
            chewing.handleSpace()
        }

        chewing.handleDefault('m')
        chewing.handleDefault('w')
        chewing.handleSpace() // 此時應該觸發送出最前端詞「老鼠」
        assertEquals(chewing.commitCheck(), 1)

        chewing.handleDefault('m')
        chewing.handleDefault('e')
        chewing.handleSpace()

        assertEquals(chewing.commitStringStatic(), "老鼠")
        assertEquals(chewing.bufferStringStatic(), "貓咪貓咪貓咪貓咪貓咪")
        assertEquals(chewing.commitCheck(), 0)
        chewing.commitPreeditBuf()
        assertEquals(chewing.commitCheck(), 1)
    }

    @After
    fun deleteChewingEngine() {
        chewing.delete()
    }
}