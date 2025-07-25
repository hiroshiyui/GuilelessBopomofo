/*
 * Guileless Bopomofo
 * Copyright (C) 2025.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus

class ChewingUtil {
    companion object {
        fun listOfDataFiles(): List<String> {
            val dataFiles = listOf("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")
            return dataFiles
        }

        fun candidateWindowOpened(): Boolean {
            return ChewingBridge.chewing.candTotalChoice() > 0
        }

        fun candidateWindowClosed(): Boolean {
            return ChewingBridge.chewing.candTotalChoice() <= 0
        }

        fun anyBufferIsNotEmpty(): Boolean {
            return ChewingBridge.chewing.bufferStringStatic()
                .isNotEmpty() || ChewingBridge.chewing.bopomofoStringStatic().isNotEmpty()
        }

        fun openSymbolCandidates() {
            ChewingBridge.chewing.candClose()
            ChewingBridge.chewing.handleDefault('`')
            ChewingBridge.chewing.candOpen()
        }

        fun openFrequentlyUsedCandidates() {
            ChewingBridge.chewing.candClose()
            ChewingBridge.chewing.handleDefault('`')
            // 「常用符號」
            // 選字鍵不能保證一定是 0-9，用 candChooseByIndex() 相對妥當
            ChewingBridge.chewing.candChooseByIndex(2)
            ChewingBridge.chewing.candOpen()
        }

        fun getCandidatesByPage(page: Int = 0): List<Candidate> {
            val fromOffset = page * ChewingBridge.chewing.candChoicePerPage()
            val toOffset = fromOffset + ChewingBridge.chewing.candChoicePerPage() - 1
            val candidatesInThisPage: MutableList<Candidate> = mutableListOf()
            val selKeys = ChewingBridge.chewing.getSelKey()

            for (i in fromOffset..toOffset) {
                if (ChewingBridge.chewing.candStringByIndexStatic(i).isNotBlank()) {
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

        fun openCandidates() {
            ChewingBridge.chewing.candClose()
            ChewingBridge.chewing.candOpen()
            EventBus.getDefault().post(Events.SwitchToLayout(Layout.CANDIDATES))
            return
        }

        private fun getCandidate(index: Int): Candidate {
            val candidate = Candidate(index)
            candidate.candidateString = ChewingBridge.chewing.candStringByIndexStatic(index)

            return candidate
        }

        // simulates [Shift] + [,]
        fun handleShiftComma() {
            // detect the current keyboard type
            val currentKeyboardType = ChewingBridge.chewing.getKBString()

            // the location of comma in KB_DVORAK_HSU (',') is different from generic QWERTY ('w'),
            // just workaround this:
            val commaKeyMapping: Char = if (currentKeyboardType == "KB_DVORAK_HSU") {
                dvorakToQwertyKeyMapping(',')
            } else {
                ','
            }

            if (ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode) {
                ChewingBridge.chewing.setEasySymbolInput(1)
                ChewingBridge.chewing.handleDefault(commaKeyMapping)
                ChewingBridge.chewing.setEasySymbolInput(0)
            } else {
                ChewingBridge.chewing.handleDefault(',')
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

        private val dvorakToQwertyKeyMappingMap: Map<Char, Char> =
            dvorakKeysList.zip(qwertyKeysList).toMap()

        fun dvorakToQwertyKeyMapping(key: Char): Char {
            dvorakToQwertyKeyMappingMap[key]?.let {
                return it
            }
            // if we can't find a mapping, then return the original key as-is
            return key
        }

        private val qwertyToDvorakKeyMappingMap: Map<Char, Char> =
            qwertyKeysList.zip(dvorakKeysList).toMap()

        fun qwertyToDvorakKeyMapping(key: Char): Char {
            qwertyToDvorakKeyMappingMap[key]?.let {
                return it
            }
            // if we can't find a mapping, then return the original key as-is
            return key
        }
    }
}