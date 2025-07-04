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

// ChiEngMode:
// 0: Symbol (alphanumeric) mode
// 1: Chinese (Bopomofo) mode
enum class ChiEngMode(val mode: Int) {
    SYMBOL(0), CHINESE(1)
}

enum class ShapeMode(val mode: Int) {
    HALF(0), FULL(1)
}

enum class BopomofoKeyboards(val layout: String) {
    KB_DEFAULT("KB_DEFAULT"),
    KB_ET26("KB_ET26"),
    KB_HSU("KB_HSU"),
    KB_DVORAK_HSU("KB_DVORAK_HSU"),
    KB_ET("KB_ET")
}

enum class ConversionEngines(val mode: Int) {
    SIMPLE_CONVERSION_ENGINE(0),
    CHEWING_CONVERSION_ENGINE(1),
    FUZZY_CHEWING_CONVERSION_ENGINE(2)
}

object ChewingBridge {
    val chewing: Chewing = Chewing()
}