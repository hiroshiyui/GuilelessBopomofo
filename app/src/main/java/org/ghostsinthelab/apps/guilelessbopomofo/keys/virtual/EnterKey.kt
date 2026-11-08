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

import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.enums.EnterKeyIntent
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ImeHost
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable

/**
 * The on-screen Enter: commits the buffer, or does what the text field wants Enter to do,
 * a line break in a multiple line field. The icon on it follows, see [org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.enterKeyIcon].
 */
object EnterKey : VirtualKey {
    override fun onDown(host: ImeHost) {
        performVibration(host.context, Vibratable.VibrationStrength.NORMAL)
    }

    override fun onTap(host: ImeHost) {
        ChewingUtil.handleEnterAction(host, EnterKeyIntent.DEFAULT)
    }
}
