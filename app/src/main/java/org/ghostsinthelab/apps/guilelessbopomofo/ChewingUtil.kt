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
            return ChewingBridge.chewing.bufferStringStatic().isNotEmpty() || ChewingBridge.chewing.bopomofoStringStatic().isNotEmpty()
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
            if (ChewingBridge.chewing.getChiEngMode() == ChiEngMode.CHINESE.mode) {
                ChewingBridge.chewing.setEasySymbolInput(1)
                ChewingBridge.chewing.handleDefault('<')
                ChewingBridge.chewing.setEasySymbolInput(0)
            } else {
                ChewingBridge.chewing.handleDefault(',')
            }
        }

    }
}