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

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_THEME_MODE
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.enums.ThemeMode
import org.ghostsinthelab.apps.guilelessbopomofo.utils.appSharedPreferences

/*
 * GuilelessTheme follows the user's ThemeMode inside Compose, but the windows themselves are
 * themed from XML (values / values-night) before Compose draws: on launch and between
 * screens. These keep them in step, so a theme picked against the phone's does not flash.
 */

/** The user's chosen [ThemeMode]. */
fun Context.userThemeMode(): ThemeMode =
    ThemeMode.fromValue(appSharedPreferences.getString(USER_THEME_MODE, ThemeMode.SYSTEM.value))

/**
 * From Android 12 on, tell the system this app's night mode, so that every window, the
 * starting window of a cold launch included, picks the matching resources. Call it on start
 * and whenever the user picks another mode.
 */
fun Context.applyThemeModeToApp(themeMode: ThemeMode = userThemeMode()) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val nightMode = when (themeMode) {
        ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
        ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
        ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
    }
    getSystemService<UiModeManager>()?.setApplicationNightMode(nightMode)
}

/**
 * Before Android 12 there is no per-app night mode: give the activity's window the chosen
 * theme's background instead, so at least moving between screens does not flash.
 */
fun Activity.applyThemeModeToWindow() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return
    val systemIsDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val isDark = userThemeMode().isDark(systemIsDark)
    val background = if (isDark) R.color.colorGuilelessBopomofoWineThemeBase10 else R.color.colorGuilelessBopomofoMorandiSurface
    window.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, background)))
}
