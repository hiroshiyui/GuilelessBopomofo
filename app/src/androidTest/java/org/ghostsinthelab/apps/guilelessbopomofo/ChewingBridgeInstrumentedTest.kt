/*
 * Guileless Bopomofo
 * Copyright (C) 2024 YOU, HUI-HONG
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

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ChewingBridgeInstrumentedTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val dataPath: String = appContext.dataDir.absolutePath
    private val chewingDataDir = File(dataPath)
    private val chewingDataFiles =
        listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

    @Before
    fun setupChewingEngine() {
        // Copy Chewing data files to data directory
        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            Log.d("ChewingInstrumentedTest", "Copying ${file}...")
            val dataInputStream = appContext.assets.open(file)
            val dataOutputStream = FileOutputStream(destinationFile)

            try {
                dataInputStream.copyTo(dataOutputStream)
            } catch (e: java.lang.Exception) {
                e.message?.let {
                    Log.e("ChewingInstrumentedTest", it)
                }
            } finally {
                Log.d("ChewingInstrumentedTest", "Closing data I/O streams")
                dataInputStream.close()
                dataOutputStream.close()
            }
        }

        // Initialize Chewing
        ChewingBridge.chewing.connect(dataPath)
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
        val dataPathContainsPackageName: Boolean = dataPath.contains(appContext.packageName)
        assertTrue(dataPathContainsPackageName)
    }

    @Test
    fun validChiEngMode() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        assertEquals(ChiEngMode.SYMBOL.mode, ChewingBridge.chewing.getChiEngMode())

        ChewingBridge.chewing.handleDefault('t')
        ChewingBridge.chewing.handleDefault('e')
        ChewingBridge.chewing.handleDefault('a')
        // 如果一開始 pre-edit buffer 完全無資料，ChiEngMode.SYMBOL.mode 會直接送出字符
        assertEquals("", ChewingBridge.chewing.bufferStringStatic())

        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        assertEquals(ChiEngMode.CHINESE.mode, ChewingBridge.chewing.getChiEngMode())

        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }

        ChewingBridge.chewing.setChiEngMode(ChiEngMode.SYMBOL.mode)
        assertEquals(ChiEngMode.SYMBOL.mode, ChewingBridge.chewing.getChiEngMode())

        ChewingBridge.chewing.handleSpace()
        ChewingBridge.chewing.handleDefault('g')
        ChewingBridge.chewing.handleDefault('r')
        ChewingBridge.chewing.handleDefault('e')
        ChewingBridge.chewing.handleDefault('e')
        ChewingBridge.chewing.handleDefault('n')
        ChewingBridge.chewing.handleSpace()
        ChewingBridge.chewing.handleDefault('t')
        ChewingBridge.chewing.handleDefault('e')
        ChewingBridge.chewing.handleDefault('a')
        assertEquals("綠茶 green tea", ChewingBridge.chewing.bufferStringStatic())
    }

    @Test
    fun validSelKeys() {
        ChewingBridge.chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';').map { it.code }
                .toIntArray()
        ChewingBridge.chewing.setSelKey(selKeys, 10)
        val getSelKey = ChewingBridge.chewing.getSelKey()
        assertNotEquals('1'.code, getSelKey[0])
        assertEquals('a'.code, getSelKey[0])
    }

    @Test
    fun validOpenFrequentlyUsedCandidates() {
        ChewingBridge.chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.chewing.setSelKey(selKeys, 10)
        ChewingUtil.openFrequentlyUsedCandidates()
        assertEquals(true, ChewingUtil.candidateWindowOpened())
        val candidateString: String = ChewingBridge.chewing.candStringByIndexStatic(0)
        assertEquals("，", candidateString)
    }

    @Test
    fun validPagedCandidates() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.chewing.setSelKey(selKeys, 10)
        ChewingBridge.chewing.setPhraseChoiceRearward(0)

        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('6')

        ChewingBridge.chewing.handleDefault('m')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('4')

        ChewingBridge.chewing.handleDefault('r')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('p')
        ChewingBridge.chewing.handleSpace()

        ChewingBridge.chewing.handleHome()
        assertEquals(0, ChewingBridge.chewing.cursorCurrent())
        ChewingBridge.chewing.handleRight()
        assertEquals(1, ChewingBridge.chewing.cursorCurrent())
        ChewingBridge.chewing.handleRight()
        assertEquals(2, ChewingBridge.chewing.cursorCurrent())
        ChewingBridge.chewing.handleLeft()
        assertEquals(1, ChewingBridge.chewing.cursorCurrent())
        ChewingBridge.chewing.handleLeft()
        assertEquals(0, ChewingBridge.chewing.cursorCurrent())

        ChewingBridge.chewing.candOpen()
        assertEquals(true, ChewingUtil.candidateWindowOpened())
        assertEquals(1, ChewingBridge.chewing.candTotalPage())
        assertEquals(1, ChewingBridge.chewing.candTotalChoice())
        assertEquals(0, ChewingBridge.chewing.candCurrentPage())
        assertEquals(10, ChewingBridge.chewing.candChoicePerPage())
        assertEquals(true, ChewingBridge.chewing.candListHasNext())

        ChewingBridge.chewing.candEnumerate()
        assertEquals("零用金", ChewingBridge.chewing.candStringStatic())
        // chewing.candHasNext() will point to the next item in candidates enumerator
        assertEquals(0, ChewingBridge.chewing.candHasNext())

        ChewingBridge.chewing.candListNext()

        assertEquals(true, ChewingUtil.candidateWindowOpened())
        assertEquals(1, ChewingBridge.chewing.candTotalPage())
        assertEquals(1, ChewingBridge.chewing.candTotalChoice())
        assertEquals(0, ChewingBridge.chewing.candCurrentPage())
        assertEquals(10, ChewingBridge.chewing.candChoicePerPage())
        assertEquals(true, ChewingBridge.chewing.candListHasNext())

        ChewingBridge.chewing.candEnumerate()
        assertEquals("零用", ChewingBridge.chewing.candStringStatic())
        assertEquals(0, ChewingBridge.chewing.candHasNext())

        ChewingBridge.chewing.candListNext()

        assertEquals(true, ChewingUtil.candidateWindowOpened())
        assertEquals(9, ChewingBridge.chewing.candTotalPage())
        assertEquals(88, ChewingBridge.chewing.candTotalChoice())
        assertEquals(0, ChewingBridge.chewing.candCurrentPage())
        assertEquals(10, ChewingBridge.chewing.candChoicePerPage())
        assertEquals(false, ChewingBridge.chewing.candListHasNext())

        // loop the candidates list
        ChewingBridge.chewing.candEnumerate()
        assertEquals("零", ChewingBridge.chewing.candStringStatic())
        assertEquals(1, ChewingBridge.chewing.candHasNext())
        assertEquals("玲", ChewingBridge.chewing.candStringStatic())
        assertEquals(1, ChewingBridge.chewing.candHasNext())
        assertEquals("靈", ChewingBridge.chewing.candStringStatic())

        // switch to next page
        ChewingBridge.chewing.handlePageDown()
        assertEquals(9, ChewingBridge.chewing.candTotalPage())
        assertEquals(1, ChewingBridge.chewing.candCurrentPage())
        ChewingBridge.chewing.candEnumerate()
        assertEquals("苓", ChewingBridge.chewing.candStringStatic())
        assertEquals(1, ChewingBridge.chewing.candHasNext())
        assertEquals("伶", ChewingBridge.chewing.candStringStatic())

        ChewingBridge.chewing.handleEsc() // should have similar effect as chewing.candClose() does
        assertEquals(true, ChewingUtil.candidateWindowClosed())
    }

    @Test
    fun validPhysicalKeyboardCandidatesSelection() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.chewing.setSelKey(selKeys, 10)
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        ChewingBridge.chewing.setSpaceAsSelection(1)

        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('6')
        ChewingBridge.chewing.handleSpace()
        assertEquals(true, ChewingUtil.candidateWindowOpened())
        assertEquals(9, ChewingBridge.chewing.candTotalPage())
        assertEquals(88, ChewingBridge.chewing.candTotalChoice())
        assertEquals(0, ChewingBridge.chewing.candCurrentPage())
        assertEquals(10, ChewingBridge.chewing.candChoicePerPage())
        assertEquals(false, ChewingBridge.chewing.candListHasNext())

        // switch to next page
        ChewingBridge.chewing.handlePageDown()
        assertEquals(1, ChewingBridge.chewing.candCurrentPage())

        ChewingBridge.chewing.candEnumerate()
        assertEquals("苓", ChewingBridge.chewing.candStringStatic())
        assertEquals(1, ChewingBridge.chewing.candHasNext())
        assertEquals("伶", ChewingBridge.chewing.candStringStatic())

        ChewingBridge.chewing.handleDefault('2')
        assertEquals("伶", ChewingBridge.chewing.bufferStringStatic())
    }

    @Test
    fun validMaxChiSymbolLen() {
        ChewingBridge.chewing.setMaxChiSymbolLen(10)
        assertEquals(10, ChewingBridge.chewing.getMaxChiSymbolLen())
    }

    @Test
    fun validCandPerPage() {
        ChewingBridge.chewing.setCandPerPage(9)
        assertEquals(9, ChewingBridge.chewing.getCandPerPage())
    }

    @Test
    fun validPhraseChoiceRearward() {
        ChewingBridge.chewing.setPhraseChoiceRearward(1)
        assertEquals(1, ChewingBridge.chewing.getPhraseChoiceRearward())
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        assertEquals(0, ChewingBridge.chewing.getPhraseChoiceRearward())
    }

    @Test
    fun validCommitPhrase() {
        // ref: https://starforcefield.wordpress.com/2012/08/13/%E6%8E%A2%E7%B4%A2%E6%96%B0%E9%85%B7%E9%9F%B3%E8%BC%B8%E5%85%A5%E6%B3%95%EF%BC%9A%E4%BD%BF%E7%94%A8libchewing/
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setMaxChiSymbolLen(10)
        ChewingBridge.chewing.setCandPerPage(9)
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        ChewingBridge.chewing.handleLeft()
        ChewingBridge.chewing.handleLeft()
        ChewingBridge.chewing.candOpen()
        ChewingBridge.chewing.candTotalChoice()
        ChewingBridge.chewing.candChooseByIndex(0)
        ChewingBridge.chewing.commitPreeditBuf()
        var commitString: String = ChewingBridge.chewing.commitString()
        assertEquals("綠茶", commitString)

        ChewingBridge.chewing.handleDefault('5')
        ChewingBridge.chewing.handleSpace()
        ChewingBridge.chewing.candOpen()
        ChewingBridge.chewing.candTotalChoice()
        ChewingBridge.chewing.candChooseByIndex(12)
        ChewingBridge.chewing.commitPreeditBuf()
        commitString = ChewingBridge.chewing.commitString()
        assertEquals("蜘", commitString)
    }

    @Test
    fun testSetPhraseChoiceRearward() { // 後方選詞
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setMaxChiSymbolLen(10)
        ChewingBridge.chewing.setCandPerPage(9)
        ChewingBridge.chewing.setPhraseChoiceRearward(1)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        ChewingBridge.chewing.candOpen()
        ChewingBridge.chewing.candTotalChoice()
        ChewingBridge.chewing.candChooseByIndex(0)
        ChewingBridge.chewing.commitPreeditBuf()
        val commitString: String = ChewingBridge.chewing.commitString()
        assertEquals("綠茶", commitString)
    }

    @Test
    fun validGetCandidatesByPage() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('a', 's', 'd', 'f', 'g', 'q', 'w', 'e', 'r', 't').map { it.code }
                .toIntArray()
        ChewingBridge.chewing.setSelKey(selKeys, 10)
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        ChewingBridge.chewing.setSpaceAsSelection(1)

        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('6')
        ChewingBridge.chewing.handleSpace()

        assertEquals(true, ChewingUtil.candidateWindowOpened())
        assertEquals(9, ChewingBridge.chewing.candTotalPage())
        assertEquals(88, ChewingBridge.chewing.candTotalChoice())
        assertEquals(0, ChewingBridge.chewing.candCurrentPage())
        assertEquals(10, ChewingBridge.chewing.candChoicePerPage())
        assertEquals(false, ChewingBridge.chewing.candListHasNext())

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
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setCandPerPage(10)
        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.chewing.setSelKey(selKeys, 10)
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        ChewingBridge.chewing.setSpaceAsSelection(1)

        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('6')
        ChewingBridge.chewing.handleSpace()

        // last page
        val candidates = ChewingUtil.getCandidatesByPage(8)
        assertNotNull(candidates[7])
        // over bounding, should throws IndexOutOfBoundsException here:
        assertNull(candidates[8])
    }

    @Test
    fun validCommitPreeditBuf() { // 測試 commitPreeditBuf() 回傳值
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setMaxChiSymbolLen(10)
        ChewingBridge.chewing.setCandPerPage(9)
        ChewingBridge.chewing.setPhraseChoiceRearward(1)
        val keys = arrayOf('x', 'm', '4', 't', '8', '6')
        for (key in keys) {
            ChewingBridge.chewing.handleDefault(key)
        }
        ChewingBridge.chewing.candOpen()
        ChewingBridge.chewing.candTotalChoice()
        ChewingBridge.chewing.candChooseByIndex(0)
        assertEquals(0, ChewingBridge.chewing.commitPreeditBuf())
        assertEquals(-1, ChewingBridge.chewing.commitPreeditBuf())
    }

    @Test
    fun validMiddlePhraseCandidate() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setMaxChiSymbolLen(10)
        ChewingBridge.chewing.setCandPerPage(9)
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        // 密封膠帶 蜜蜂 交代 交待 蜂膠
        // ㄇ一ˋ
        ChewingBridge.chewing.handleDefault('a')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('4')
        // ㄈㄥ
        ChewingBridge.chewing.handleDefault('z')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleSpace()
        // ㄐㄧㄠ
        ChewingBridge.chewing.handleDefault('r')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('l')
        ChewingBridge.chewing.handleSpace()
        // ㄉㄞˋ
        ChewingBridge.chewing.handleDefault('2')
        ChewingBridge.chewing.handleDefault('9')
        ChewingBridge.chewing.handleDefault('4')

        // 蜂膠
        ChewingBridge.chewing.handleLeft()
        ChewingBridge.chewing.handleLeft()
        ChewingBridge.chewing.handleLeft()
        ChewingBridge.chewing.candOpen()
        val candidateString: String = ChewingBridge.chewing.candStringByIndexStatic(0)
        assertEquals("蜂膠", candidateString)
        ChewingBridge.chewing.candChooseByIndex(0)
        ChewingBridge.chewing.commitPreeditBuf()
        val commitString: String = ChewingBridge.chewing.commitString()
        assertEquals("密蜂膠代", commitString)
        assertEquals(0, ChewingBridge.chewing.candClose())
    }

    @Test
    fun validCandListNext() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setMaxChiSymbolLen(10)
        ChewingBridge.chewing.setCandPerPage(10)
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        // 零用金 零用 零
        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('6')

        ChewingBridge.chewing.handleDefault('m')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('4')

        ChewingBridge.chewing.handleDefault('r')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('p')
        ChewingBridge.chewing.handleSpace()

        ChewingBridge.chewing.handleHome()
        ChewingBridge.chewing.candOpen()

        assertEquals("零用金", ChewingBridge.chewing.candStringByIndexStatic(0))
        assertEquals(true, ChewingBridge.chewing.candListHasNext())
        assertEquals(0, ChewingBridge.chewing.candListNext())
        assertEquals("零用", ChewingBridge.chewing.candStringByIndexStatic(0))
        assertEquals(true, ChewingBridge.chewing.candListHasNext())
        assertEquals(0, ChewingBridge.chewing.candListNext())
        assertEquals("零", ChewingBridge.chewing.candStringByIndexStatic(0))
        assertEquals(-1, ChewingBridge.chewing.candListNext())
        assertEquals(false, ChewingBridge.chewing.candListHasNext())

        ChewingBridge.chewing.candListLast()
        assertEquals("零", ChewingBridge.chewing.candStringByIndexStatic(0))

        ChewingBridge.chewing.candListFirst()
        assertEquals("零用金", ChewingBridge.chewing.candStringByIndexStatic(0))
        assertEquals(0, ChewingBridge.chewing.candClose())
    }

    @Test
    fun switchToHsuLayout() {
        val newKeyboardType = ChewingBridge.chewing.convKBStr2Num("KB_HSU")
        ChewingBridge.chewing.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.chewing.getKBType()
        val currentKeyboardTypeString = ChewingBridge.chewing.getKBString()
        assertEquals(1, currentKeyboardType)
        assertEquals("KB_HSU", currentKeyboardTypeString)

        ChewingBridge.chewing.handleDefault('l')
        ChewingBridge.chewing.handleDefault('l')
        assertEquals("ㄌㄥ", ChewingBridge.chewing.bopomofoStringStatic())
        ChewingBridge.chewing.handleDefault('f')
        assertEquals("冷", ChewingBridge.chewing.bufferString())
        ChewingBridge.chewing.handleDefault('d')
        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('l')
        ChewingBridge.chewing.handleDefault('j')
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("冷凍", ChewingBridge.chewing.commitString())
    }

    @Test
    fun switchToColemakAnsiLayout() {
        val newKeyboardType = ChewingBridge.chewing.convKBStr2Num("KB_COLEMAK_DH_ANSI")
        ChewingBridge.chewing.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.chewing.getKBType()
        val currentKeyboardTypeString = ChewingBridge.chewing.getKBString()
        assertEquals(13, currentKeyboardType)
        assertEquals("KB_COLEMAK_DH_ANSI", currentKeyboardTypeString)

        ChewingBridge.chewing.handleDefault('c')
        ChewingBridge.chewing.handleDefault('/')
        assertEquals("ㄌㄥ", ChewingBridge.chewing.bopomofoStringStatic())
        ChewingBridge.chewing.handleDefault('3')
        assertEquals("冷", ChewingBridge.chewing.bufferString())
        ChewingBridge.chewing.handleDefault('2')
        ChewingBridge.chewing.handleDefault('n')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('4')
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("冷凍", ChewingBridge.chewing.commitString())
    }

    @Test
    fun switchToColemakOrtholinearLayout() {
        val newKeyboardType = ChewingBridge.chewing.convKBStr2Num("KB_COLEMAK_DH_ORTH")
        ChewingBridge.chewing.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.chewing.getKBType()
        val currentKeyboardTypeString = ChewingBridge.chewing.getKBString()
        assertEquals(14, currentKeyboardType)
        assertEquals("KB_COLEMAK_DH_ORTH", currentKeyboardTypeString)

        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('/')
        assertEquals("ㄌㄥ", ChewingBridge.chewing.bopomofoStringStatic())
        ChewingBridge.chewing.handleDefault('3')
        assertEquals("冷", ChewingBridge.chewing.bufferString())
        ChewingBridge.chewing.handleDefault('2')
        ChewingBridge.chewing.handleDefault('n')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('4')
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("冷凍", ChewingBridge.chewing.commitString())
    }

    @Test
    fun switchToDvorakHsuLayout() {
        val newKeyboardType = ChewingBridge.chewing.convKBStr2Num("KB_DVORAK_HSU")
        ChewingBridge.chewing.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.chewing.getKBType()
        val currentKeyboardTypeString = ChewingBridge.chewing.getKBString()
        assertEquals(7, currentKeyboardType)
        assertEquals("KB_DVORAK_HSU", currentKeyboardTypeString)

        // test ChewingUtil.dvorakToQwertyKeyMapping()
        assertEquals('c', ChewingUtil.dvorakToQwertyKeyMapping('j'))
        assertEquals('p', ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals('1', ChewingUtil.dvorakToQwertyKeyMapping('1'))
        assertEquals('!', ChewingUtil.dvorakToQwertyKeyMapping('!'))

        ChewingBridge.chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        ChewingBridge.chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals("ㄌㄥ", ChewingBridge.chewing.bopomofoStringStatic())
        // ˇ
        ChewingBridge.chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('f'))
        assertEquals("冷", ChewingBridge.chewing.bufferString())
        ChewingBridge.chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('d'))
        ChewingBridge.chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('x'))
        ChewingBridge.chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('l'))
        assertEquals("ㄉㄨㄥ", ChewingBridge.chewing.bopomofoStringStatic())
        // ˋ
        ChewingBridge.chewing.handleDefault(ChewingUtil.dvorakToQwertyKeyMapping('j'))
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("冷凍", ChewingBridge.chewing.commitString())
    }

    @Test
    fun validCandidateWindowOpenClose() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setMaxChiSymbolLen(10)
        ChewingBridge.chewing.setCandPerPage(10)
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        ChewingBridge.chewing.setSpaceAsSelection(1)

        // 零
        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('u')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('6')

        ChewingBridge.chewing.handleHome()
        // candidate window opened here
        ChewingBridge.chewing.handleSpace()
        assertTrue(ChewingBridge.chewing.candTotalChoice() > 0)
        assertEquals(true, ChewingUtil.candidateWindowOpened())
        // candidate window closed here (after I picker the first candidate)
        ChewingBridge.chewing.handleDefault('1')
        assertEquals(0, ChewingBridge.chewing.candTotalChoice())
        assertEquals(true, ChewingUtil.candidateWindowClosed())
    }

    @Test
    fun switchToEten26Layout() {
        val newKeyboardType = ChewingBridge.chewing.convKBStr2Num("KB_ET26")
        ChewingBridge.chewing.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.chewing.getKBType()
        val currentKeyboardTypeString = ChewingBridge.chewing.getKBString()
        assertEquals(5, currentKeyboardType)
        assertEquals("KB_ET26", currentKeyboardTypeString)

        ChewingBridge.chewing.handleDefault('l')
        ChewingBridge.chewing.handleDefault('l')
        assertEquals("ㄌㄥ", ChewingBridge.chewing.bopomofoStringStatic())
        ChewingBridge.chewing.handleDefault('j')
        assertEquals("冷", ChewingBridge.chewing.bufferString())
        ChewingBridge.chewing.handleDefault('d')
        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('l')
        ChewingBridge.chewing.handleDefault('k')
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("冷凍", ChewingBridge.chewing.commitString())
    }

    @Test
    fun switchToDaChenLayout() {
        val newKeyboardType = ChewingBridge.chewing.convKBStr2Num("KB_DEFAULT")
        ChewingBridge.chewing.setKBType(newKeyboardType)
        val currentKeyboardType = ChewingBridge.chewing.getKBType()
        val currentKeyboardTypeString = ChewingBridge.chewing.getKBString()
        assertEquals(0, currentKeyboardType)
        assertEquals("KB_DEFAULT", currentKeyboardTypeString)

        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('/')
        assertEquals("ㄌㄥ", ChewingBridge.chewing.bopomofoStringStatic())
        ChewingBridge.chewing.handleDefault('3')
        assertEquals("冷", ChewingBridge.chewing.bufferString())
        ChewingBridge.chewing.handleDefault('2')
        ChewingBridge.chewing.handleDefault('j')
        ChewingBridge.chewing.handleDefault('/')
        ChewingBridge.chewing.handleDefault('4')
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("冷凍", ChewingBridge.chewing.commitString())
    }

    @Test
    fun switchToSymbolSelectionMode() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setMaxChiSymbolLen(10)
        ChewingBridge.chewing.setCandPerPage(10)
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        ChewingBridge.chewing.handleDefault('`')
        ChewingBridge.chewing.candOpen()
        assertEquals(22, ChewingBridge.chewing.candTotalChoice())
        assertEquals("…", ChewingBridge.chewing.candStringByIndexStatic(0))
        assertEquals("※", ChewingBridge.chewing.candStringByIndexStatic(1))
        assertEquals("常用符號", ChewingBridge.chewing.candStringByIndexStatic(2))
        assertEquals("雙線框", ChewingBridge.chewing.candStringByIndexStatic(10))
        assertEquals("線段", ChewingBridge.chewing.candStringByIndexStatic(12))
        ChewingBridge.chewing.handleDefault('1')
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("…", ChewingBridge.chewing.commitString())

        // 換頁到「雙線框」
        // keyboardless API 版
        ChewingBridge.chewing.handleDefault('`')
        ChewingBridge.chewing.candChooseByIndex(10)
        assertEquals(29, ChewingBridge.chewing.candTotalChoice())
        assertEquals("╔", ChewingBridge.chewing.candStringByIndexStatic(0))
        ChewingBridge.chewing.candChooseByIndex(0)
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("╔", ChewingBridge.chewing.commitString())

        // 模擬鍵盤操作版
        ChewingBridge.chewing.handleDefault('`')
        // next page
        ChewingBridge.chewing.handleRight()
        ChewingBridge.chewing.handleDefault('1')
        assertEquals(29, ChewingBridge.chewing.candTotalChoice())
        ChewingBridge.chewing.handleDefault('1')
        ChewingBridge.chewing.commitPreeditBuf()
        ChewingBridge.chewing.candClose()
        assertEquals("╔", ChewingBridge.chewing.commitString())

        val selKeys: IntArray =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }
                .toIntArray()
        ChewingBridge.chewing.setSelKey(selKeys, 10)

        ChewingBridge.chewing.handleDefault('`')
        ChewingBridge.chewing.handleDefault('3')
        ChewingBridge.chewing.handleDefault('1')
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("，", ChewingBridge.chewing.commitString())

        ChewingBridge.chewing.setSpaceAsSelection(1)
        ChewingBridge.chewing.handleDefault('1')
        ChewingBridge.chewing.handleDefault('l')
        ChewingBridge.chewing.handleDefault('3')
        ChewingBridge.chewing.handleHome()
        ChewingBridge.chewing.handleSpace()
        ChewingBridge.chewing.handleDefault('3')
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals("飽", ChewingBridge.chewing.commitString())
    }

    @Test
    fun testCommitCheck() {
        ChewingBridge.chewing.setChiEngMode(ChiEngMode.CHINESE.mode)
        ChewingBridge.chewing.setMaxChiSymbolLen(10)
        assertEquals(10, ChewingBridge.chewing.getMaxChiSymbolLen())
        ChewingBridge.chewing.setCandPerPage(10)
        assertEquals(10, ChewingBridge.chewing.getMaxChiSymbolLen())
        ChewingBridge.chewing.setPhraseChoiceRearward(0)
        assertEquals(10, ChewingBridge.chewing.getMaxChiSymbolLen())
        ChewingBridge.chewing.setSpaceAsSelection(1)
        assertEquals(10, ChewingBridge.chewing.getMaxChiSymbolLen())
        val newKeyboardType = ChewingBridge.chewing.convKBStr2Num("KB_HSU")
        ChewingBridge.chewing.setKBType(newKeyboardType)
        assertEquals(10, ChewingBridge.chewing.getMaxChiSymbolLen())

        ChewingBridge.chewing.handleDefault('l')
        ChewingBridge.chewing.handleDefault('w')
        ChewingBridge.chewing.handleDefault('f')
        ChewingBridge.chewing.handleDefault('c')
        ChewingBridge.chewing.handleDefault('x')
        ChewingBridge.chewing.handleDefault('f')

        assertEquals("老鼠", ChewingBridge.chewing.bufferString())

        repeat(4) {
            ChewingBridge.chewing.handleDefault('m')
            ChewingBridge.chewing.handleDefault('w')
            ChewingBridge.chewing.handleSpace()
            ChewingBridge.chewing.handleDefault('m')
            ChewingBridge.chewing.handleDefault('e')
            ChewingBridge.chewing.handleSpace()
        }

        assertEquals("老鼠貓咪貓咪貓咪貓咪", ChewingBridge.chewing.bufferString())

        ChewingBridge.chewing.handleDefault('m')
        ChewingBridge.chewing.handleDefault('w')
        ChewingBridge.chewing.handleSpace() // 超出 maxChiSymbolLen，此時應該觸發送出最前端詞「老鼠」
        assertEquals("貓咪貓咪貓咪貓咪貓", ChewingBridge.chewing.bufferString())
        assertEquals("老鼠", ChewingBridge.chewing.commitString())
        assertEquals(1, ChewingBridge.chewing.commitCheck())

        ChewingBridge.chewing.handleDefault('m')
        ChewingBridge.chewing.handleDefault('e')
        ChewingBridge.chewing.handleSpace()

        assertEquals("貓咪貓咪貓咪貓咪貓咪", ChewingBridge.chewing.bufferString())
        assertEquals(0, ChewingBridge.chewing.commitCheck())
        ChewingBridge.chewing.commitPreeditBuf()
        assertEquals(1, ChewingBridge.chewing.commitCheck())
    }

    @Test
    fun testSetGetShapeMode() {
        ChewingBridge.chewing.setShapeMode(0)
        assertEquals(0, ChewingBridge.chewing.getShapeMode())
        ChewingBridge.chewing.setShapeMode(1)
        assertEquals(1, ChewingBridge.chewing.getShapeMode())
    }

    @After
    fun deleteChewingEngine() {
        // Close Chewing
        ChewingBridge.chewing.delete()
    }
}