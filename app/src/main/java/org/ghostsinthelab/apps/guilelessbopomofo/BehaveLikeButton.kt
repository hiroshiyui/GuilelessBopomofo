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

import android.content.Context
import android.os.Build
import android.os.IBinder
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputMethodManager

interface BehaveLikeButton<T : View> {
    fun setBackMainLayoutOnClickListener(imeService: GuilelessBopomofoService) {
        this as View
        this.setOnClickListener {
            imeService.chewingEngine.candClose()
            imeService.chewingEngine.handleEnd()
            imeService.viewBinding.keyboardPanel.switchToMainLayout(imeService)
        }
    }

    fun setCandidateButtonOnClickListener(imeService: GuilelessBopomofoService, data: Int) {
        this as View
        this.setOnClickListener {
            imeService.chewingEngine.apply {
                candChooseByIndex(data)
                candClose()
                handleEnd()
            }
            imeService.viewBinding.apply {
                keyboardPanel.currentCandidatesList = 0
                keyboardView.updateBuffers(imeService)
                keyboardPanel.switchToMainLayout(imeService)
            }
        }
    }

    fun setImeSwitchButtonOnClickListener(imeService: GuilelessBopomofoService) {
        this as KeyImageButton
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                imeService.switchToNextInputMethod(false)
            }
        } else {
            // backward compatibility, support IME switch on legacy devices
            val imm =
                imeService.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val imeToken: IBinder? = imeService.window?.let {
                it.window?.attributes?.token
            }
            this.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                imm.switchToNextInputMethod(imeToken, false)
            }
        }
    }

    fun setImeSwitchButtonOnLongClickListener(imeService: GuilelessBopomofoService) {
        this as KeyImageButton
        this.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            val imm =
                imeService.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
            return@setOnLongClickListener true
        }
    }

    fun setKeyImageButtonPuncOnLongClickListener(imeService: GuilelessBopomofoService) {
        this as KeyImageButton
        this.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            imeService.chewingEngine.apply {
                candClose()
                // 「常用符號」
                handleDefault('`')
                handleDefault('3')
                candOpen()
            }

            imeService.viewBinding.keyboardPanel.switchToCandidatesLayout(imeService)

            return@setOnLongClickListener true
        }
    }
}