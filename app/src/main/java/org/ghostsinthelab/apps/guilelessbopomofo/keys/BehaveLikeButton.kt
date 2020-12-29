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

package org.ghostsinthelab.apps.guilelessbopomofo.keys

import android.view.HapticFeedbackConstants
import android.view.View
import org.ghostsinthelab.apps.guilelessbopomofo.*

interface BehaveLikeButton<T : View> {
    fun setBackMainLayoutOnClickListener() {
        this as View
        this.setOnClickListener {
            ChewingEngine.endCandidateChoice()
            GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.switchToMainLayout()
        }
    }

    fun setCandidateButtonOnClickListener(
        guilelessBopomofoService: GuilelessBopomofoService,
        data: Int
    ) {
        this as View
        this.setOnClickListener {
            ChewingEngine.apply {
                candChooseByIndex(data)
                endCandidateChoice()
            }
            guilelessBopomofoService.doneCandidateChoice()
        }
    }

    fun setKeyImageButtonPuncOnLongClickListener() {
        this as KeyImageButton
        this.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            ChewingEngine.openPuncCandidates()
            GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.switchToCandidatesLayout()
            return@setOnLongClickListener true
        }
    }

    fun setModeSwitchButtonOnClickListener() {
        this as KeyImageButton
        this.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val chewingChiEngMode = ChewingEngine.getChiEngMode()
            if (chewingChiEngMode == CHINESE_MODE) {
                ChewingEngine.setChiEngMode(SYMBOL_MODE)
                GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.switchToQwertyLayout()
            } else {
                ChewingEngine.setChiEngMode(CHINESE_MODE)
                GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.switchToBopomofoLayout()
            }
        }
    }
}