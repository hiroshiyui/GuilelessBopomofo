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
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.apps.guilelessbopomofo.R
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
            val resources = LocalContext.current.resources
            val label = remember(keySpec) {
                KeyLabels.forCharacterKey(resources.getResourceEntryName(keySpec.icon), keySpec.keyCode)
            }
            KeyButton(key, KeyStyle.NORMAL, keySpec.icon, label, keyHeight, modifier)
        }

        is KeySpec.Function -> {
            val key = remember(keySpec.key, uiState) { virtualKeyFor(keySpec.key, uiState) }
            val shift = if (keySpec.key == FunctionKey.SHIFT) shiftKeyAppearance(uiState.shiftKeyState) else null
            KeyButton(
                key, keySpec.key.style, keySpec.key.iconFor(uiState), keySpec.key.labelFor(uiState),
                keyHeight, modifier,
                colorsOverride = shift?.colors,
                marker = shift?.marker,
                stateDescription = shift?.let { stringResource(it.stateDescription) },
            )
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

/** Whether key faces are outlined; [ImeRoot] provides it from [ImeUiState.outlineKeys]. */
val LocalOutlineKeys = compositionLocalOf { false }

/** A small shape under a key's icon that tells its state apart from the others without colour. */
enum class KeyMarker { DOT, BAR }

/** How the Shift key looks and is described in one of its states. */
internal class ShiftKeyAppearance(
    val colors: Pair<Color, Color>,
    val marker: KeyMarker?,
    @param:StringRes val stateDescription: Int,
)

/** Each state in its own colour pair (glyph on face, so the glyph keeps its contrast) and marker. */
@Composable
internal fun shiftKeyAppearance(state: ShiftKey.ShiftKeyState): ShiftKeyAppearance = with(MaterialTheme.colorScheme) {
    when (state) {
        ShiftKey.ShiftKeyState.RELEASED -> ShiftKeyAppearance(tertiary to onTertiary, null, R.string.shift_state_off)
        ShiftKey.ShiftKeyState.PRESSED ->
            ShiftKeyAppearance(secondary to onSecondary, KeyMarker.DOT, R.string.shift_state_next_key)
        ShiftKey.ShiftKeyState.HOLD -> ShiftKeyAppearance(primary to onPrimary, KeyMarker.BAR, R.string.shift_state_locked)
    }
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

/**
 * One key with an icon on it; [label] is what a screen reader says for it, [stateDescription]
 * what it adds for a key with a state, which [marker] shows under the icon.
 */
@Composable
fun KeyButton(
    key: VirtualKey,
    style: KeyStyle,
    icon: Int,
    label: String,
    minHeight: Dp,
    modifier: Modifier = Modifier,
    colorsOverride: Pair<Color, Color>? = null,
    marker: KeyMarker? = null,
    stateDescription: String? = null,
) {
    val (background, content) = colorsOverride ?: keyColors(style)
    KeySurface(key, minHeight, background, modifier, label = label, stateDescription = stateDescription) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = content)
            if (marker != null) KeyMarkerShape(marker, content)
        }
    }
}

private val MarkerDotSize = 4.dp
private val MarkerBarWidth = 14.dp
private val MarkerBarHeight = 3.dp
private val MarkerSpacing = 2.dp

@Composable
private fun KeyMarkerShape(marker: KeyMarker, color: Color) {
    val size = when (marker) {
        KeyMarker.DOT -> DpSize(MarkerDotSize, MarkerDotSize)
        KeyMarker.BAR -> DpSize(MarkerBarWidth, MarkerBarHeight)
    }
    Box(
        modifier = Modifier
            .padding(top = MarkerSpacing)
            .size(size)
            .background(color, CircleShape),
    )
}

/**
 * The raised, rounded face of a key. The whole of its slot, the gaps around the face
 * included, answers to touch, like the insets of the MaterialButton it replaces did. The
 * face fills whatever size the slot is given, and wraps its content otherwise.
 *
 * To accessibility services the key is one button that a click taps. It is read as [label],
 * or as the text on its face when there is none, then [stateDescription] for a key that
 * has a state; [customActions] are what it does besides a tap, a swipe say.
 */
@Composable
fun KeySurface(
    key: VirtualKey,
    minHeight: Dp,
    background: Color,
    modifier: Modifier = Modifier,
    testTag: String = KEY_TEST_TAG,
    label: String? = null,
    stateDescription: String? = null,
    customActions: List<CustomAccessibilityAction> = emptyList(),
    onSwipeDirectionChange: (SwipeDirection?) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val host = LocalImeHost.current
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .testTag(testTag)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                if (label != null) contentDescription = label
                if (stateDescription != null) this.stateDescription = stateDescription
                if (customActions.isNotEmpty()) this.customActions = customActions
                onClick {
                    key.onAccessibilityClick(host)
                    true
                }
                if (key.detectsLongPress) {
                    onLongClick {
                        key.onDown(host)
                        key.onLongPress(host)
                        true
                    }
                }
            }
            .heightIn(min = minHeight)
            .keyGestures(key, host, interactionSource, onSwipeDirectionChange)
            .padding(horizontal = GuilelessDimens.KeyHorizontalInset, vertical = GuilelessDimens.KeyVerticalInset),
        propagateMinConstraints = true,
    ) {
        Surface(
            shape = GuilelessDimens.KeyShape,
            color = background,
            border = if (LocalOutlineKeys.current) {
                BorderStroke(GuilelessDimens.KeyBorderWidth, MaterialTheme.colorScheme.outline)
            } else {
                null
            },
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
