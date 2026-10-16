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

import android.content.res.Configuration
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.apps.guilelessbopomofo.keys.SwipeDirection
import org.ghostsinthelab.apps.guilelessbopomofo.keys.VirtualKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.BackspaceKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.CharacterKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.EnterKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ImeSwitchFunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ModeSwitchFunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.PunctuationFunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.ShiftKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.SpaceKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.SymbolFunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeySpec
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyStyle
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyboardSpec
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyboardVariant
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessDimens

/** Test tag of every key, for counting them. */
const val KEY_TEST_TAG = "key"

/** The [KeyboardVariant] for the screen this is composed on. */
@Composable
fun currentKeyboardVariant(): KeyboardVariant {
    val configuration = LocalConfiguration.current
    return KeyboardVariant.of(
        smallestScreenWidthDp = configuration.smallestScreenWidthDp,
        isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
    )
}

/** An on-screen keyboard: [spec]'s rows, each key as wide as its weight says. */
@Composable
fun Keyboard(spec: KeyboardSpec, uiState: ImeUiState, modifier: Modifier = Modifier) {
    val keyHeight = uiState.keyHeightDp.dp
    Column(modifier = modifier.fillMaxWidth()) {
        spec.rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { keySpec ->
                    // the IME switch key is there only when the user wants it
                    if (keySpec is KeySpec.Function && keySpec.key == FunctionKey.IME_SWITCH && !uiState.imeSwitchEnabled) {
                        return@forEach
                    }
                    KeyFor(keySpec, uiState, keyHeight, Modifier.weight(keySpec.weight))
                }
            }
        }
    }
}

@Composable
private fun KeyFor(keySpec: KeySpec, uiState: ImeUiState, keyHeight: Dp, modifier: Modifier) {
    when (keySpec) {
        is KeySpec.Character -> {
            val key = remember(keySpec.keyCode) { CharacterKey(keySpec.keyCode) }
            KeyButton(key, KeyStyle.NORMAL, keySpec.icon, keyHeight, modifier)
        }

        is KeySpec.Function -> {
            val key = remember(keySpec.key, uiState) { virtualKeyFor(keySpec.key, uiState) }
            val shiftBackground = if (keySpec.key == FunctionKey.SHIFT) shiftKeyColor(uiState.shiftKeyState) else null
            KeyButton(key, keySpec.key.style, keySpec.key.icon, keyHeight, modifier, shiftBackground)
        }
    }
}

internal fun virtualKeyFor(functionKey: FunctionKey, uiState: ImeUiState): VirtualKey = when (functionKey) {
    FunctionKey.MODE_SWITCH_TO_ENGLISH, FunctionKey.MODE_SWITCH_TO_BOPOMOFO -> ModeSwitchFunctionKey
    FunctionKey.SYMBOL -> SymbolFunctionKey
    FunctionKey.IME_SWITCH -> ImeSwitchFunctionKey
    FunctionKey.SPACE -> SpaceKey
    FunctionKey.PUNCTUATION -> PunctuationFunctionKey
    FunctionKey.BACKSPACE -> BackspaceKey
    FunctionKey.ENTER -> EnterKey
    FunctionKey.SHIFT -> ShiftKey(uiState)
}

@Composable
private fun shiftKeyColor(state: ShiftKey.ShiftKeyState): Color = when (state) {
    ShiftKey.ShiftKeyState.RELEASED -> MaterialTheme.colorScheme.tertiary
    ShiftKey.ShiftKeyState.PRESSED -> MaterialTheme.colorScheme.secondary
    ShiftKey.ShiftKeyState.HOLD -> MaterialTheme.colorScheme.primary
}

/** The background and content colours of a key in [style]. */
@Composable
internal fun keyColors(style: KeyStyle): Pair<Color, Color> = with(MaterialTheme.colorScheme) {
    when (style) {
        KeyStyle.NORMAL -> surfaceContainerHighest to onSurface
        KeyStyle.SPECIAL -> tertiary to onTertiary
        KeyStyle.ENTER -> secondary to onSecondary
    }
}

/** One key with an icon on it. */
@Composable
fun KeyButton(
    key: VirtualKey,
    style: KeyStyle,
    icon: Int,
    minHeight: Dp,
    modifier: Modifier = Modifier,
    backgroundOverride: Color? = null,
) {
    val (background, content) = keyColors(style)
    KeySurface(key, minHeight, backgroundOverride ?: background, modifier) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = content)
    }
}

/**
 * The raised, rounded face of a key. The whole of its slot, the gaps around the face
 * included, answers to touch, like the insets of the MaterialButton it replaces did. The
 * face fills whatever size the slot is given, and wraps its content otherwise.
 */
@Composable
fun KeySurface(
    key: VirtualKey,
    minHeight: Dp,
    background: Color,
    modifier: Modifier = Modifier,
    testTag: String = KEY_TEST_TAG,
    onSwipeDirectionChange: (SwipeDirection?) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = minHeight)
            .keyGestures(key, context, interactionSource, onSwipeDirectionChange)
            .padding(horizontal = GuilelessDimens.KeyHorizontalInset, vertical = GuilelessDimens.KeyVerticalInset),
        propagateMinConstraints = true,
    ) {
        Surface(
            shape = GuilelessDimens.KeyShape,
            color = background,
            shadowElevation = GuilelessDimens.KeyElevation,
        ) {
            Box(
                modifier = Modifier.indication(interactionSource, ripple()),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}
