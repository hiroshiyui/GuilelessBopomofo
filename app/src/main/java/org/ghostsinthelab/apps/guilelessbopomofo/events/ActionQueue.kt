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

/**
 * Carries out actions one at a time, in the order they were asked for. An action asked for
 * while another is being carried out waits its turn: the handler that asked sees it done
 * after it returns, not in the middle of its own work.
 *
 * A handler that fails is reported to [onFailure] and does not stop the actions queued
 * behind it. Only an [Error] gets through, and then the queue is dropped along with it.
 *
 * Not thread-safe: meant for the main thread only.
 */
class ActionQueue(
    private val handle: (ImeAction) -> Unit,
    private val onFailure: (ImeAction, RuntimeException) -> Unit,
) : ImeDispatcher {
    private val pending = ArrayDeque<ImeAction>()
    private var dispatching = false

    override fun dispatch(action: ImeAction) {
        pending.addLast(action)
        if (dispatching) return
        dispatching = true
        try {
            while (pending.isNotEmpty()) carryOut(pending.removeFirst())
        } finally {
            // whatever happened, the next action starts with a clean slate
            pending.clear()
            dispatching = false
        }
    }

    private fun carryOut(action: ImeAction) {
        try {
            handle(action)
        } catch (exception: RuntimeException) {
            onFailure(action, exception)
        }
    }
}
