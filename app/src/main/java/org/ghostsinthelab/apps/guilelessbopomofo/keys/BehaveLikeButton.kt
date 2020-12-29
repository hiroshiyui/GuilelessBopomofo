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

import android.content.Context
import android.os.Build
import android.os.IBinder
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputMethodManager
import org.ghostsinthelab.apps.guilelessbopomofo.*

interface BehaveLikeButton<T : View> {
    fun setBackMainLayoutOnClickListener(guilelessBopomofoService: GuilelessBopomofoService) {
        this as View
        this.setOnClickListener {
            ChewingEngine.endCandidateChoice()
            guilelessBopomofoService.viewBinding.keyboardPanel.switchToMainLayout(
                guilelessBopomofoService
            )
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

    fun setImeSwitchButtonOnClickListener(guilelessBopomofoService: GuilelessBopomofoService) {
        this as KeyImageButton
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                guilelessBopomofoService.switchToNextInputMethod(false)
            }
        } else {
            // backward compatibility, support IME switch on legacy devices
            val imm =
                guilelessBopomofoService.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val imeToken: IBinder? = guilelessBopomofoService.window?.let {
                it.window?.attributes?.token
            }
            this.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                imm.switchToNextInputMethod(imeToken, false)
            }
        }
    }

    fun setImeSwitchButtonOnLongClickListener(guilelessBopomofoService: GuilelessBopomofoService) {
        this as KeyImageButton
        this.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            val imm =
                guilelessBopomofoService.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
            return@setOnLongClickListener true
        }
    }

    fun setKeyImageButtonPuncOnLongClickListener(guilelessBopomofoService: GuilelessBopomofoService) {
        this as KeyImageButton
        this.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            ChewingEngine.openPuncCandidates()
            guilelessBopomofoService.viewBinding.keyboardPanel.switchToCandidatesLayout(
                guilelessBopomofoService
            )
            return@setOnLongClickListener true
        }
    }

    fun setModeSwitchButtonOnClickListener(guilelessBopomofoService: GuilelessBopomofoService) {
        this as KeyImageButton
        this.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val chewingChiEngMode = ChewingEngine.getChiEngMode()
            if (chewingChiEngMode == CHINESE_MODE) {
                ChewingEngine.setChiEngMode(SYMBOL_MODE)
                guilelessBopomofoService.viewBinding.keyboardPanel.switchToQwertyLayout(
                    guilelessBopomofoService
                )
            } else {
                ChewingEngine.setChiEngMode(CHINESE_MODE)
                guilelessBopomofoService.viewBinding.keyboardPanel.switchToBopomofoLayout(
                    guilelessBopomofoService
                )
            }
        }
    }
}