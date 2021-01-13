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
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.inputmethod.InputMethodManager
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoServiceContext

class ImeSwitchFunctionKey(context: Context, attrs: AttributeSet) :
    KeyImageButton(context, attrs) {

    init {
        this.setOnClickListener {
            Log.v(LOGTAG, "KeyImageButtonImeSwitch")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                GuilelessBopomofoServiceContext.serviceInstance.switchToNextInputMethod(false)
            } else {
                // backward compatibility, support IME switch on legacy devices
                val imm =
                    GuilelessBopomofoServiceContext.serviceInstance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val imeToken: IBinder? =
                    GuilelessBopomofoServiceContext.serviceInstance.window?.let {
                        it.window?.attributes?.token
                    }
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                imm.switchToNextInputMethod(imeToken, false)
            }
        }

        this.setOnLongClickListener {
            Log.v(LOGTAG, "KeyImageButtonImeSwitch")
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            val imm =
                GuilelessBopomofoServiceContext.serviceInstance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
            return@setOnLongClickListener true
        }
    }
}