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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.apps.guilelessbopomofo.BopomofoPhysicalKeyboards
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CANDIDATE_SELECTION_KEYS_OPTION
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv.USER_PHYSICAL_KEYBOARD_LAYOUT
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys
import org.ghostsinthelab.apps.guilelessbopomofo.utils.rememberStringPreference

private val DropdownSpacing = 8.dp
private val SelectionKeysSpacing = 16.dp
private val TipsSpacing = 24.dp
private val TipsIconSpacing = 8.dp

// In the order R.array.physical_bopomofo_keyboard_layouts names them.
private val PHYSICAL_KEYBOARD_LAYOUTS: List<String> = listOf(
    BopomofoPhysicalKeyboards.KB_DEFAULT.layout,
    BopomofoPhysicalKeyboards.KB_HSU.layout,
    BopomofoPhysicalKeyboards.KB_IBM.layout,
    BopomofoPhysicalKeyboards.KB_GIN_YIEH.layout,
    BopomofoPhysicalKeyboards.KB_ET.layout,
    BopomofoPhysicalKeyboards.KB_ET26.layout,
    BopomofoPhysicalKeyboards.KB_DVORAK.layout,
    BopomofoPhysicalKeyboards.KB_DVORAK_HSU.layout,
    BopomofoPhysicalKeyboards.KB_DACHEN_CP26.layout,
    BopomofoPhysicalKeyboards.KB_HANYU_PINYIN.layout,
    BopomofoPhysicalKeyboards.KB_THL_PINYIN.layout,
    BopomofoPhysicalKeyboards.KB_MPS2_PINYIN.layout,
    BopomofoPhysicalKeyboards.KB_CARPALX.layout,
    BopomofoPhysicalKeyboards.KB_COLEMAK_DH_ANSI.layout,
    BopomofoPhysicalKeyboards.KB_COLEMAK_DH_ORTH.layout,
    BopomofoPhysicalKeyboards.KB_WORKMAN.layout,
    BopomofoPhysicalKeyboards.KB_COLEMAK.layout,
)

// The radio buttons are labelled with the keys themselves.
private val SELECTION_KEY_OPTIONS: List<Pair<String, String>> = listOf(
    "1234567890" to SelectionKeys.NUMBER_ROW.set,
    "qwertyuiop" to SelectionKeys.TAB_ROW.set,
    "asdfghjkl;" to SelectionKeys.HOME_ROW.set,
    "asdfgqwert" to SelectionKeys.HOME_TAB_MIXED_MODE1.set,
    "hjkl;yuiop" to SelectionKeys.HOME_TAB_MIXED_MODE2.set,
    "aoeuidhtns" to SelectionKeys.DVORAK_HOME_ROW.set,
)

@Composable
fun PhysicalKeyboardSettingsScreen(modifier: Modifier = Modifier) {
    SettingsScreen(modifier) {
        SettingsPrimaryHeader(stringResource(R.string.physical_keyboard_settings))

        SettingsLabel(stringResource(R.string.physical_bopomofo_keyboard_layout))
        var physicalKeyboardLayout by rememberStringPreference(
            USER_PHYSICAL_KEYBOARD_LAYOUT, BopomofoPhysicalKeyboards.KB_DEFAULT.layout
        )
        DropdownPreference(
            entries = stringArrayResource(R.array.physical_bopomofo_keyboard_layouts).toList(),
            values = PHYSICAL_KEYBOARD_LAYOUTS,
            selected = physicalKeyboardLayout,
            onSelected = { physicalKeyboardLayout = it },
            modifier = Modifier.padding(top = DropdownSpacing),
        )

        SettingsLabel(stringResource(R.string.physical_selection_keys))
        var selectionKeys by rememberStringPreference(
            USER_CANDIDATE_SELECTION_KEYS_OPTION, SelectionKeys.NUMBER_ROW.set
        )
        Column(Modifier.padding(top = SelectionKeysSpacing)) {
            RadioGroupPreference(SELECTION_KEY_OPTIONS, selectionKeys) { selectionKeys = it }
        }

        SettingsLabel(stringResource(R.string.textViewSettingsConversionEnginesPhysical))
        SettingsDescription(stringResource(R.string.textViewSettingsDescriptionConversionEnginesPhysical))
        ConversionEnginePreference(USER_CONVERSION_ENGINE_WHEN_USING_PHYSICAL_KEYBOARD)

        Row(modifier = Modifier.padding(top = TipsSpacing)) {
            Image(painter = painterResource(android.R.drawable.ic_menu_info_details), contentDescription = null)
            Text(boldMarkupStringResource(R.string.tips_using_physical_keyboard), Modifier.padding(start = TipsIconSpacing))
        }
    }
}
