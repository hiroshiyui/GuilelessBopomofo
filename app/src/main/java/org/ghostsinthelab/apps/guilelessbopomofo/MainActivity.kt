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

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.GeneralSettingsScreen
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.PhysicalKeyboardSettingsScreen
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.UserInterfaceSettingsScreen
import org.ghostsinthelab.apps.guilelessbopomofo.ui.settings.UserPhraseManagerScreen
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessTheme

class MainActivity : ComponentActivity() {
    private val logTag: String = "MainActivity"

    companion object {
        // How many taps on the app icon it takes to reveal the engineering mode.
        private const val ENGINEERING_MODE_ENTER_CLICKS = 5
    }

    private var engineeringModeEnterCount: Int = 0
    private var engineeringModeEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(logTag, "onCreate()")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GuilelessTheme {
                SettingsApp(onAppIconClick = ::onAppIconClick)
            }
        }
    }

    private fun onAppIconClick() {
        if (engineeringModeEnterCount >= ENGINEERING_MODE_ENTER_CLICKS || engineeringModeEnabled) {
            engineeringModeEnabled = true
            startActivity(Intent(this, EngineeringModeActivity::class.java))
        } else {
            engineeringModeEnterCount += 1
        }
    }
}

/** The settings tabs, in the order the bottom navigation shows them. */
private enum class SettingsTab(@param:StringRes val title: Int, @param:DrawableRes val icon: Int) {
    GENERAL(R.string.general_settings_label, R.drawable.ic_build_settings),
    USER_INTERFACE(R.string.user_interface_settings, R.drawable.ic_touch_app),
    PHYSICAL_KEYBOARD(R.string.physical_keyboard_settings, R.drawable.ic_keyboard),
    USER_PHRASES(R.string.user_phrase_manager_title, R.drawable.ic_dictionary),
}

private val SystemInsets: WindowInsets
    @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)

@Composable
private fun SettingsApp(onAppIconClick: () -> Unit) {
    var currentTab by rememberSaveable { mutableStateOf(SettingsTab.GENERAL) }
    // In landscape the header is hidden to leave room for the settings themselves.
    val showHeader = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            val topInsets = SystemInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            if (showHeader) {
                SettingsHeader(onAppIconClick, Modifier.windowInsetsPadding(topInsets))
                HorizontalDivider()
            }
            val contentModifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .let { if (showHeader) it.consumeWindowInsets(topInsets) else it.windowInsetsPadding(topInsets) }
            when (currentTab) {
                SettingsTab.GENERAL -> GeneralSettingsScreen(contentModifier)
                SettingsTab.USER_INTERFACE -> UserInterfaceSettingsScreen(contentModifier)
                SettingsTab.PHYSICAL_KEYBOARD -> PhysicalKeyboardSettingsScreen(contentModifier)
                SettingsTab.USER_PHRASES -> UserPhraseManagerScreen(contentModifier)
            }
            NavigationBar(windowInsets = SystemInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)) {
                SettingsTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == currentTab,
                        onClick = { currentTab = tab },
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = { Text(stringResource(tab.title)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorResource(R.color.bottomNavIconSelected),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(onAppIconClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIcon(
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(AppIconSize)
                .clickable(onClick = onAppIconClick),
        )
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(top = 4.dp),
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME, ChewingBridge.chewing.version()),
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

private val AppIconSize = 48.dp

/** The launcher icon is an adaptive icon, which painterResource can not draw, so rasterize it. */
@Composable
private fun AppIcon(contentDescription: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { AppIconSize.roundToPx() }
    val bitmap = remember(sizePx) {
        ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)?.toBitmap(sizePx, sizePx)?.asImageBitmap()
    } ?: return
    Image(bitmap = bitmap, contentDescription = contentDescription, modifier = modifier)
}
