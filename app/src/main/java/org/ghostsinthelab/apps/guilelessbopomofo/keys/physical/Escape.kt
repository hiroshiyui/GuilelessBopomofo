/*
 * Guileless Bopomofo
 * Copyright (C) 2025 YOU, HUI-HONG
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

package org.ghostsinthelab.apps.guilelessbopomofo.keys.physical

import android.content.Context
import android.content.SharedPreferences
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.greenrobot.eventbus.EventBus

class Escape : PhysicalKeyHandler {
    override fun onKeyDown(
        context: Context,
        keyCode: Int,
        event: KeyEvent?,
    ): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("GuilelessBopomofoService", AppCompatActivity.MODE_PRIVATE)

        if (event?.isAltPressed == true && sharedPreferences.getBoolean("user_enhanced_compat_physical_keyboard", false) == true) {
            EventBus.getDefault().post(Events.ToggleForceCompactLayout())
            return true
        }

        EventBus.getDefault().post(Events.ExitKeyboardSubLayouts())
        return true
    }
}