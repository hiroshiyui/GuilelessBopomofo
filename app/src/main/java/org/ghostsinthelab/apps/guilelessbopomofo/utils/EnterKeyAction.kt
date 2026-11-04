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
package org.ghostsinthelab.apps.guilelessbopomofo.utils

import android.view.inputmethod.InputConnection

/**
 * Carries out what Enter should do once there is nothing left to commit, as
 * [EnterKeyBehaviorResolver] worked it out.
 */
object EnterKeyAction {
    /**
     * Performs [behavior] on [inputConnection].
     *
     * @return whether that was enough; false means a plain Enter key press is what is
     * needed, either because the behavior is a line break or because the input connection
     * died under us. A false return from the text field never means it turned the action
     * down, so the plain Enter is a last resort that will quietly do nothing in that case.
     */
    fun perform(behavior: EnterKeyBehavior, inputConnection: InputConnection?): Boolean = when (behavior) {
        is EnterKeyBehavior.EditorAction -> inputConnection?.performEditorAction(behavior.actionId) == true
        EnterKeyBehavior.NewLine -> false
    }
}
