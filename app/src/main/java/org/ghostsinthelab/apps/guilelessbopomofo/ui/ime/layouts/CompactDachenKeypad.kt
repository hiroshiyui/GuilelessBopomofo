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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts

import org.ghostsinthelab.apps.guilelessbopomofo.keys.SwipeDirection
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.A
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.AI
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.AN
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.ANG
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.AO
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.B
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.CHI
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.CI
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.D
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.E
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.EI
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.EN
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.ENG
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.ER
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.F
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.G
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.H
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.IE
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.J
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.K
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.L
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.M
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.N
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.O
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.OU
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.P
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.Q
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.RI
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.SHI
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.SI
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.T
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.TONE_2
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.TONE_3
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.TONE_4
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.TONE_LIGHT
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.WU
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.X
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.YI
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.YU
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.ZHI
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph.ZI

/** A phone-keypad key: [tap] when tapped, and the others when swiped that way. */
data class SwipeKeySpec(
    val tap: BopomofoGlyph,
    val up: BopomofoGlyph? = null,
    val right: BopomofoGlyph? = null,
    val left: BopomofoGlyph? = null,
    val down: BopomofoGlyph? = null,
) {
    /** What a swipe [direction] types, if anything. */
    fun glyphFor(direction: SwipeDirection): BopomofoGlyph? = when (direction) {
        SwipeDirection.UP -> up
        SwipeDirection.RIGHT -> right
        SwipeDirection.LEFT -> left
        SwipeDirection.DOWN -> down
    }

    val glyphs: List<BopomofoGlyph> get() = listOfNotNull(tap, up, right, left, down)
}

/**
 * The compact Dachen keyboard: Bopomofo on a feature phone's number keys, in the classic
 * Taiwanese phone arrangement. The keys are listed row by row, `1` to `3` first; the last
 * row is `*`, `0`, and `#`, which is the punctuation key.
 */
object CompactDachenKeypad {
    val rows: List<List<SwipeKeySpec>> = listOf(
        listOf(
            SwipeKeySpec(B, up = P, right = M, left = F),
            SwipeKeySpec(D, up = T, right = N, left = L),
            SwipeKeySpec(TONE_2, up = TONE_3, right = TONE_4, left = TONE_LIGHT),
        ),
        listOf(
            SwipeKeySpec(G, up = K, right = H),
            SwipeKeySpec(J, up = Q, right = X),
            SwipeKeySpec(ZHI, up = CHI, right = SHI, left = RI),
        ),
        listOf(
            SwipeKeySpec(ZI, up = CI, right = SI),
            SwipeKeySpec(A, up = O, right = E, left = IE),
            SwipeKeySpec(AI, up = EI, right = AO, left = OU),
        ),
        listOf(
            SwipeKeySpec(YI, up = WU, right = YU),
            SwipeKeySpec(AN, up = EN, right = ANG, left = ENG, down = ER),
        ),
    )
}
