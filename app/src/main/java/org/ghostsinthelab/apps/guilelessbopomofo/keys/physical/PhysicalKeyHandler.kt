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

package org.ghostsinthelab.apps.guilelessbopomofo.keys.physical

import android.view.KeyEvent
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ImeHost

interface PhysicalKeyHandler {
    /**
     * Called when a key down event occurs for the key this handler is responsible for.
     *
     * @param host The input method the key acts through.
     * @param keyCode The value in event.getKeyCode().
     * @param event The key event. May be null if triggered from a soft key.
     * @return True if the event was handled, false otherwise.
     */
    fun onKeyDown(host: ImeHost, keyCode: Int, event: KeyEvent?): Boolean

    /**
     * (Optional) Called when a key up event occurs.
     * You might not need this for all keys if onKeyDown handles everything.
     *
     * @param host The input method the key acts through.
     * @param keyCode The value in event.getKeyCode().
     * @param event The key event. May be null if triggered from a soft key.
     * @return True if the event was handled, false otherwise.
     */
    fun onKeyUp(host: ImeHost, keyCode: Int, event: KeyEvent?): Boolean {
        // Default implementation: if not overridden, assume not handled by onKeyUp
        return false
    }

    /**
     * (Optional) Called when a key long press event occurs.
     * You might not need this for all keys if onKeyDown handles everything.
     *
     * @param host The input method the key acts through.
     * @param keyCode The value in event.getKeyCode().
     * @param event The key event. May be null if triggered from a soft key.
     * @return True if the event was handled, false otherwise.
     */
    fun onKeyLongPress(host: ImeHost, keyCode: Int, event: KeyEvent?): Boolean {
        // Default implementation: if not overridden, assume not handled by onKeyLongPress
        return false
    }
}