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
package org.ghostsinthelab.apps.guilelessbopomofo

import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import java.lang.reflect.Proxy

/** A text field that only writes down what the input method did to it. */
class RecordingInputConnection {
    val sentKeyEvents = mutableListOf<KeyEvent>()
    val contextMenuActions = mutableListOf<Int>()
    val committed = mutableListOf<String>()

    val connection: InputConnection = Proxy.newProxyInstance(
        InputConnection::class.java.classLoader, arrayOf(InputConnection::class.java)
    ) { _, method, args ->
        when (method.name) {
            "sendKeyEvent" -> { sentKeyEvents += args[0] as KeyEvent; true }
            "performContextMenuAction" -> { contextMenuActions += args[0] as Int; true }
            "commitText" -> { committed += args[0].toString(); true }
            "getTextBeforeCursor" -> ""
            "beginBatchEdit", "endBatchEdit", "deleteSurroundingText" -> true
            else -> error("unexpected ${method.name}")
        }
    } as InputConnection
}
