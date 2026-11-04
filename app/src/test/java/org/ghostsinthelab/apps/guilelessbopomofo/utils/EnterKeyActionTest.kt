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

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnterKeyActionTest {
    private val performed = mutableListOf<Int>()
    private var fieldAnswers = true

    private val connection: InputConnection = Proxy.newProxyInstance(
        InputConnection::class.java.classLoader, arrayOf(InputConnection::class.java)
    ) { _, method, args ->
        when (method.name) {
            "performEditorAction" -> { performed += args[0] as Int; fieldAnswers }
            else -> error("unexpected ${method.name}")
        }
    } as InputConnection

    @Test
    fun anEditorAction_isPerformedOnTheField() {
        assertTrue(EnterKeyAction.perform(EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_SEND), connection))
        assertEquals(listOf(EditorInfo.IME_ACTION_SEND), performed)
    }

    @Test
    fun aNewLine_isLeftToAPlainEnterKey() {
        assertFalse(EnterKeyAction.perform(EnterKeyBehavior.NewLine, connection))
        assertEquals(emptyList<Int>(), performed)
    }

    @Test
    fun aDeadConnection_fallsBackToAPlainEnterKey() {
        fieldAnswers = false
        assertFalse(EnterKeyAction.perform(EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_GO), connection))
        assertFalse(EnterKeyAction.perform(EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_GO), null))
    }
}
