/*
 * Guileless Bopomofo
 * Copyright (C) 2026.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
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

import org.ghostsinthelab.apps.guilelessbopomofo.enums.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun followingTheSystem_isDarkWhenThePhoneIs() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemIsDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemIsDark = false))
    }

    @Test
    fun aPickedMode_ignoresThePhone() {
        listOf(true, false).forEach { systemIsDark ->
            assertFalse(ThemeMode.LIGHT.isDark(systemIsDark))
            assertTrue(ThemeMode.DARK.isDark(systemIsDark))
        }
    }

    @Test
    fun storedValues_roundTrip_andAnythingElseFollowsTheSystem() {
        ThemeMode.entries.forEach { assertEquals(it, ThemeMode.fromValue(it.value)) }
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromValue(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromValue("SEPIA"))
    }
}
