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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

// The scrims enableEdgeToEdge() puts behind three-button navigation by default.
private val LightNavigationBarScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DarkNavigationBarScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B)

/**
 * Draws the activity edge to edge, with the system bars' icons light or dark as the theme
 * is rather than as the phone is, since the user may have picked a theme of their own.
 * Call it inside [GuilelessTheme].
 */
@Composable
fun ComponentActivity.EdgeToEdgeFollowingTheme() {
    val isDark = LocalGuilelessThemeExtras.current.isDark
    DisposableEffect(isDark) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark },
            navigationBarStyle = SystemBarStyle.auto(LightNavigationBarScrim, DarkNavigationBarScrim) { isDark },
        )
        onDispose {}
    }
}
