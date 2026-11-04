/*
 * Guileless Bopomofo
 * Copyright (C) 2026.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
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

import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService
import org.ghostsinthelab.apps.guilelessbopomofo.events.ImeAction
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ImeHost
import org.ghostsinthelab.apps.guilelessbopomofo.keys.SwipeDirection
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SwipeKeySpec

/**
 * A phone-keypad key of the compact Dachen keyboard: a tap types [spec]'s tap symbol, a
 * swipe the symbol that way, if there is one. Unlike [CharacterKey] it types as the finger
 * lifts, since only then is it known which way it went.
 */
class SwipeCharacterKey(private val spec: SwipeKeySpec) : VirtualKey {
    override val detectsSwipes: Boolean = true

    override fun onDown(host: ImeHost) {
        performVibration(host.context, GuilelessBopomofoService.userHapticFeedbackStrength)
    }

    override fun onTap(host: ImeHost) {
        host.type(spec.tap)
    }

    override fun onSwipe(host: ImeHost, direction: SwipeDirection) {
        // A way with no symbol on it types nothing.
        spec.glyphFor(direction)?.let { host.type(it) }
    }

    private fun ImeHost.type(glyph: BopomofoGlyph) {
        dispatch(ImeAction.PrintingKeyDown(glyph.dachenKeyCode))
    }
}
