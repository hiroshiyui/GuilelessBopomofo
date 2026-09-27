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

package org.ghostsinthelab.apps.guilelessbopomofo.prediction

/**
 * Offers English words that the given word being typed could become.
 */
fun interface WordPredictor {
    /**
     * @param prefix the part of the word typed so far, as the user typed it.
     * @param limit how many words to return at most.
     * @return the most likely words first, already in the letter case [prefix] suggests,
     * never [prefix] itself.
     */
    fun suggest(prefix: String, limit: Int): List<String>
}
