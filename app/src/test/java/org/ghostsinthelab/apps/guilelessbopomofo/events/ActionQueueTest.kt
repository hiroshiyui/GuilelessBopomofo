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
package org.ghostsinthelab.apps.guilelessbopomofo.events

import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionQueueTest {
    private val done = mutableListOf<ImeAction>()
    private val failed = mutableListOf<ImeAction>()
    private var handler: (ImeAction) -> Unit = { done += it }
    private val queue = ActionQueue({ handler(it) }) { action, _ -> failed += action }

    @Test
    fun anActionIsCarriedOutAtOnce() {
        queue.dispatch(ImeAction.UpdateBufferViews)
        assertEquals(listOf<ImeAction>(ImeAction.UpdateBufferViews), done)
    }

    @Test
    fun anActionAskedForByAHandler_waitsUntilThatHandlerReturns() {
        handler = { action ->
            if (action == ImeAction.UpdateBufferViews) {
                queue.dispatch(ImeAction.UpdateCursorPosition)
                // not yet: the handler is still at work
                assertEquals(emptyList<ImeAction>(), done)
            }
            done += action
        }
        queue.dispatch(ImeAction.UpdateBufferViews)
        assertEquals(listOf<ImeAction>(ImeAction.UpdateBufferViews, ImeAction.UpdateCursorPosition), done)
    }

    @Test
    fun nestedActions_keepTheirOrder() {
        handler = { action ->
            done += action
            if (action == ImeAction.UpdateBufferViews) {
                queue.dispatch(ImeAction.SwitchToLayout(Layout.MAIN))
                queue.dispatch(ImeAction.UpdateCursorPosition)
            }
            if (action == ImeAction.SwitchToLayout(Layout.MAIN)) {
                queue.dispatch(ImeAction.KeyboardLayoutSwitched)
            }
        }
        queue.dispatch(ImeAction.UpdateBufferViews)
        assertEquals(
            listOf<ImeAction>(
                ImeAction.UpdateBufferViews,
                ImeAction.SwitchToLayout(Layout.MAIN),
                ImeAction.UpdateCursorPosition,
                ImeAction.KeyboardLayoutSwitched,
            ),
            done,
        )
    }

    @Test
    fun aFailingHandler_isReported_andTheActionsBehindItStillRun() {
        handler = { action ->
            if (action == ImeAction.UpdateBufferViews) {
                queue.dispatch(ImeAction.UpdateCursorPosition)
                throw IllegalStateException("boom")
            }
            done += action
        }
        queue.dispatch(ImeAction.UpdateBufferViews)
        assertEquals(listOf<ImeAction>(ImeAction.UpdateBufferViews), failed)
        assertEquals(listOf<ImeAction>(ImeAction.UpdateCursorPosition), done)
    }

    @Test
    fun anError_getsThrough_andDropsWhatWasQueued() {
        handler = { action ->
            if (action == ImeAction.UpdateBufferViews) {
                queue.dispatch(ImeAction.UpdateCursorPosition)
                throw AssertionError("not even caught")
            }
            done += action
        }
        var thrown = false
        try {
            queue.dispatch(ImeAction.UpdateBufferViews)
        } catch (_: AssertionError) {
            thrown = true
        }
        assertEquals(true, thrown)
        // the queued one went with it, and the queue is usable again
        queue.dispatch(ImeAction.KeyboardLayoutSwitched)
        assertEquals(listOf<ImeAction>(ImeAction.KeyboardLayoutSwitched), done)
    }
}
