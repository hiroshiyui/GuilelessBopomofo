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

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.SuggestionButton
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.KeyboardSpecs
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.SoftKeyboard
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.SettingsCurrentValue
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessColors
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessDimens
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessFonts
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Haptics
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus

/**
 * Resource id the input view answers to, for UI Automator, as the root of the old
 * `ime_layout.xml` did.
 */
const val IME_ROOT_TEST_TAG = "keyboardView"

/** The whole input view: buffers or suggestions, then whichever panel [uiState] asks for. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImeRoot(uiState: ImeUiState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { testTagsAsResourceId = true }
            .testTag(IME_ROOT_TEST_TAG),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (uiState.showBuffers) {
                BufferArea(uiState)
            }
            if (uiState.suggestions.isNotEmpty()) {
                SuggestionStrip(uiState.suggestions)
            }
            KeyboardPanel(
                uiState,
                Modifier.padding(vertical = GuilelessDimens.KeyboardPanelVerticalPadding),
            )
            BottomGradientSpacer()
        }
    }
}

@Composable
private fun KeyboardPanel(uiState: ImeUiState, modifier: Modifier = Modifier) {
    when (uiState.layout) {
        Layout.MAIN -> KeyboardSpecs.of(uiState.softKeyboard, currentKeyboardVariant())
            ?.let { Keyboard(it, uiState, modifier) }
            ?: PhoneKeypad(uiState, modifier)

        Layout.QWERTY -> Keyboard(requireNotNull(KeyboardSpecs.of(SoftKeyboard.QWERTY, currentKeyboardVariant())), uiState, modifier)
        Layout.CANDIDATES, Layout.SYMBOLS -> CandidatesPanel(uiState, modifier)
        Layout.COMPACT -> CompactBar(uiState, modifier)
    }
}

/**
 * From Android 15 on the input view reaches down behind the navigation bar; fill that part
 * with a gradient rather than leave the keys under it.
 */
@Composable
private fun BottomGradientSpacer() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsBottomHeight(WindowInsets.navigationBars)
            .background(BottomGradient),
    )
}

private val BottomGradient = Brush.verticalGradient(
    0f to Color.Transparent,
    0.2f to Color(0x11AAAAAA),
    1f to Color(0x7C666666),
)

/** The pre-edit and Bopomofo buffers, side by side, wrapping when they get long. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BufferArea(uiState: ImeUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = GuilelessDimens.BufferElevation,
    ) {
        FlowRow(modifier = Modifier.fillMaxWidth()) {
            PreEditBufferText(uiState)
            Text(
                text = uiState.bopomofoText,
                modifier = Modifier
                    .heightIn(min = GuilelessDimens.BufferMinHeight)
                    .background(GuilelessColors.BopomofoBufferBackground)
                    .padding(horizontal = GuilelessDimens.BopomofoBufferPadding),
                color = GuilelessColors.BopomofoBufferText,
                style = BufferTextStyle,
            )
        }
    }
}

private val BufferTextStyle = TextStyle(
    fontFamily = GuilelessFonts.Buffer,
    fontSize = GuilelessDimens.BufferTextSize,
    lineHeight = GuilelessDimens.BufferLineHeight,
)

/**
 * The converted text, the character under the cursor underlined. Touching a character moves
 * the cursor there and offers the candidates for it.
 */
@Composable
private fun PreEditBufferText(uiState: ImeUiState) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val text = uiState.preEditText
    val cursorIndex = uiState.preEditCursorIndex

    // Keep the text clear of the edges, so the characters at either end are easy to hit.
    val padding = when {
        text.isEmpty() -> PaddingValues(0.dp)
        uiState.preEditCursorAtEnd -> PaddingValues(
            start = GuilelessDimens.PreEditBufferPadding, end = GuilelessDimens.PreEditBufferPadding * 2
        )

        else -> PaddingValues(horizontal = GuilelessDimens.PreEditBufferPadding)
    }
    val currentPadding by rememberUpdatedState(padding)

    Text(
        text = buildAnnotatedString {
            append(text)
            text.utf16RangeOfCharacter(cursorIndex)?.let { range ->
                addStyle(SpanStyle(textDecoration = TextDecoration.Underline), range.first, range.last + 1)
            }
        },
        modifier = Modifier
            .background(GuilelessColors.PreEditBufferBackground)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startPadding = currentPadding.calculateLeftPadding(layoutDirection).toPx()
                    val utf16Offset = textLayout?.getOffsetForPosition(down.position - Offset(startPadding, 0f)) ?: 0
                    val offset = uiState.preEditText.characterIndexOf(utf16Offset)
                    Haptics.performVibration(context, Vibratable.VibrationStrength.LIGHT)
                    if (waitForUpOrCancellation() != null) {
                        EventBus.getDefault().post(Events.PreEditBufferTouched(offset))
                    }
                }
            }
            .padding(padding),
        color = GuilelessColors.PreEditBufferText,
        style = BufferTextStyle,
        onTextLayout = { textLayout = it },
    )
}

// libchewing counts the buffer in characters (code points), Compose text in UTF-16 units, and
// a character outside the BMP, such as an Ext-B Han character or an emoji, takes two of them.

/** The character a UTF-16 [utf16Offset] into this string falls at. */
internal fun String.characterIndexOf(utf16Offset: Int): Int = codePointCount(0, utf16Offset.coerceIn(0, length))

/** The UTF-16 units of this string's [characterIndex]-th character, or null if there is none. */
internal fun String.utf16RangeOfCharacter(characterIndex: Int): IntRange? {
    if (characterIndex !in 0 until codePointCount(0, length)) return null
    val start = offsetByCodePoints(0, characterIndex)
    return start until offsetByCodePoints(start, 1)
}

/** English words to pick from, most likely first; swipe it for the less likely ones. */
@Composable
private fun SuggestionStrip(words: List<String>) {
    val listState = rememberLazyListState()
    // A new list starts from its beginning.
    LaunchedEffect(words) { listState.scrollToItem(0) }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(GuilelessDimens.SuggestionStripHeight),
        contentPadding = PaddingValues(horizontal = GuilelessDimens.SuggestionStripPadding),
    ) {
        items(words, key = { it }) { word ->
            val virtualKey = remember(word) { SuggestionButton(word) }
            KeySurface(
                key = virtualKey,
                minHeight = GuilelessDimens.SuggestionStripHeight,
                background = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = GuilelessDimens.SuggestionMargin),
            ) {
                Text(
                    text = word,
                    modifier = Modifier
                        .widthIn(min = GuilelessDimens.SuggestionMinWidth)
                        .padding(horizontal = GuilelessDimens.SuggestionHorizontalPadding),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = GuilelessDimens.SuggestionTextSize,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * What is left of the keyboard with a physical one attached: which modes are on, centred in
 * the panel as the old KeyboardPanel's gravity had it.
 */
@Composable
private fun CompactBar(uiState: ImeUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_guileless_bopomofo_statusbar_icon),
            contentDescription = stringResource(R.string.app_name),
        )
        Text(
            text = stringResource(R.string.current_mode),
            modifier = Modifier.padding(start = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        SettingsCurrentValue(
            text = stringResource(if (uiState.compactChineseMode) R.string.mode_bopomofo else R.string.mode_alphanumerical),
            modifier = Modifier.padding(start = 2.dp),
        )
        Text(
            text = stringResource(if (uiState.compactFullWidth) R.string.full_width_mode else R.string.half_width_mode),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
