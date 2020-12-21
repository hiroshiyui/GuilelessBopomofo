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

object ChewingEngine {
    // Chewing context pointer, represent its address as a Long
    var context: Long = 0

    init {
        System.loadLibrary("chewing")
    }

    // Chewing API JNIs
    external fun chewingNew(dataPath: String): Long
    external fun bopomofoStringStatic(chewingCtx: Long = context): String
    external fun bufferCheck(chewingCtx: Long = context): Int
    external fun bufferLen(chewingCtx: Long = context): Int
    external fun bufferString(chewingCtx: Long = context): String
    external fun bufferStringStatic(chewingCtx: Long = context): String
    external fun candChooseByIndex(index: Int, chewingCtx: Long = context): Int
    external fun candClose(chewingCtx: Long = context): Int
    external fun candListFirst(chewingCtx: Long = context): Int
    external fun candListHasNext(chewingCtx: Long = context): Boolean
    external fun candListHasPrev(chewingCtx: Long = context): Boolean
    external fun candListLast(chewingCtx: Long = context): Int
    external fun candListNext(chewingCtx: Long = context): Int
    external fun candListPrev(chewingCtx: Long = context): Int
    external fun candOpen(chewingCtx: Long = context): Int
    external fun candStringByIndexStatic(index: Int, chewingCtx: Long = context): String
    external fun candTotalChoice(chewingCtx: Long = context): Int
    external fun cleanBopomofoBuf(chewingCtx: Long = context): Int
    external fun cleanPreeditBuf(chewingCtx: Long = context): Int
    external fun commitCheck(chewingCtx: Long = context): Int
    external fun commitPreeditBuf(chewingCtx: Long = context): Int
    external fun commitString(chewingCtx: Long = context): String
    external fun commitStringStatic(chewingCtx: Long = context): String
    external fun convKBStr2Num(keyboardString: String): Int
    external fun delete(chewingCtx: Long = context)
    external fun free(resourcePtr: Long)
    external fun getCandPerPage(chewingCtx: Long = context): Int
    external fun getChiEngMode(chewingCtx: Long = context): Int
    external fun getKBString(chewingCtx: Long = context): String
    external fun getKBType(chewingCtx: Long = context): Int
    external fun getMaxChiSymbolLen(chewingCtx: Long = context): Int
    external fun getPhraseChoiceRearward(chewingCtx: Long = context): Boolean
    external fun getSelKey(chewingCtx: Long = context): Long
    external fun getSpaceAsSelection(chewingCtx: Long = context): Int
    external fun handleBackspace(chewingCtx: Long = context)
    external fun handleDefault(key: Char, chewingCtx: Long = context)
    external fun handleEnd(chewingCtx: Long = context)
    external fun handleEnter(chewingCtx: Long = context)
    external fun handleHome(chewingCtx: Long = context)
    external fun handleLeft(chewingCtx: Long = context)
    external fun handleRight(chewingCtx: Long = context)
    external fun handleSpace(chewingCtx: Long = context)
    external fun setCandPerPage(candidates: Int, chewingCtx: Long = context)
    external fun setChiEngMode(mode: Int, chewingCtx: Long = context)
    external fun setEasySymbolInput(mode: Int, chewingCtx: Long = context)
    external fun setKBType(type: Int, chewingCtx: Long = context): Int
    external fun setMaxChiSymbolLen(length: Int, chewingCtx: Long = context)
    external fun setPhraseChoiceRearward(boolean: Boolean, chewingCtx: Long = context)
    external fun setSelKey(selKeys: List<Int>, length: Int, chewingCtx: Long = context)
    external fun setSpaceAsSelection(mode: Int, chewingCtx: Long = context)

    fun start(dataPath: String): Long {
        context = chewingNew(dataPath)
        return context
    }

    // derived methods
    fun hasCandidates(): Boolean {
        if (candStringByIndexStatic(0).isEmpty()) {
            return false
        }
        return true
    }

    fun anyPreeditBufferIsNotEmpty(): Boolean {
        if (bufferStringStatic()
                .isNotEmpty() || bopomofoStringStatic().isNotEmpty()
        ) {
            return true
        }
        return false
    }

    fun openSymbolCandidates() {
        handleDefault('`')
        candOpen()
    }

    fun openPuncCandidates() {
        candClose()
        // 「常用符號」
        handleDefault('`')
        handleDefault('3')
        candOpen()
    }

    fun endCandidateChoice() {
        candClose()
        handleEnd()
    }

    fun moveToPreEditBufferOffset(offset: Int) {
        // close if any been opened candidate window first
        candClose()
        // move to first character
        handleHome()
        // move to clicked character
        repeat(offset) { handleRight() }
        // open candidates window
        candOpen()
    }

    // simulates [Shift] + [,]
    fun handleShiftComma() {
        setEasySymbolInput(1)
        handleDefault(',')
        setEasySymbolInput(0)
    }
}