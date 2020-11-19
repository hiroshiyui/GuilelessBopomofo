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

class ChewingEngine constructor(dataPath: String) {
    // Chewing context pointer, represent its address as a Long
    val context: Long

    init {
        System.loadLibrary("chewing")
        context = chewingNew(dataPath)
    }

    // Chewing API JNIs
    private external fun chewingNew(dataPath: String): Long
    external fun delete(chewingCtx: Long = context)
    external fun free(resourcePtr: Long)
    external fun setChiEngMode(mode: Int, chewingCtx: Long = context)
    external fun getChiEngMode(chewingCtx: Long = context): Int
    external fun setSelKey(selKeys: List<Int>, length: Int, chewingCtx: Long = context)
    external fun getSelKey(chewingCtx: Long = context): Long
    external fun setMaxChiSymbolLen(length: Int, chewingCtx: Long = context)
    external fun getMaxChiSymbolLen(chewingCtx: Long = context): Int
    external fun setCandPerPage(candidates: Int, chewingCtx: Long = context)
    external fun getCandPerPage(chewingCtx: Long = context): Int
    external fun setPhraseChoiceRearward(boolean: Boolean, chewingCtx: Long = context)
    external fun getPhraseChoiceRearward(chewingCtx: Long = context): Boolean
    external fun setKBType(type: Int, chewingCtx: Long = context): Int
    external fun getKBType(chewingCtx: Long = context): Int
    external fun getKBString(chewingCtx: Long = context): String
    external fun convKBStr2Num(keyboardString: String): Int
    external fun handleDefault(key: Char, chewingCtx: Long = context)
    external fun handleEnter(chewingCtx: Long = context)
    external fun handleSpace(chewingCtx: Long = context)
    external fun handleLeft(chewingCtx: Long = context)
    external fun handleRight(chewingCtx: Long = context)
    external fun commitString(chewingCtx: Long = context): String
    external fun commitPreeditBuf(chewingCtx: Long = context): Int
    external fun candOpen(chewingCtx: Long = context): Int
    external fun candTotalChoice(chewingCtx: Long = context): Int
    external fun candStringByIndexStatic(index: Int, chewingCtx: Long = context): String
    external fun candChooseByIndex(index: Int, chewingCtx: Long = context): Int
    external fun candListHasNext(chewingCtx: Long = context): Boolean
    external fun candListNext(chewingCtx: Long = context): Boolean
    external fun bufferString(chewingCtx: Long = context): String
    external fun bopomofoStringStatic(chewingCtx: Long = context): String
}