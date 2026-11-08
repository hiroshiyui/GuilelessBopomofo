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

import android.view.inputmethod.EditorInfo
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.ui.ime.layouts.FunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EnterKeyBehavior

/** The icon that tells what the on-screen Enter will do: a return arrow, or the action's symbol. */
@DrawableRes
fun enterKeyIcon(behavior: EnterKeyBehavior): Int = when (behavior) {
    EnterKeyBehavior.NewLine -> R.drawable.ic_keyboard_return_24
    is EnterKeyBehavior.EditorAction -> when (behavior.actionId) {
        EditorInfo.IME_ACTION_SEND -> R.drawable.ic_keyboard_action_send_24
        EditorInfo.IME_ACTION_SEARCH -> R.drawable.ic_keyboard_action_search_24
        EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT -> R.drawable.ic_keyboard_action_forward_24
        EditorInfo.IME_ACTION_PREVIOUS -> R.drawable.ic_keyboard_action_back_24
        // IME_ACTION_DONE, and the action ids apps make up for a custom action label
        else -> R.drawable.ic_keyboard_action_done_24
    }
}

/** What a screen reader says for the on-screen Enter: the action it will perform. */
@StringRes
fun enterKeyLabel(behavior: EnterKeyBehavior): Int = when (behavior) {
    EnterKeyBehavior.NewLine -> R.string.key_enter
    is EnterKeyBehavior.EditorAction -> when (behavior.actionId) {
        EditorInfo.IME_ACTION_SEND -> R.string.key_enter_send
        EditorInfo.IME_ACTION_SEARCH -> R.string.key_enter_search
        EditorInfo.IME_ACTION_GO -> R.string.key_enter_go
        EditorInfo.IME_ACTION_NEXT -> R.string.key_enter_next
        EditorInfo.IME_ACTION_PREVIOUS -> R.string.key_enter_previous
        else -> R.string.key_enter_done
    }
}

/** The icon to draw on this key right now; only Enter's changes with the text field. */
@DrawableRes
fun FunctionKey.iconFor(uiState: ImeUiState): Int =
    if (this == FunctionKey.ENTER) enterKeyIcon(uiState.enterKeyBehavior) else icon

/** What a screen reader says for this key right now; only Enter's changes with the text field. */
@Composable
fun FunctionKey.labelFor(uiState: ImeUiState): String =
    stringResource(if (this == FunctionKey.ENTER) enterKeyLabel(uiState.enterKeyBehavior) else label)
