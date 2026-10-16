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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.apps.guilelessbopomofo.keys.SwipeDirection
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.SwipeCharacterKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.BopomofoGlyph
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.CompactDachenKeypad
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyStyle
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SwipeKeySpec
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.LocalGuilelessThemeExtras

// The keypad has four rows where the other keyboards have five; make them taller to match.
private const val KEYPAD_ROW_HEIGHT_FACTOR = 5f / 4f

// Column widths: the function key columns either side, and the number pad's columns.
private const val SIDE_COLUMN_WEIGHT = 1f
private const val DIGIT_COLUMN_WEIGHT = 1.4f

// The tap glyph in the middle, the swipe glyphs around a key's edge, and how faint the rest
// go while one is aimed at. Sized so that all five fit a key face without overlapping.
private val TapGlyphSize = 22.dp
private val SwipeGlyphSize = 13.dp
private val SwipeGlyphEdgePadding = 2.dp
private const val SWIPE_GLYPH_ALPHA = 0.7f
private const val NOT_AIMED_ALPHA = 0.25f

/**
 * The compact Dachen keyboard: a feature phone's number pad, flanked by Aa, symbols and the
 * IME switch on the left and Backspace, Space and a tall Enter on the right.
 */
@Composable
fun PhoneKeypad(uiState: ImeUiState, modifier: Modifier = Modifier) {
    val rowHeight = (uiState.keyHeightDp * KEYPAD_ROW_HEIGHT_FACTOR).dp
    val rows = CompactDachenKeypad.rows

    Row(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(SIDE_COLUMN_WEIGHT)) {
            FunctionKeyCell(FunctionKey.MODE_SWITCH_TO_ENGLISH, uiState, rowHeight)
            FunctionKeyCell(FunctionKey.SYMBOL, uiState, rowHeight)
            // the IME switch key is there only when the user wants it
            if (uiState.imeSwitchEnabled) {
                FunctionKeyCell(FunctionKey.IME_SWITCH, uiState, rowHeight)
            } else {
                Spacer(Modifier.height(rowHeight))
            }
            Spacer(Modifier.height(rowHeight))
        }
        repeat(rows.maxOf { it.size }) { column ->
            Column(modifier = Modifier.weight(DIGIT_COLUMN_WEIGHT)) {
                rows.forEach { row ->
                    // The `#` slot, after `*` and `0`, is the punctuation key.
                    row.getOrNull(column)?.let { SwipeKeyCell(it, rowHeight) }
                        ?: FunctionKeyCell(FunctionKey.PUNCTUATION, uiState, rowHeight)
                }
            }
        }
        Column(modifier = Modifier.weight(SIDE_COLUMN_WEIGHT)) {
            FunctionKeyCell(FunctionKey.BACKSPACE, uiState, rowHeight)
            FunctionKeyCell(FunctionKey.SPACE, uiState, rowHeight)
            FunctionKeyCell(FunctionKey.ENTER, uiState, rowHeight * 2)
        }
    }
}

@Composable
private fun FunctionKeyCell(functionKey: FunctionKey, uiState: ImeUiState, height: Dp) {
    val key = remember(functionKey, uiState) { virtualKeyFor(functionKey, uiState) }
    KeyButton(
        key = key,
        style = functionKey.style,
        icon = functionKey.icon,
        minHeight = height,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    )
}

/**
 * A number key: the tap symbol large in the middle, the swipe symbols small at the edges
 * they are swiped towards. While a swipe is under way the symbol it is aimed at stands out.
 */
@Composable
private fun SwipeKeyCell(spec: SwipeKeySpec, height: Dp) {
    val key = remember(spec) { SwipeCharacterKey(spec) }
    var aimedAt by remember { mutableStateOf<SwipeDirection?>(null) }
    val (background, content) = keyColors(KeyStyle.NORMAL)
    val highlight = LocalGuilelessThemeExtras.current.primaryText
    val aimedGlyph = aimedAt?.let(spec::glyphFor)

    KeySurface(
        key = key,
        minHeight = height,
        background = background,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        onSwipeDirectionChange = { aimedAt = it },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            fun tintFor(glyph: BopomofoGlyph, restingAlpha: Float): Color = when {
                aimedAt == null -> content.copy(alpha = restingAlpha)
                glyph == aimedGlyph -> highlight
                else -> content.copy(alpha = NOT_AIMED_ALPHA)
            }

            Glyph(spec.tap, tintFor(spec.tap, restingAlpha = 1f), Alignment.Center, TapGlyphSize)
            spec.up?.let { Glyph(it, tintFor(it, SWIPE_GLYPH_ALPHA), Alignment.TopCenter) }
            spec.right?.let { Glyph(it, tintFor(it, SWIPE_GLYPH_ALPHA), Alignment.CenterEnd) }
            spec.left?.let { Glyph(it, tintFor(it, SWIPE_GLYPH_ALPHA), Alignment.CenterStart) }
            spec.down?.let { Glyph(it, tintFor(it, SWIPE_GLYPH_ALPHA), Alignment.BottomCenter) }
        }
    }
}

/** One glyph, at [alignment] within the key. */
@Composable
private fun BoxScope.Glyph(glyph: BopomofoGlyph, tint: Color, alignment: Alignment, size: Dp = SwipeGlyphSize) {
    Icon(
        painter = painterResource(glyph.icon),
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .align(alignment)
            .padding(SwipeGlyphEdgePadding)
            .size(size),
    )
}
