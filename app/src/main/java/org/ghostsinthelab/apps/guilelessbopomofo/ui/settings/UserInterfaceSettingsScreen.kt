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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_DYNAMIC_COLOR
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_ENABLE_IME_SWITCH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_LANDSCAPE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_FULLSCREEN_WHEN_IN_PORTRAIT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_HAPTIC_FEEDBACK_STRENGTH
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_KEY_BUTTON_HEIGHT
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_THEME_MODE
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.enums.ThemeMode
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessDimens
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.applyThemeModeToApp
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.dynamicColorAvailable
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Haptics
import org.ghostsinthelab.apps.guilelessbopomofo.utils.rememberIntPreference
import org.ghostsinthelab.apps.guilelessbopomofo.utils.rememberStringPreference

private val HAPTIC_FEEDBACK_STRENGTH_RANGE = 0..255
private val KEY_BUTTON_HEIGHT_RANGE_DP = 40..80
private val DEFAULT_KEY_BUTTON_HEIGHT_DP = GuilelessDimens.KeyMinHeightDefault.value.toInt()

@Composable
fun UserInterfaceSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    SettingsScreen(modifier) {
        SettingsPrimaryHeader(stringResource(R.string.user_interface_settings))

        SettingsLabel(stringResource(R.string.haptic_feedback_strength))
        SettingsDescription(stringResource(R.string.haptic_feedback_strength_description))
        var hapticFeedbackStrength by rememberIntPreference(
            USER_HAPTIC_FEEDBACK_STRENGTH, GuilelessBopomofoService.defaultHapticFeedbackStrength
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IntSliderPreference(
                value = hapticFeedbackStrength,
                range = HAPTIC_FEEDBACK_STRENGTH_RANGE,
                onValueChange = { strength ->
                    if (strength != hapticFeedbackStrength) {
                        // Let the user feel what they are choosing.
                        Haptics.performVibration(context, strength)
                        hapticFeedbackStrength = strength
                    }
                },
                modifier = Modifier.weight(1f),
            )
            SettingsCurrentValue(stringResource(R.string.haptic_feedback_strength_setting, hapticFeedbackStrength))
        }
        BooleanPreferenceSwitch(
            R.string.setting_apply_same_haptic_feedback_strength_to_function_buttons,
            SAME_HAPTIC_FEEDBACK_TO_FUNCTION_BUTTONS,
            false,
        )

        SettingsLabel(stringResource(R.string.fullscreen_mode_setting_title))
        SettingsDescription(stringResource(R.string.fullscreen_mode_setting_description))
        BooleanPreferenceSwitch(R.string.fullscreen_when_in_landscape_setting_switch, USER_FULLSCREEN_WHEN_IN_LANDSCAPE, true)
        BooleanPreferenceSwitch(R.string.fullscreen_when_in_portrait_setting_switch, USER_FULLSCREEN_WHEN_IN_PORTRAIT, false)

        SettingsLabel(stringResource(R.string.key_button_height_label))
        SettingsDescription(stringResource(R.string.key_button_height_description, DEFAULT_KEY_BUTTON_HEIGHT_DP))
        var keyButtonHeight by rememberIntPreference(
            USER_KEY_BUTTON_HEIGHT, DEFAULT_KEY_BUTTON_HEIGHT_DP
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IntSliderPreference(
                value = keyButtonHeight,
                range = KEY_BUTTON_HEIGHT_RANGE_DP,
                onValueChange = { keyButtonHeight = it },
                modifier = Modifier.weight(1f),
            )
            SettingsCurrentValue(stringResource(R.string.key_button_height_setting, keyButtonHeight))
        }

        SettingsLabel(stringResource(R.string.text_view_setting_ime_switch))
        BooleanPreferenceSwitch(R.string.switch_setting_enable_ime_switch, USER_ENABLE_IME_SWITCH, false)
        BooleanPreferenceSwitch(R.string.switch_setting_ime_switch, USER_ENABLE_DOUBLE_TOUCH_IME_SWITCH, false)

        SettingsLabel(stringResource(R.string.theme_setting_title))
        SettingsDescription(stringResource(R.string.theme_setting_description))
        var themeMode by rememberStringPreference(USER_THEME_MODE, ThemeMode.SYSTEM.value)
        RadioGroupPreference(
            options = listOf(
                stringResource(R.string.theme_follow_system) to ThemeMode.SYSTEM.value,
                stringResource(R.string.theme_light) to ThemeMode.LIGHT.value,
                stringResource(R.string.theme_dark) to ThemeMode.DARK.value,
            ),
            selected = themeMode,
            onSelected = {
                themeMode = it
                context.applyThemeModeToApp(ThemeMode.fromValue(it))
            },
        )
        // Material You's wallpaper colours only exist from Android 12 on.
        if (dynamicColorAvailable) {
            BooleanPreferenceSwitch(R.string.dynamic_color_setting_switch, USER_DYNAMIC_COLOR, false)
        }
    }
}
