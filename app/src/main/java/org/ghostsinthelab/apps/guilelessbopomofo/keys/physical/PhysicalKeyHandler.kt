/*
 * Guileless Bopomofo
 * Copyright (C) 2025 YOU, HUI-HONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ghostsinthelab.apps.guilelessbopomofo.keys.physical

import android.content.Context
import android.view.KeyEvent

interface PhysicalKeyHandler {
    /**
     * Called when a key down event occurs for the key this handler is responsible for.
     *
     * @param context The context.
     * @param keyCode The value in event.getKeyCode().
     * @param event The key event. May be null if triggered from a soft key.
     * @return True if the event was handled, false otherwise.
     */
    fun onKeyDown(context: Context, keyCode: Int, event: KeyEvent?): Boolean

    /**
     * (Optional) Called when a key up event occurs.
     * You might not need this for all keys if onKeyDown handles everything.
     *
     * @param context The context.
     * @param keyCode The value in event.getKeyCode().
     * @param event The key event. May be null if triggered from a soft key.
     * @return True if the event was handled, false otherwise.
     */
    fun onKeyUp(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        // Default implementation: if not overridden, assume not handled by onKeyUp
        return false
    }

    /**
     * (Optional) Called when a key long press event occurs.
     * You might not need this for all keys if onKeyDown handles everything.
     *
     * @param context The context.
     * @param keyCode The value in event.getKeyCode().
     * @param event The key event. May be null if triggered from a soft key.
     * @return True if the event was handled, false otherwise.
     */
    fun onKeyLongPress(context: Context, keyCode: Int, event: KeyEvent?): Boolean {
        // Default implementation: if not overridden, assume not handled by onKeyLongPress
        return false
    }
}