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
import android.util.AttributeSet
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

class PunctuationFunctionKey(context: Context, attrs: AttributeSet) : KeyImageButton(context, attrs) {
    init {
        // 在大千鍵盤下，標準的逗號鍵會對映到「ㄝ」，這裡的逗號鍵要另外當成特別的「常用符號」功能鍵，
        // 短觸會輸出全形逗號
        this.setOnClickListener {
            performVibrate(Vibratable.VibrationStrength.NORMAL)
            ChewingUtil.handleShiftComma()
            GuilelessBopomofoServiceContext.serviceInstance.viewBinding.let {
                it.textViewPreEditBuffer.update()
                it.textViewBopomofoBuffer.update()
            }
        }

        this.setOnLongClickListener {
            performVibrate(Vibratable.VibrationStrength.STRONG)
            ChewingUtil.openPuncCandidates()
            GuilelessBopomofoServiceContext.serviceInstance.viewBinding.keyboardPanel.switchToCandidatesLayout()
            return@setOnLongClickListener true
        }
    }
}