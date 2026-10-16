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

package org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual

import android.content.Context
import android.view.inputmethod.InputMethodManager
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.ghostsinthelab.apps.guilelessbopomofo.utils.appSharedPreferences
import org.greenrobot.eventbus.EventBus

/** Shown only when the user turns it on, see `USER_ENABLE_IME_SWITCH`. */
object ImeSwitchFunctionKey : VirtualKey {
    override val detectsDoubleTap: Boolean = true
    override val detectsLongPress: Boolean = true

    override fun onDown(context: Context) {
        performVibration(context, Vibratable.VibrationStrength.NORMAL)
    }

    override fun onTap(context: Context) {
        if (!switchesOnDoubleTap(context)) switchNextInputMethod()
    }

    override fun onDoubleTap(context: Context) {
        if (switchesOnDoubleTap(context)) switchNextInputMethod()
    }

    override fun onLongPress(context: Context) {
        performVibration(context, Vibratable.VibrationStrength.STRONG)
        val imm = context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }

    private fun switchesOnDoubleTap(context: Context): Boolean =
        context.appSharedPreferences.getBoolean(USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH, false)

    private fun switchNextInputMethod() {
        EventBus.getDefault().post(Events.SwitchToNextInputMethod())
    }
}
