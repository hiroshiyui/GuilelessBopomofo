/*
 * Guileless Bopomofo
 * Copyright (C) 2025 YOU, HUI-HONG
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

const val SYMBOL_MODE: Int = 0
const val CHINESE_MODE: Int = 1

class Chewing {
    // Chewing context pointer, represent its address as a Long
    var context: Long = 0

    init {
        try {
            System.loadLibrary("libchewing_android_jni")
        } catch (exception: Throwable) {
            exception.printStackTrace()
            throw ChewingInitException
        }
    }

    object ChewingInitException : Throwable() {
        private fun readResolve(): Any = ChewingInitException
        override val message: String = "Unable to initialize Chewing"
    }

    private external fun chewingNew(dataPath: String): Long
    external fun ack(chewingCtx: Long = context): Int
    external fun bopomofoStringStatic(chewingCtx: Long = context): String
    external fun bufferCheck(chewingCtx: Long = context): Int
    external fun bufferLen(chewingCtx: Long = context): Int
    external fun bufferString(chewingCtx: Long = context): String
    external fun bufferStringStatic(chewingCtx: Long = context): String
    external fun candChoicePerPage(chewingCtx: Long = context): Int
    external fun candChooseByIndex(index: Int, chewingCtx: Long = context): Int
    external fun candClose(chewingCtx: Long = context): Int
    external fun candCurrentPage(chewingCtx: Long = context): Int
    external fun candEnumerate(chewingCtx: Long = context)
    external fun candHasNext(chewingCtx: Long = context): Int
    external fun candListFirst(chewingCtx: Long = context): Int
    external fun candListHasNext(chewingCtx: Long = context): Boolean
    external fun candListHasPrev(chewingCtx: Long = context): Boolean
    external fun candListLast(chewingCtx: Long = context): Int
    external fun candListNext(chewingCtx: Long = context): Int
    external fun candListPrev(chewingCtx: Long = context): Int
    external fun candOpen(chewingCtx: Long = context): Int
    external fun candString(chewingCtx: Long = context): String
    external fun candStringByIndexStatic(index: Int, chewingCtx: Long = context): String
    external fun candStringStatic(chewingCtx: Long = context): String
    external fun candTotalChoice(chewingCtx: Long = context): Int
    external fun candTotalPage(chewingCtx: Long = context): Int
    external fun cleanBopomofoBuf(chewingCtx: Long = context): Int
    external fun cleanPreeditBuf(chewingCtx: Long = context): Int
    external fun commitCheck(chewingCtx: Long = context): Int
    external fun commitPreeditBuf(chewingCtx: Long = context): Int
    external fun commitString(chewingCtx: Long = context): String
    external fun commitStringStatic(chewingCtx: Long = context): String
    external fun convKBStr2Num(keyboardString: String): Int
    external fun cursorCurrent(chewingCtx: Long = context): Int
    external fun delete(chewingCtx: Long = context)
    external fun free(resourcePtr: Long)
    external fun getCandPerPage(chewingCtx: Long = context): Int
    external fun getChiEngMode(chewingCtx: Long = context): Int
    external fun getKBString(chewingCtx: Long = context): String
    external fun getKBType(chewingCtx: Long = context): Int
    external fun getMaxChiSymbolLen(chewingCtx: Long = context): Int
    external fun getPhraseChoiceRearward(chewingCtx: Long = context): Int
    external fun getSelKey(chewingCtx: Long = context): IntArray
    external fun getSpaceAsSelection(chewingCtx: Long = context): Int
    external fun getShapeMode(chewingCtx: Long = context): Int
    external fun handleBackspace(chewingCtx: Long = context)
    external fun handleDefault(key: Char, chewingCtx: Long = context)
    external fun handleEnd(chewingCtx: Long = context)
    external fun handleEnter(chewingCtx: Long = context)
    external fun handleEsc(chewingCtx: Long = context)
    external fun handleHome(chewingCtx: Long = context)
    external fun handleLeft(chewingCtx: Long = context)
    external fun handlePageDown(chewingCtx: Long = context)
    external fun handlePageUp(chewingCtx: Long = context)
    external fun handleRight(chewingCtx: Long = context)
    external fun handleSpace(chewingCtx: Long = context)
    external fun setCandPerPage(candidates: Int, chewingCtx: Long = context)
    external fun setChiEngMode(mode: Int, chewingCtx: Long = context)
    external fun setEasySymbolInput(mode: Int, chewingCtx: Long = context)
    external fun setKBType(type: Int, chewingCtx: Long = context): Int
    external fun setMaxChiSymbolLen(length: Int, chewingCtx: Long = context)
    external fun setPhraseChoiceRearward(mode: Int, chewingCtx: Long = context)
    external fun setSelKey(selKeys: IntArray, length: Int, chewingCtx: Long = context)
    external fun setSpaceAsSelection(mode: Int, chewingCtx: Long = context)
    external fun setShapeMode(mode: Int, chewingCtx: Long = context)

    fun connect(dataPath: String): Long {
        context = chewingNew(dataPath)
        return context
    }
}