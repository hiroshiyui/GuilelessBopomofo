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

class ChewingUtil {
    companion object {
        fun candWindowOpened(): Boolean {
            if (Chewing.candTotalChoice() > 0) {
                return true
            }
            return false
        }

        fun candWindowClosed(): Boolean {
            if (Chewing.candTotalChoice() > 0) {
                return false
            }
            return true
        }

        fun anyPreeditBufferIsNotEmpty(): Boolean {
            if (Chewing.bufferStringStatic()
                    .isNotEmpty() || Chewing.bopomofoStringStatic().isNotEmpty()
            ) {
                return true
            }
            return false
        }

        fun openSymbolCandidates() {
            Chewing.candClose()
            Chewing.handleDefault('`')
            Chewing.candOpen()
        }

        fun openPuncCandidates() {
            Chewing.candClose()
            Chewing.handleDefault('`')
            // 「常用符號」
            // 選字鍵不能保證一定是 0-9，用 candChooseByIndex() 相對妥當
            Chewing.candChooseByIndex(2)
            Chewing.candOpen()
        }

        fun getCandidatesByPage(page: Int = 0): List<Candidate> {
            val fromOffset = page * Chewing.candChoicePerPage()
            val toOffset = fromOffset + Chewing.candChoicePerPage() - 1
            val candidatesInThisPage: MutableList<Candidate> = mutableListOf()
            val selKeys = Chewing.getSelKey()

            for (i in fromOffset..toOffset) {
                if (Chewing.candStringByIndexStatic(i).isNotBlank()) {
                    val candidate = getCandidate(index = i)
                    candidatesInThisPage.add(candidate)
                }
            }

            // binding selection key
            candidatesInThisPage.mapIndexed { index, candidate ->
                candidate.selectionKey = selKeys[index].toChar()
            }

            return candidatesInThisPage.toList()
        }

        private fun getCandidate(index: Int): Candidate {
            val candidate = Candidate(index)
            candidate.candidateString = Chewing.candStringByIndexStatic(index)

            return candidate
        }

        fun moveToPreEditBufferOffset(offset: Int) {
            // close if any been opened candidate window first
            Chewing.candClose()
            // move to first character
            Chewing.handleHome()
            // move to clicked character
            repeat(offset) { Chewing.handleRight() }
            // open candidates window
            Chewing.candOpen()
        }

        // simulates [Shift] + [,]
        fun handleShiftComma() {
            if (Chewing.getChiEngMode() == CHINESE_MODE) {
                Chewing.setEasySymbolInput(1)
                Chewing.handleDefault(',')
                Chewing.setEasySymbolInput(0)
            } else {
                Chewing.handleDefault(',')
            }
        }

        private val dvorakKeysList: List<Char> = listOf(
            '\'', '\"', ',', '<', '.', '>', 'p', 'P', 'y', 'Y', 'f', 'F', 'g', 'G',
            'c', 'C', 'r', 'R', 'l', 'L', '/', '?', '=', '+', '\\', '|',
            'a', 'A', 'o', 'O', 'e', 'E', 'u', 'U', 'i', 'I', 'd', 'D', 'h', 'H',
            't', 'T', 'n', 'N', 's', 'S', '-', '_',
            ';', ':', 'q', 'Q', 'j', 'J', 'k', 'K', 'x', 'X', 'b', 'B', 'm', 'M',
            'w', 'W', 'v', 'V', 'z', 'Z'
        )

        private val qwertyKeysList: List<Char> = listOf(
            'q', 'Q', 'w', 'W', 'e', 'E', 'r', 'R', 't', 'T', 'y', 'Y', 'u', 'U',
            'i', 'I', 'o', 'O', 'p', 'P', '[', '{', ']', '}', '\\', '|',
            'a', 'A', 's', 'S', 'd', 'D', 'f', 'F', 'g', 'G', 'h', 'H', 'j', 'J',
            'k', 'K', 'l', 'L', ';', ':', '\'', '\"',
            'z', 'Z', 'x', 'X', 'c', 'C', 'v', 'V', 'b', 'B', 'n', 'N', 'm', 'M',
            ',', '<', '.', '>', '/', '?'
        )

        private val qwertyKeyMapping: Map<Char, Char> = dvorakKeysList.zip(qwertyKeysList).toMap()

        fun dvorakToQwertyKeyMapping(key: Char): Char {
            qwertyKeyMapping[key]?.let {
                return it
            }
            // if we can't find a mapping, then return the original key as-is
            return key
        }
    }
}