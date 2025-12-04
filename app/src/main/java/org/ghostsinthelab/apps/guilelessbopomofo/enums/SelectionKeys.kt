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

package org.ghostsinthelab.apps.guilelessbopomofo.enums

enum class SelectionKeys(val keys: IntArray, val set: String) {
    NUMBER_ROW(
        charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { it.code }.toIntArray(),
        "NUMBER_ROW"
    ),
    HOME_ROW(
        charArrayOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';').map { it.code }.toIntArray(),
        "HOME_ROW"
    ),
    HOME_TAB_MIXED_MODE1(
        charArrayOf('a', 's', 'd', 'f', 'g', 'q', 'w', 'e', 'r', 't').map { it.code }.toIntArray(),
        "HOME_TAB_MIXED_MODE1"
    ),
    HOME_TAB_MIXED_MODE2(
        charArrayOf('h', 'j', 'k', 'l', ';', 'y', 'u', 'i', 'o', 'p').map { it.code }.toIntArray(),
        "HOME_TAB_MIXED_MODE2"
    )
}