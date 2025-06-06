/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
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

package org.ghostsinthelab.apps.guilelessbopomofo

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

object ChewingBridge {
    val chewing: Chewing = Chewing()
}